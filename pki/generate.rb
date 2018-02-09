require 'yaml'

require_relative 'certs'
require_relative 'metadata'
require_relative 'truststores'
require_relative 'utils'

if ARGV.size < 3
  abort('Usage: generate.rb proxy-node-manifest.yml env_var_key output_dir')
end

puts <<-EOS
🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺  🇪🇺🇪🇺    🇪🇺🇪🇺 🇪🇺🇪🇺🇪🇺🇪🇺     🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺    🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺  
🇪🇺🇪🇺     🇪🇺🇪🇺 🇪🇺🇪🇺   🇪🇺🇪🇺   🇪🇺🇪🇺     🇪🇺🇪🇺    🇪🇺🇪🇺  🇪🇺🇪🇺     🇪🇺🇪🇺 
🇪🇺🇪🇺     🇪🇺🇪🇺 🇪🇺🇪🇺  🇪🇺🇪🇺    🇪🇺🇪🇺     🇪🇺🇪🇺        🇪🇺🇪🇺     🇪🇺🇪🇺 
🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺  🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺     🇪🇺🇪🇺     🇪🇺🇪🇺   🇪🇺🇪🇺🇪🇺🇪🇺 🇪🇺🇪🇺     🇪🇺🇪🇺 
🇪🇺🇪🇺        🇪🇺🇪🇺  🇪🇺🇪🇺    🇪🇺🇪🇺     🇪🇺🇪🇺    🇪🇺🇪🇺  🇪🇺🇪🇺     🇪🇺🇪🇺 
🇪🇺🇪🇺        🇪🇺🇪🇺   🇪🇺🇪🇺   🇪🇺🇪🇺     🇪🇺🇪🇺    🇪🇺🇪🇺  🇪🇺🇪🇺     🇪🇺🇪🇺 
🇪🇺🇪🇺        🇪🇺🇪🇺    🇪🇺🇪🇺 🇪🇺🇪🇺🇪🇺🇪🇺     🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺    🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺🇪🇺  


EOS

def sub(common_name)
  "/C=UK/O=Verify/OU=Notification/CN=#{common_name}"
end

def selfsigned_keypair(cn)
  puts("Generating self-signed CA cert - #{sub(cn)}")
  ss_key = create_key
  ss_cert = create_certificate(ss_key, sub(cn)).tap do |cert|
    ca_certificate(cert)
    sign_certificate(cert, ss_key)
  end
  KeyPair.new(ss_key, ss_cert)
end

def sub_keypair(issuer_keypair, cn, key_usage)
  puts("Issuing cert - #{sub(cn)} from #{issuer_keypair.cert.subject}")
  sub_key = create_key
  sub_cert = create_certificate(sub_key, sub(cn)).tap do |cert|
    issue_certificate(cert, issuer_keypair.cert, key_usage)
    sign_certificate(cert, issuer_keypair.key)
  end
  KeyPair.new(sub_key, sub_cert)
end

def strip_pem(pem)
  pem.gsub(/-----(BEGIN|END) CERTIFICATE-----/, '').gsub("\n", '')
end

def der2pk8(fn, out)
  puts("Converting #{fn} (DER) to #{out} (PK8)")
  `openssl pkcs8 -in #{fn} -inform DER -topk8 -outform DER -out #{out} -nocrypt`
end

def s2i(s)
  Integer(s)
rescue ArgumentError
  s
end

def traverse_hash(h, path)
  path.split('.').reduce do |init, key|
    h = init.nil? ? h : h[s2i(init)]
    h = h[s2i(key)]
    nil
  end
  h
end

# Fetch environment variables
manifest = YAML.load_file(ARGV[0])
env = traverse_hash(manifest, ARGV[1])
puts('Using environment variables:')
puts(env.map { |k,v| " - #{k}=#{v}" }.join("\n"))

# Root CA
root_keypair = selfsigned_keypair('Root CA')

# Verify Hub Metadata Signing
hub_meta_keypair = sub_keypair(root_keypair, 'Hub Metadata Signing', USAGE_SIGNING)

# eIDAS Proxy Metadata Signing
proxy_node_meta_keypair = sub_keypair(root_keypair, 'Proxy Node Metadata Signing', USAGE_SIGNING)

# Hub Signing
hub_signing_keypair = sub_keypair(root_keypair, 'Hub Signing', USAGE_SIGNING)

