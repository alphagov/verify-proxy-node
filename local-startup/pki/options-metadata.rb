require 'optparse'

USAGE = 'Usage: generate-proxy-node-metadata.rb [options] <output directory>'.freeze

LOCAL_PKI_DIR = '../.local_pki/'

MetadataOptions =
  Struct.new(
    :proxy_entity_id,
    :proxy_url,
    :metadata_env,
    :root_ca_cert,
    :metadata_signing_cert,
    :proxy_signing_cert,
    :xmlsectool_path,
    :filename
  )

class MetadataOptionsParser
  def self.parse(args)
    options =
      MetadataOptions.new(
        'http://proxy-node.local',
        'http://localhost/proxy',
        'dev',
        File.join(LOCAL_PKI_DIR, 'root_ca.crt'),
        File.join(LOCAL_PKI_DIR, 'proxy_node_metadata_signing.crt'),
        File.join(LOCAL_PKI_DIR, 'proxy_node_signing.crt'),
        'xmlsectool',
        'metadata.xml'
      )

    parser = OptionParser.new do |opts|
      opts.banner = USAGE

      opts.on('--proxy-node-entity-id ENTITY_ID', "Proxy Node's entity ID in Hub metadata") { |s| options.proxy_entity_id = s }
      opts.on('--proxy-url URL', 'Proxy node gateway base URL') { |s| options.proxy_url = s }
      opts.on('--metadata_env ENV', 'Environment for the Metadata') { |s| options.metadata_env = s }
      opts.on('--root_ca_cert CERT', 'Path to Root CA Cert') { |s| options.root_ca_cert = s }
      opts.on('--metadata_signing_cert CERT', 'Path to Metadata Signing Cert') { |s| options.metadata_signing_cert = s }
      opts.on('--proxy_signing_cert CERT', 'Path to Proxy Signing Cert') { |s| options.proxy_signing_cert = s }
      opts.on('--xmlsectool PATH', 'Path to xmlsectool (default: xmlsectool)') { |s| options.xmlsectool_path = s }
      opts.on('--filename NAME', 'Filename for metadata file (default: metadata.xml)') { |s| options.filename = s }
      opts.on('-h', '--help', 'Print help message') { |_| abort(opts.to_s) }
    end

    parser.parse!(args)
    options
  end
end
