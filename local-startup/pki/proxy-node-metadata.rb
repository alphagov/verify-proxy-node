require 'verify/metadata/generator'
require 'yaml'
require 'nokogiri'

require_relative 'utils'
require_relative 'certs'

def gen_sign_proxy_node_metadata(options, proxy_node_config, xmlsectool_path)
    return sign_metadata_hsm(
                generate_proxy_node_metadata(options.metadata_env, proxy_node_config, read_cert(options.root_ca_cert)),
                read_cert(options.metadata_signing_cert),
                xmlsectool_path)
end

def generate_proxy_node_metadata(metadata_env, proxy_node_config, ca_cert)
  puts('Generating Proxy Node Metadata')
  in_tmp_dir('proxy_node_meta') do
    ca_file = create_file('ca.crt', ca_cert.to_pem)

    Dir.mkdir(metadata_env)
    Dir.chdir(metadata_env) do
      Dir.mkdir('idps')
      Dir.chdir('idps') do
        create_file('proxy-node.yml', YAML.dump(proxy_node_config))
        puts(YAML.dump(proxy_node_config) + "---")
      end
    end

    generated_xml = capture_output do
      Verify::Metadata::Generator.run!(%W(-e #{metadata_env} --valid-until=365 --proxy-node --idpCA ca.crt))
    end

    Nokogiri::XML(generated_xml).tap do |xml|
      extensions = <<~EOS
        <md:Extensions>
          <mdattr:EntityAttributes xmlns:mdattr="urn:oasis:names:tc:SAML:metadata:attribute">
            <saml2:Attribute xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" Name="http://eidas.europa.eu/LoA" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
              <saml2:AttributeValue>http://eidas.europa.eu/LoA/substantial</saml2:AttributeValue>
            </saml2:Attribute>
          </mdattr:EntityAttributes>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#sha384"/>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmlenc#sha512"/>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"/>
        </md:Extensions>
      EOS

      xml.at_xpath("//md:EntityDescriptor//md:IDPSSODescriptor").add_previous_sibling(extensions)
    end
  end
end

#
#   Sign Metadata locally - requires a keypair and an xmlsectool installation
#
def sign_metadata(metadata_xml, keypair, xmlsectool_path = nil)
  puts("Signing metadata locally with cert #{keypair.cert.subject}")
  in_tmp_dir('proxy_node_meta') do |dir|
    cert_file = create_file('metadata.crt', keypair.cert.to_pem)
    key_file = create_file('metadata.key', keypair.key.to_der)
    metadata_file = create_file('metadata.xml', metadata_xml)

    cmd = <<~EOS
    #{xmlsectool_path} \
      --sign \
      --inFile #{dir}/metadata.xml \
      --outFile #{dir}/metadata_signed.xml \
      --certificate #{dir}/metadata.crt \
      --key #{dir}/metadata.key \
      --digest SHA-256
    EOS

    cmd_out = `#{cmd}`
    puts (cmd_out)
    File.open('metadata_signed.xml').read
  end
end

#
#   Sign Metadata with the HSM - requires a signing cert and a PKCS11 installation
#
def sign_metadata_hsm(metadata_xml, signing_cert, xmlsectool_path = nil)
    puts("TODO: Signing metadata using HSM with cert #{signing_cert.subject}")
    return metadata_xml
end
