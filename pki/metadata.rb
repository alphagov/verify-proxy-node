require 'verify/metadata/generator'
require 'yaml'

require_relative 'utils'

def generate_hub_metadata(hub_config, idp_configs, ca_cert)
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

def generate_proxy_node_metadata(proxy_node_config, ca_cert)
  in_tmp_dir('proxy_node_meta') do
    ca_file = create_file('ca.crt', ca_cert.to_pem)

    Dir.mkdir('dev')
    Dir.chdir('dev') do
      Dir.mkdir('idps')
      Dir.chdir('idps') do
        create_file('proxy-node.yml', YAML.dump(proxy_node_config))
      end
    end

    capture_output do
      Verify::Metadata::Generator.run!(%w(-e dev --valid-until=365 --proxy-node --idpCA ca.crt))
    end
  end
end

def sign_metadata(metadata_xml, keypair)
  in_tmp_dir('proxy_node_meta') do
    cert_file = create_file('metadata.crt', keypair.cert.to_pem)
    key_file = create_file('metadata.pk8', keypair.key.to_der)
    metadata_file = create_file('metadata.xml', metadata_xml)

    cmd = %(xmlsectool \
      --sign \
      --inFile metadata.xml \
      --outFile metadata_signed.xml \
      --certificate metadata.crt \
      --key metadata.key \
      --digest SHA-256
    )
    cmd_out = %x(#{cmd})
    File.open('metadata_signed.xml').read
  end
end