# Hub Encryption
hub_encryption_keypair = sub_keypair(root_keypair, 'Hub Encryption', USAGE_ENCRYPTION)

# Stub IDP Signing
idp_signing_keypair = sub_keypair(root_keypair, 'IDP Signing', USAGE_SIGNING)

# Proxy Node Signing
proxy_signing_keypair = sub_keypair(root_keypair, 'Proxy Node Signing', USAGE_SIGNING)

# Generate Hub Metadata
hub_config = {
  'id' => 'VERIFY-HUB',
  'entity_id' => env.fetch('HUB_ENTITY_ID'),
  'assertion_consumer_service_uri' => env.fetch('PROXY_NODE_HUB_RESPONSE_URL'),
  'organization' => { 'name' => 'Hub', 'display_name' => 'Hub', 'url' => 'http://localhost' },
  'signing_certificates' => [
    { 'name' => 'hub_signing', 'x509' => strip_pem(hub_signing_keypair.cert.to_pem) }
  ],
  'encryption_certificate' => { 'name' => 'hub_encryption', 'x509' => strip_pem(hub_encryption_keypair.cert.to_pem) }
}
stub_idp_config = {
  'id' => 'stub-idp-demo',
  'entity_id' => 'http://stub_idp.acme.org/stub-idp-demo/SSO/POST',
  'sso_uri' => env.fetch('HUB_URL'),
  'organization' => { 'name' => 'stub-idp-demo', 'display_name' => 'Stub IDP', 'url' => 'http://localhost' },
  'signing_certificates' => [
    { 'x509' => strip_pem(idp_signing_keypair.cert.to_pem) }
  ],
  'enabled' => true
}
hub_metadata_xml = generate_hub_metadata(hub_config, [stub_idp_config], root_keypair.cert)
hub_metadata_xml_signed = sign_metadata(hub_metadata_xml, hub_meta_keypair)

# Generate Proxy Node Metadata
proxy_node_config = {
  'id' => '_entities',
  'entity_id' => '_verify_proxy_node',
  'sso_uri' => env.fetch('PROXY_NODE_AUTHN_REQUEST_URL'),
  'organization' => { 'name' => 'eIDAS Service', 'display_name' => 'eIDAS Service', 'url' => 'https://eidas-service.eu' },
  'signing_certificates' => [
    { 'x509' => strip_pem(proxy_signing_keypair.cert.to_pem) }
  ],
  'enabled' => true
}
proxy_node_metadata_xml = generate_proxy_node_metadata(proxy_node_config, root_keypair.cert)
proxy_node_metadata_xml_signed = sign_metadata(proxy_node_metadata_xml, proxy_node_meta_keypair)

# Output
output_dir = ARGV[2]
Dir.mkdir(output_dir) unless Dir.exist?(output_dir)
Dir.chdir(output_dir) do
  create_truststore('ida_metadata_truststore.ts', 'marshmallow', {'root_ca' => root_keypair.cert})

  create_file('root_ca.crt', root_keypair.cert.to_pem)
  create_file('hub_metadata_signing.crt', hub_meta_keypair.cert.to_pem)
  create_file('proxy_node_metadata_signing.crt', proxy_node_meta_keypair.cert.to_pem)

  create_file('hub_signing.crt', hub_signing_keypair.cert.to_pem)
  create_file('hub_signing.der', hub_signing_keypair.key.to_der)
  create_file('hub_encryption.crt', hub_encryption_keypair.cert.to_pem)
  create_file('hub_encryption.der', hub_encryption_keypair.key.to_der)
  create_file('stub_idp_signing.crt', idp_signing_keypair.cert.to_pem)
  create_file('stub_idp_signing.der', idp_signing_keypair.key.to_der)
  create_file('proxy_node_signing.crt', proxy_signing_keypair.cert.to_pem)
  create_file('proxy_node_signing.der', proxy_signing_keypair.key.to_der)
  create_file('metadata_for_hub.xml', hub_metadata_xml_signed)
  create_file('metadata_for_connector_node.xml', proxy_node_metadata_xml_signed)

  der2pk8('hub_signing.der', 'hub_signing.pk8')
  der2pk8('hub_encryption.der', 'hub_encryption.pk8')
  der2pk8('stub_idp_signing.der', 'stub_idp_signing.pk8')
  der2pk8('proxy_node_signing.der', 'proxy_node_signing.pk8')
end

