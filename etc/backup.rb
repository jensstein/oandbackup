#!/usr/bin/ruby

# a dirty ruby script to backup packages via adb from a PC with Neo-Backup compatible format
# you probably need to know ruby to use it

# first, you need to install
#   ruby
# then use
#   gem install ruby_apk
# (if ruby doesn't include the gem command, you proably need to install rubygems or similar)

# use it like
#   ruby backup.rb a.package.name another.package.name
# or for all packages with apks
#   ruby backup.rb

puts "running #{$PROGRAM_NAME} on #{RUBY_ENGINE} #{RUBY_VERSION}"

require 'optparse'
require 'open3'
include Open3
require 'fileutils'
include FileUtils
require 'json'
require 'rexml/document'

# non standard (please install each with: gem install xxx)
require 'ruby_apk'

#---------------------------------------- config

if ARGV.empty?
  # ARGV = ['--help']
  # ARGV = ['-p']       # doesn't work, because ruby_apk is too old or something
  # ARGV = ['-g']
  ARGV = ['-m']
  # ARGV = ['com.machiav3lli.backup.hg42.debug']
  # ARGV = ['com.whatsapp']
  # ARGV = ['com.machiav3lli.backup.hg42', 'com.whatsapp']
end

NB_compatible = true    # keep this true, it does not make sense to change this
BACKUP_all = false
BACKUP_clear = false    # clear the whole backup directory before backup
BACKUP_only_first = false # for testing
BACKUP_version = 8003
BACKUP_profile = 0      # only 0 working in recovery
DIR_backup = "./backup"
DIR_data_system = "/data/system"
DIR_data_system_user = "/data/system/users/#{BACKUP_profile}"
DIR_apps = "/data/app"
DIR_data = "/data/data" # recoveries of my test devices all have an empty /data/user/#{BACKUP_profile} in adb, change if not
DIR_dedata = "/data/user_de/#{BACKUP_profile}" # but this is not empty
DIR_extdata = "/sdcard/Android/data"
DIR_obb = "/sdcard/Android/obb"
DIR_media = "/sdcard/Android/media"
ARCH_name_data = "data"
ARCH_name_dedata = "device_protected_files"
ARCH_name_extdata = "external_files"
ARCH_name_obb = "obb_files"
ARCH_name_media = "media_files"
COMPRESS = NB_compatible ? "gz" : "bz2"

OPT = {
  backup: true,
  perm: false,
  ssaid: false,
  compress: COMPRESS,
  missing: false,
  clear: false,
}

#---------------------------------------- options

OptionParser.new do |o|
  o.banner = "Usage: #{$PROGRAM_NAME} [options] [a.package.name ...]"

  # o.on('-v', '--[no-]verbose') { |v| OPT[:verbose] = v }

  o.on('-p', '--[no-]perm') { |v| OPT[:perm] = v }      # does not work

  o.on('-c', '--clear')     { |v| OPT[:clear] = v }     # clear the backup directory !!!

  o.on('-m', '--missing')   { |v| OPT[:missing] = v }   # only backup missing packages, to continue a canceled backup

  # o.on('-g', '--[no-]gui') { |v| OPT[:gui] = v }

  o.on('-b', '--backup', '(default)') do |v|            # restore doesn't exist anyways...
    OPT[:backup] = v
    OPT[:restore] = !v
  end

  # o.on('-r', '--restore') do |v|                      # in case someone wants to implement it
  #   OPT[:restore] = v
  #   OPT[:backup] = !v
  # end

  o.on('-h', '--help', 'Prints this help') do
    puts o
    exit
  end
end.parse!

p OPT
p ARGV

#---------------------------------------- derived

COMPRESSION = {
  'gz' => %w[-z .gz],             # only one compatible with NB
  #'zst' => %w[-T .zst],             # option does not exist in toybox tar
  #'bz2' => %w[-j .bz2],           # option exist in toybox tar, but probably does not work (missing bzip2)
  #'xz' => %w[-J .xz],               # option exist in toybox tar, but probably does not work (missing xz)
}
COMPRESS_tarOpt, COMPRESS_ext = COMPRESSION[COMPRESS]

