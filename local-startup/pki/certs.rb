require 'openssl'

class KeyPair
  attr_reader :key, :cert

  def initialize(key, cert)
    @key = key
    @cert = cert
  end
end

def create_key
  OpenSSL::PKey::RSA.new(2048)
end

def create_certificate(key, subject)
  OpenSSL::X509::Certificate.new.tap do |cert|
    cert.subject = OpenSSL::X509::Name.parse(subject)
    cert.not_before = Time.now
    cert.not_after = Time.now + 365 * 24 * 60 * 60
    cert.public_key = key.public_key
    cert.version = 2
  end
end

def ca_certificate(cert, issuer = nil)
  cert.serial = 1
  cert.issuer = issuer.nil? ? cert.subject : issuer.subject

  ef = OpenSSL::X509::ExtensionFactory.new
  ef.subject_certificate = cert
  ef.issuer_certificate = issuer || cert

  cert.add_extension(ef.create_extension('basicConstraints', 'CA:TRUE', true))
  cert.add_extension(ef.create_extension('keyUsage', 'keyCertSign, cRLSign', true))
  cert.add_extension(ef.create_extension('subjectKeyIdentifier', 'hash', false))
  cert.add_extension(ef.create_extension('authorityKeyIdentifier', 'keyid:always', false))
end

def issue_certificate(cert, issuer, usage)
  cert.serial = 2
  cert.issuer = issuer.subject

  ef = OpenSSL::X509::ExtensionFactory.new
  ef.subject_certificate = cert
  ef.issuer_certificate = issuer

  cert.add_extension(ef.create_extension('keyUsage', usage, true))
  cert.add_extension(ef.create_extension('subjectKeyIdentifier', 'hash', false))
end

def sign_certificate(cert, key)
  cert.sign(key, OpenSSL::Digest::SHA256.new)
end

#   Read cert from file, DER- or PEM-encoded
def read_cert(certPath)
    return OpenSSL::X509::Certificate.new File.read certPath
end

def strip_pem(pem)
  pem.gsub(/-----(BEGIN|END) CERTIFICATE-----/, '').gsub("\n", '')
end

USAGE_SIGNING = 'digitalSignature'
USAGE_ENCRYPTION = 'keyEncipherment'
