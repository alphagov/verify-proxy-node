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

def generate_proxy_node_metadata(proxy_node_config, ca_cert)
  puts('Generating Proxy Node Metadata')
  in_tmp_dir('proxy_node_meta') do
    ca_file = create_file('ca.crt', ca_cert.to_pem)

    Dir.mkdir('dev')
    Dir.chdir('dev') do
      Dir.mkdir('idps')
      Dir.chdir('idps') do
        create_file('proxy-node.yml', YAML.dump(proxy_node_config))
      end
    end

    generated_xml = capture_output do
      Verify::Metadata::Generator.run!(%w(-e dev --valid-until=365 --proxy-node --idpCA ca.crt))
    end

    Nokogiri::XML(generated_xml).tap do |xml|
      extensions = <<~EOS
        <md:Extensions>
          <mdattr:EntityAttributes xmlns:mdattr="urn:oasis:names:tc:SAML:metadata:attribute">
            <saml2:Attribute xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" Name="http://eidas.europa.eu/LoA" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
              <saml2:AttributeValue>http://eidas.europa.eu/LoA/substantial</saml2:AttributeValue>
            </saml2:Attribute>
          </mdattr:EntityAttributes>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#sha384"/>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmlenc#sha512"/>
          <alg:DigestMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1"/>
          <alg:SigningMethod xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport" Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"/>
        </md:Extensions>
      EOS

      xml.at_xpath("//md:EntityDescriptor//md:IDPSSODescriptor").add_previous_sibling(extensions)
    end
  end
end

def sign_metadata(metadata_xml, keypair, xmlsectool_path = 'xmlsectool')
  puts("Signing metadata with cert #{keypair.cert.subject}")
  in_tmp_dir('proxy_node_meta') do
    cert_file = create_file('metadata.crt', keypair.cert.to_pem)
    key_file = create_file('metadata.key', keypair.key.to_der)
    metadata_file = create_file('metadata.xml', metadata_xml)

    cmd = <<~EOS
    #{xmlsectool_path} \
      --sign \
      --inFile metadata.xml \
      --outFile metadata_signed.xml \
      --certificate metadata.crt \
      --key metadata.key \
      --digest SHA-256
    EOS

    cmd_out = `#{cmd}`
    puts (cmd_out)
    File.open('metadata_signed.xml').read
  end
end
