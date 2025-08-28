#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'status_bar_control_plus'
  s.version          = '3.3.0'
  s.summary          = 'Status Bar Control Plus'
  s.description      = <<-DESC
Status Bar Control Plus
                       DESC
  s.homepage         = 'https://github.com/Gulshank61/statusbar_control_plus'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Gulshan K.' => 'gulshankhandale61@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  
  s.ios.deployment_target = '15.0'
end