RE_ignore_package = %r{
    ^android$
}x

BACKUP_clear = OPT[:clear]

#---------------------------------------- classes

class Apks
  attr_accessor :count, :nbytes, :version_code, :version_name, :package_label
end

class Infos
  attr_accessor :perms, :ssaid, :optimized
end

#---------------------------------------- remotes

class Remote
  def lines(command)
    run(command) { |o| o.readlines.map(&:chomp) }
  end

  def line(command)
    lines(command)[0]
  end

  def check(command)
    run(command).exitstatus.zero?
  end

  def show(command)
    run(command) { |o| o.each_line { |line| puts line } }
  end
end

class RemoteAdb < Remote
  def initialize
    super
    @adb = ['adb', 'shell']
    @adb = check('[ $(whoami) == root ]') ? ['adb', 'shell'] : ['adb', 'shell', 'su']
  end

  def run(command, &block)
    popen2(*@adb) do |i, o, w|
      i.write('set -x ;')
      i.write(command)
      i.close
      if block_given?
        block.call(o)
      else
        w.value
      end
    end
  end
end

#---------------------------------------- remote

def packages(remote)
  remote.lines("cd '#{DIR_data}/' ; ls -1").reject do |package|
    RE_ignore_package.match(package)
  end
end

def app_dirs(remote)
  remote.lines("find '#{DIR_apps}' -name \*.apk")
        .map { |package| package.sub(%r{/[^/]+$}, '') }
        .sort
        .uniq
end

def packages_xml(remote)
  remote.run("cat '#{DIR_data_system}/packages.xml'") do |o|
    #REXML::Document.new(o)
    axml = o.read
    #pp axml
    parser = Android::AXMLParser.new(axml)
    pp parser
    xml = parser.parse
    pp xml
    exit
  end
end

def ssaid_xml(remote)
  remote.run("cat '#{DIR_data_system_user}/settings_ssaid.xml'") do |o|
    #REXML::Document.new(o)
    Android::AXMLParser.new(o.read).parse
  end
end

def idle_xml(remote)
  remote.run("cat '#{DIR_data_system}/deviceidle.xml'") do |o|
    #REXML::Document.new(o)
    Android::AXMLParser.new(o.read).parse
  end
end

String.class_eval do
  def with_apk
    name = self
    APP_DIRS.find { |d| d.include?("/#{name}-") }
  end
end

#---------------------------------------- operations

def backup_app(remote, profile, package, dir)
  from = APP_DIRS.find { |it| it.include?("/#{package}-") }
  return 0, 0, nil, nil, nil unless from

  makedirs(dir)
  apks = remote.lines("find '#{from}' -type f -name \*.apk")
  count = 0
  nbytes = 0
  info = Apks.new
  apks.each do |a|
    to = "#{dir}/#{a.sub(%r{^.*/}, '')}"
    File.open(to, 'w') do |f|
      remote.run("cat '#{a}'") do |o|
        nbytes += IO.copy_stream(o, f)
        count += 1
        begin
          apk = Android::Apk.new(to)
          manifest = apk.manifest
          info.version_name = manifest.version_name
          if info.version_name
            info.version_code = manifest.version_code
            label = manifest.label
            info.package_label = label&.encode('UTF-8', 'UTF-8', invalid: :replace)&.unicode_normalize(:nfkc)
            if info.version_name && info.version_code
              puts "version: #{info.version_name} (#{info.version_code}) '#{info.package_label}'"
              break
            end
          end
        rescue StandardError => e
          puts e
        end
      end
    end
  end
  if count == apks.size
    puts "backup app  #{nbytes} bytes in #{count} apks"
  else
    puts "backup app  FAILED #{nbytes} bytes, only copied #{count} apks of #{apks.size}"
  end
  info.count = count
  info.nbytes = nbytes
  info
