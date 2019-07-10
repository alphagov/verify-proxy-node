require 'keystores'

def create_truststore(path, password, certs)
  ks = OpenSSL::JKS.new
  certs.each do |name, cert|
    puts("Adding cert #{cert.subject} to truststore #{path}")
    ks.set_certificate_entry(name, cert)
  end
  ks.store(path, password)
end
