
Pod::Spec.new do |s|
  s.name         = "RNReactNativeSettings"
  s.version      = "1.0.0"
  s.summary      = "RNReactNativeSettings"
  s.description  = <<-DESC
                  RNReactNativeSettings
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNReactNativeSettings.git", :tag => "master" }
  s.source_files  = "RNReactNativeSettings/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  