end

def backup_generic(name, remote, package, dir, from, archive)
  return 0 unless remote.check("test -d #{from}")

  makedirs(dir)
  nbytes = 0
  File.open(archive, 'w') do |f|
    remote.run("tar -c #{COMPRESS_tarOpt} -C #{from} .") do |o|
      nbytes = IO.copy_stream(o, f)
    end
  end
  puts "backup #{package} #{name} #{nbytes} bytes"
  nbytes
end

def backup_data(remote, profile, package, dir)
  backup_generic(
    'data', remote, package, dir,
    "'#{DIR_data}/#{package}'",
    "#{dir}/#{ARCH_name_data}.tar#{COMPRESS_ext}"
  )
end

def backup_dedata(remote, profile, package, dir)
  backup_generic(
    'dedata', remote, package, dir,
    "'#{DIR_dedata}/#{package}'",
    "#{dir}/#{ARCH_name_dedata}.tar#{COMPRESS_ext}"
  )
end

def backup_extdata(remote, profile, package, dir)
  backup_generic(
    'extdata', remote, package, dir,
    "'#{DIR_extdata}/#{package}'",
    "#{dir}/#{ARCH_name_extdata}.tar#{COMPRESS_ext}"
  )
end

def backup_obb(remote, profile, package, dir)
  backup_generic(
    'obb', remote, package, dir,
    "'#{DIR_obb}/#{package}'",
    "#{dir}/#{ARCH_name_obb}.tar#{COMPRESS_ext}"
  )
end

def backup_media(remote, profile, package, dir)
  backup_generic(
    'media', remote, package, dir,
    "'#{DIR_media}/#{package}'",
    "#{dir}/#{ARCH_name_media}.tar#{COMPRESS_ext}"
  )
end

IGNORED_PERMISSIONS = [
  "android.permission.ACCESS_WIFI_STATE",
  "android.permission.ACCESS_NETWORK_STATE",
  "android.permission.CHANGE_WIFI_MULTICAST_STATE",
  "android.permission.FOREGROUND_SERVICE",
  "android.permission.INSTALL_SHORTCUT",
  "android.permission.INTERNET",
  "android.permission.QUERY_ALL_PACKAGES",
  "android.permission.REQUEST_DELETE_PACKAGES",
  "android.permission.RECEIVE_BOOT_COMPLETED",
  "android.permission.READ_SYNC_SETTINGS",
  "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
  "android.permission.USE_FINGERPRINT",
  "android.permission.WAKE_LOCK",
].select { |n| n != nil }

def backup_infos(_remote, profile, package)
  infos = Infos.new

  perms = []

  if OPT[:perm]

    perms = PACKAGES_XML.elements
                        .to_a('//permissions/item')
                        .to_h { |n| [n.attributes['name'], n.attributes['protection']] }
                        .sort
    #pp perms

    allowed =
      PACKAGES_XML.elements
                  .to_a('//permissions/item')
                  .select { |n| n.attributes['protection'] }
                  .select { |n| n.attributes['protection'].to_i < 1000 }
                  .map { |n| n.attributes['name'] }
                  .sort
      .select { |n| n.start_with?('android.permission.') }
    #pp allowed

    infos.perms =
      PACKAGES_XML.elements
                  .to_a("//package[@name='#{package}']/perms/item[@granted='true']")
                  .map { |n| n.attributes['name'] }
                  .select { |n| allowed.include?(n) }
    # .sort

  end

  if OPT[:ssaid]

    infos.ssaid =
      SSAID_XML.elements["//setting[@package='#{package}']"]&.attributes&.[]('value')

  end

  if OPT[:idle]

    infos.optimized =
      IDLE_XML.elements
              .to_a("//wl[@n='#{package}']")
              .empty?

  end

  infos
