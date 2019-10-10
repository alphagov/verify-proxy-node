#! /usr/bin/python

from sys import exit
from urllib2 import urlopen
from OpenSSL import crypto
from dateutil import parser, tz
from datetime import datetime
from re import findall
from xml.etree.ElementTree import fromstring
from HTMLParser import HTMLParser


BASE_URL = "https://%s-proxy-node.london.verify.govsvc.uk"

METADATA_ROOT = "/ServiceMetadata"
SIGNING_CERTS_ROOT = "/ServiceMetadataSigningCertificates" 

HEADER = "-----BEGIN CERTIFICATE-----\n"
FOOTER = "\n-----END CERTIFICATE-----"

FIND_CERT_REGEX = r"(-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----)"

names_spaces = {
    'ds': 'http://www.w3.org/2000/09/xmldsig#'
}

connected_countries = {
    "test-integration": "Test Intergration",
    "nl": "Netherlands Prod",
    "nl-integration": "Netherlands Intergration",
    "it": "Italy Prod",
    "it-integration": "Italy Intergration",
    "se": "Sweden Prod",
    "se-integration": "Sweden Intergration",
    "cz": "Czech Republic Prod",
    "cz-integration": "Czech Republic Intergration"
}

now = datetime.utcnow()

def colored(text, colour):
    if colour == 'green':
        return '\x1b[6;30;42m%s\x1b[0m' % text
    elif colour == 'red':
        return '\x1b[6;37;41m%s\x1b[0m' % text

def cert_checker(cert_contents):
    metadata_valid = True

    x509_cert = crypto.load_certificate(crypto.FILETYPE_PEM, cert_contents)

    subject = x509_cert.get_subject().CN

    not_after_date = parser.parse(x509_cert.get_notAfter(), ignoretz=True)
    in_future = (not_after_date > now)

    if not in_future:
        print "%s's not_after_date is not in the future." % subject
        metadata_valid = False  

    certificate_expired = (x509_cert.has_expired() == 1)

    if certificate_expired:
        print "%s's Certificate has expired!." % subject
        metadata_valid = False

    return metadata_valid

def get_cert_chain(country):
    metadata_valid = True

    try:

        base_url = BASE_URL % country
        chain_url = base_url + SIGNING_CERTS_ROOT

        certificate_page = urlopen(chain_url).read()

        certs = findall(FIND_CERT_REGEX, certificate_page)

        for cert in certs:
            unescaped_cert = HTMLParser().unescape(cert) # Remove the HTML Special Charectors.

            cert_status = cert_checker(unescaped_cert)

            if not cert_status:
                metadata_valid = False
    
    except:
        metadata_valid = False

    return metadata_valid

def get_metadata_xml(country):
    metadata_valid = True

    base_url = BASE_URL % country
    metadata_url = base_url + METADATA_ROOT

    try:

        metadata = urlopen(metadata_url).read()

        root = fromstring(metadata) 

        valid_until = root.attrib['validUntil']

        if valid_until is not None:
            parsed_date = parser.parse(valid_until, ignoretz=True)
            valid_until_in_future = (parsed_date > now)
            if not valid_until_in_future:
                print "%s's valid_until is not in the future." % country
                metadata_valid = False


        for certificate in root.findall('.//*/ds:X509Certificate', names_spaces):
            certificate_contents = HEADER + certificate.text + FOOTER

            cert_status = cert_checker(certificate_contents)

            if not cert_status:
                metadata_valid = False
    except:
        metadata_valid = False
    
    return metadata_valid

# Main entry point.
metadata_is_valid_for_all = True

for country in connected_countries.keys():
    country_is_valid = True

    country_name = connected_countries[country]

    if not get_cert_chain(country):
        print colored("Cert chain is not valid for %s" % country_name, 'red')
        metadata_is_valid_for_all = False
        country_is_valid = False

    if not get_metadata_xml(country):
        print colored("Metadata is not valid for %s" % country_name, 'red')
        metadata_is_valid_for_all = False
        country_is_valid = False

    if country_is_valid:
        print colored("Metadata and Cert chain is valid for %s" % country_name, 'green')

if not metadata_is_valid_for_all:
    print "\a" 
    exit(1)