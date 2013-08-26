#!/usr/bin/expect -f

# Author: Chris Koeritz

# Given an installer to run, this will try to drive it as if a user was
# supplying all the console inputs.

if { $argc < 8 } {
  puts "This script will install a Genesis II installer and feed it the appropriate"
  puts "lines of text on the console as if a user was running it.  However, we need"
  puts "several command-line arguments.  These are:"
  puts "\t(1) the path to the installer."
  puts "\t(2) path to install the grid code to."
  puts "\t(3) a normal grid account to use to obtain a certificate."
  puts "\t(4) the port to run the container on."
  puts "\t(5) the grid to connect to: 1=xcg, 2=xsede."
  puts "\t(6) the hostname of the container."
  puts "\t(7) the path to the keypair to be used for login."
  puts "\t(8) the password for the keypair."
  exit 1
}

set installer_app_path [lrange $argv 0 0]
set path_for_install [lrange $argv 1 1]
set normal_account [lrange $argv 2 2 ]
set different_port [lrange $argv 4 4 ]
set grid_choice [lrange $argv 5 5 ]
set container_hostname [lrange $argv 6 6]
set keypair_path [lrange $argv 7 7 ]
set keypair_password [lrange $argv 8 8]

puts "we were passed these parameters:"
puts "installer_app_path $installer_app_path"
puts "path_for_install $path_for_install"
puts "normal_account $normal_account"
puts "container port $different_port"
puts "grid choice $grid_choice"
puts "container host $container_hostname"
puts "keypair path $keypair_path"
puts "keypair_password OMITTED"

spawn bash $installer_app_path -c

expect -r "will install.*computer.*OK.*Cancel"
send "\r"

expect -r "Where should.*be installed?"
send "$path_for_install\r"

expect -r "Deployment.*XSEDE"
send "$grid_choice\r"

expect -r ".*Port Number"
send "$different_port\r"

expect -r "Specify.*commun.*container.*Name"
send "$container_hostname\r"

expect -r "user.*manage the container.*Name"
send "$normal_account\r"

expect -r "service will generate.*Keypair.*No"
send "n\r"

expect -r "path.*keypair.*Path"
send "$keypair_path\r"

expect -r "Keystore Password"
send "$keypair_password\r"

expect -r "Alias"
send "xsede cert"
#hmmm: will have to adjust that for generality!

## used to be needed when we were not using certificate.
## # IMPORTANT: caller has to make sure there is no DISPLAY variable or the
## # installer will try to launch gui password.
## expect -r "Password for.*"
## send "$normal_password\r"

expect -r "Start.*Service.*Enter"
send "y\r"

# look for those startup messages.
expect -r "Starting.*Service"

# now container should be running, and we can do rest of regression test...
puts "All steps concluded successfully.  Installer should be running."

exit 0


