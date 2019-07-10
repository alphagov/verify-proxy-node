require 'verify/metadata/generator'
require 'yaml'
require 'nokogiri'

require_relative 'utils'

def generate_hub_metadata(hub_config, idp_configs, ca_cert)
  puts('Generating Hub Metadata')
  in_tmp_dir('proxy_node_meta') do
    ca_file = create_file('ca.crt', ca_cert.to_pem)

    Dir.mkdir('dev')
    Dir.chdir('dev') do
      create_file('hub.yml', YAML.dump(hub_config))
      Dir.mkdir('idps')
      Dir.chdir('idps') do
        idp_configs.each_with_index do |cfg, i|
          create_file("idp_#{i}.yml", YAML.dump(cfg))
        end
      end
    end

    capture_output do
      Verify::Metadata::Generator.run!(%w(-e dev --valid-until=365 --hubCA ca.crt --idpCA ca.crt))
    end
  end
end

