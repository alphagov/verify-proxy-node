require 'keystores'

def create_truststore(path, password, certs)
  ks = OpenSSL::JKS.new
  certs.each { |name, cert| ks.set_certificate_entry(name, cert) }
  ks.store(path, password)
end
