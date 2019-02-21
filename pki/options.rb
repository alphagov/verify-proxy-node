require 'optparse'

USAGE = 'Usage: generate.rb [options] <output directory>'.freeze

Options =
  Struct.new(
    :release_name,
    :hub_entity_id,
    :idp_entity_id,
    :proxy_entity_id,
    :idp_url,
    :proxy_url,
    :connector_url,
    :do_files,
    :do_manifests,
    :do_secrets,
    :do_env,
    :xmlsectool_path,
    :truststore_pass,
  )

class Parser
  def self.parse(args)
    options = Options.new('test',
			  'http://dev-hub.local',
                          'http://stub-idp-demo.local',
                          'http://proxy-node.local',
                          'http://localhost/idp',
                          'http://localhost/proxy',
                          'http://localhost/connector',
                          false,
                          false,
                          false,
                          false,
                          'xmlsectool',
                          'marshmallow',
                          false)

    parser = OptionParser.new do |opts|
      opts.banner = USAGE

      opts.on('--release-name RELEASE', 'Name of proxy node release instance (ie france)') { |s| options.release_name = s }
      opts.on('--hub-entity-id ENTITY_ID', "Hub's entity ID in Hub metadata") { |s| options.hub_entity_id = s }
      opts.on('--idp-entity-id ENTITY_ID', "Stub IDP's entity ID in Hub metadata") { |s| options.idp_entity_id = s }
      opts.on('--proxy-node-entity-id ENTITY_ID', "Proxy Node's entity ID in Hub metadata") { |s| options.proxy_entity_id = s }
      opts.on('--idp-url URL', 'Stub IDP base URL') { |s| options.idp_url = s }
      opts.on('--proxy-url URL', 'Proxy node gateway base URL') { |s| options.proxy_url = s }
      opts.on('--connector-url URL', 'Stub connector base URL') { |s| options.connector_url = s }
      opts.on('--files', 'Set to output keys, certs and truststores') { |_| options.do_files = true }
      opts.on('--manifests', 'Set to output CF manifests with PKI inlined') { |_| options.do_manifests = true }
      opts.on('--secrets', 'Set to output Kubernetes Secrets with PKI inlined') { |_| options.do_secrets = true }
      opts.on('--env', 'Output environment files for Docker Compose') { |_| options.do_env = true }
      opts.on('--xmlsectool PATH', 'Path to xmlsectool (default: xmlsectool)') { |s| options.xmlsectool_path = s }
      opts.on('--truststore-pass PASSWORD', 'Password for generated truststores (default: marshmallow)') { |s| options.truststore_pass = s }
      opts.on('-h', '--help', 'Print help message') { |_| abort(opts.to_s) }
    end

    parser.parse!(args)
    options
  end
end
