require 'optparse'

USAGE = 'Usage: generate.rb [options] <output directory>'

Options = Struct.new(:hub_entity_id, :idp_entity_id, :proxy_entity_id, :hub_response_url, :idp_sso_url, :proxy_sso_url, :do_files, :do_manifests)

class Parser
  def self.parse(args)
    options = Options.new('http://dev-hub.local',
                          'http://stub-idp-demo.local',
                          'http://proxy-node.local',
                          'http://localhost/hub/SAML2/SSO/Response',
                          'http://localhost/idp/SAML2/SSO/POST',
                          'http://localhost/proxy/SAML2/SSO/POST',
                          false,
                          false
                         )

    parser = OptionParser.new do |opts|
      opts.banner = USAGE

      opts.on('--hub-entity-id ENTITY_ID', "Hub's entity ID in Hub metadata") { |s| options.hub_entity_id = s }
      opts.on('--idp-entity-id ENTITY_ID', "Stub IDP's entity ID in Hub metadata") { |s| options.idp_entity_id = s }
      opts.on('--proxy-node-entity-id ENTITY_ID', "Proxy Node's entity ID in Hub metadata") { |s| options.proxy_entity_id = s }
      opts.on('--hub-response-url RESPONSE_URL', "URL to post Hub response to") { |s| options.hub_response_url = s }
      opts.on('--idp-sso-url SSO_URL', "URL to post Hub AuthnRequest to Stub IDP") { |s| options.idp_sso_url = s }
      opts.on('--proxy-sso-url SSO_URL', "URL to post eIDAS AuthnRequest to Proxy Node") { |s| options.proxy_sso_url = s }
      opts.on('--files', "Set to output keys, certs and truststores") { |_| options.do_files = true }
      opts.on('--manifests', "Set to output CF manifests with PKI inlined") { |_| options.do_manifests = true }
      opts.on('-h', '--help', 'Print help message') { |_| abort(opts.to_s) }
    end

    parser.parse!(args)
    return options
  end
end
