# microservice_spec.rb
#
require_relative './spec_helper.rb'

describe file('/opt/vehicles-online-1.0-SNAPSHOT/conf/vehiclesOnline.conf') do
  its(:content) { should include 'ordnancesurvey'  }
 end

upstart_services = [ 'vehicles-online' ]

upstart_services.each do |item|
  describe command("initctl status #{item}") do
    its(:stdout) {should include "start/running"}
  end
end


sysV_services = [ 'monit' ]

sysV_services.each do |item|
  describe service(item) do
    it { should be_running }
  end
end


ports = [ 9000 ]

ports.each do |item|
  describe command('netstat -anl | grep LISTEN | grep -v ING') do
    its(:stdout) {should include "#{item}" }
  end
end



