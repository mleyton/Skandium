require 'bundler/setup'
require 'buildr-eclipse-launch'

COPYRIGHT = 'Copyright 2011, NIC Labs, Universidad de Chile'

THIS_VERSION = "1.0.0"

repositories.remote << 'http://repo1.maven.org/maven2/'

#TESTING = "org.mockito:mockito-all:jar:1.8.5"

desc 'Skandium'
define 'skandium' do

  project.version = THIS_VERSION
  project.group = 'cl.niclabs.skandium'

  desc 'Skandium Core'
  define 'core' do


    package(:jar).with(:manifest=>{ 'Copyright' => COPYRIGHT })
    package(:javadoc)
    package(:sources)

  end

  desc 'Examples'
  define 'examples' do
    compile.with project('core')
    package(:jar).with(:manifest=>{ 'Copyright' => COPYRIGHT })
    package(:javadoc)
    package(:sources)
  end

end

