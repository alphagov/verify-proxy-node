#!/usr/bin/env ruby

require_relative 'certs'
require_relative 'proxy-node-metadata'
require_relative 'utils'
require_relative 'options-metadata'

options = MetadataOptionsParser.parse(ARGV)
output_dir = ARGV.pop || abort(USAGE)

xmlsectool_path = options.xmlsectool_path || ENV.fetch('XMLSECTOOL', 'xmlsectool')

# Generate Proxy Node Metadata
proxy_node_config = {
  'id' => '_entities',
  'entity_id' => options.proxy_entity_id,
  'sso_uri' => "#{options.proxy_url}/SAML2/SSO/POST",
  'organization' => {
    'name' => 'eIDAS Service',
    'display_name' => 'eIDAS Service',
    'url' => options.proxy_url
  },
  'signing_certificates' => [
    { 'x509' => strip_pem(read_cert(options.proxy_signing_cert).to_pem) }
  ],
  'enabled' => true
}
proxy_node_metadata_xml_signed =
    gen_sign_proxy_node_metadata(
        options,
        proxy_node_config,
        xmlsectool_path)

# Output
Dir.mkdir(output_dir) unless Dir.exist?(output_dir)
Dir.chdir(output_dir) do create_file(options.filename, proxy_node_metadata_xml_signed) end
