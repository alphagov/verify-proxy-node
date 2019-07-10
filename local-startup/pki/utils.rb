require 'tempfile'

def create_file(fn, s)
  File.new(fn, 'w').tap do |f|
    f.write(s)
    f.flush
  end
end

def in_tmp_dir(dn)
  Dir.mktmpdir(dn) do |dir|
    puts("-- in_tmp_dir #{dir}")
    Dir.chdir(dir) { yield(dir) }
  end
end

def capture_output
  cap = StringIO.new
  $stdout = cap
  yield
  $stdout.flush
  $stdout = STDOUT
  cap.string
end