end

def backup(remote, profile, package)
  now = Time.now
  dir = "#{DIR_backup}/#{package}@#{now.strftime('%Y-%m-%d-%H-%M-%S-%L')}-user_#{profile}"
  properties = "#{dir}.properties"

  puts

  if OPT[:missing] && Dir.glob("#{DIR_backup}/#{package}@*-user_#{profile}").length > 0
    puts "---------- [#{profile}] #{package} already saved"
    return
  end

  makedirs(dir)

  puts "---------- [#{profile}] #{package}"

  begin
    remove_dir(dir)
  rescue StandardError
    nil
  end

  nbytes = 0
  begin
    apks = backup_app(remote, profile, package, dir)
    nbytes += apks.nbytes
  rescue StandardError => e
    puts "FAILED to backup app: #{e}"
  end

  return unless apks.count || BACKUP_all

  begin
    infos = backup_infos(remote, profile, package)
  rescue StandardError => e
    puts "FAILED to backup infos: #{e}"
  end

  begin
    size_data = backup_data(remote, profile, package, dir)
    nbytes += size_data
  rescue StandardError => e
    puts "FAILED to backup data: #{e}"
  end

  begin
    size_dedata = backup_dedata(remote, profile, package, dir)
    nbytes += size_dedata
  rescue StandardError => e
    puts "FAILED to backup dedata: #{e}"
  end

  begin
    size_extdata = backup_extdata(remote, profile, package, dir)
    nbytes += size_extdata
  rescue StandardError => e
    puts "FAILED to backup extdata: #{e}"
  end

  begin
    size_obb = backup_obb(remote, profile, package, dir)
    nbytes += size_obb
  rescue StandardError => e
    puts "FAILED to backup obb: #{e}"
  end

  begin
    size_media = backup_media(remote, profile, package, dir)
    nbytes += size_media
  rescue StandardError => e
    puts "FAILED to backup media: #{e}"
  end

  props =
    {
      backupVersionCode: BACKUP_version,
      backupDate: now.strftime('%Y-%m-%dT%H:%M:%S.%L'),
      profileId: profile,
      cpuArch: BACKUP_cpuArch,
      packageName: package,
      packageLabel: apks.package_label || package,
      versionName: apks.version_name || '0.0.0',
      versionCode: apks.version_code || 0,
      hasApk: apks.count > 0,
      hasAppData: size_data > 0,
      hasDevicesProtectedData: size_dedata > 0,
      hasExternalData: size_extdata > 0,
      hasObbData: size_obb > 0,
      hasMediaData: size_media > 0,
      # iv: [],
      permissions: infos.perms,
      size: nbytes
    }

  unless NB_compatible
    props[:optimized] = infos.optimized if infos.optimized
    props[:ssaid] = infos.ssaid if infos.ssaid
  end

  json = JSON.pretty_generate(props)

  puts json

  File.open(properties, 'w') { |f| f.write(json) }

  BACKUP_only_first && exit
end

def backup_packages(remote, profile, packages)
  packages.each do |package|
    backup(remote, profile, package)
  end
end

#---------------------------------------- main

if BACKUP_clear
  begin
    remove_dir(DIR_backup)
  rescue StandardError
    nil
  end
end

makedirs(DIR_backup)

remote = RemoteAdb.new

BACKUP_cpuArch = remote.line('uname -m') || 'aarch64'

PACKAGES = packages(remote)
APP_DIRS = app_dirs(remote)
if OPT[:perm]
  PACKAGES_XML = packages_xml(remote)
  #pp PACKAGES_XML
end
if OPT[:ssaid]
  SSAID_XML = ssaid_xml(remote)
end
if OPT[:idle]
  IDLE_XML = idle_xml(remote)
end

packages = ARGV
packages = PACKAGES.select(&:with_apk) if packages.empty?

if OPT[:backup]

  backup_packages(remote, BACKUP_profile, packages)

end
