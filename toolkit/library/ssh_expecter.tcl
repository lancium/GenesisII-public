#!/usr/bin/expect -f

# this command takes at least 4 parameters:
#   ssh_expecter user passwd host cmd1 [ cmd2 cmd3 ... ]
# it will connect to the host specified, using the user and password, and will
# try to run each of the commands (cmd1, cmd2, etc).
# no error checking is done per command.

if { $argc < 4 } {
  puts "This script will run a collection of commands on a remote host through ssh."
  puts "It requires at least 4 command line arguments:"
  puts "\t(1) The user to log in as."
  puts "\t(2) The password for that user."
  puts "\t(3) The hostname to log in on with ssh."
  puts "\t(4-...) Commands to run on the remote host."
  exit 1
}

# timeout is predefined and by default is only 10 seconds.
set timeout 60 

# get the command line parameters loaded in.
set user [lrange $argv 0 0]
set password [lrange $argv 1 1]
set machine [lrange $argv 2 2]
set commands [lrange $argv 3 [llength $argv] ]

#debugging...
#set len [llength $argv]
#for { set i 3 } { $i < $len} {incr i} {
#  puts "argv\[$i\] => [lrange $argv $i $i ]"
#}
#puts "commands got: $commands"

##############

# we count up errors in this variable.
set errors 0

# login via ssh as the user on the machine they gave us.
spawn ssh $user@$machine

# look for the login prompt and part of the noise afterwards.
while {1} {
  expect {
    eof                          {set errors {$errors + 1} ; break}
    "The authenticity of host"   {send "yes\r"}
    "password:"                  {send "$password\r"}
    "Last login:*from"           {break}
  }
}
if {$errors > 0} { puts "==> Saw an error during login."; exit 1 }

# pause to allow other messages to spew.
sleep 1

puts "==> Got logged on to: $machine"

# we want to see all output.
#send "stty echo\r"

set len [llength $commands]
for { set i 0 } { $i < $len} {incr i} {
  # make sure we see a CR before we start sending.
  set curr_cmd "[lindex $commands $i]"
  puts "==> Sending command: $curr_cmd"
  # send the command, but put a failure noise into the log if it doesn't succeed.
  send -- "$curr_cmd || echo Y'O'_FAILURE\r"
  # we use a sentinel to know when the command is done, and we cannot have the form
  # that might get echoed when the command is submitted actually match the form
  # that will be echoed, or will just match our own command submission.
  send -- "echo -e __CMD'-'FINISHED__\r"
  # make sure we see the final result flag too.
  expect {
    "__CMD-FINISHED__\r" { }
    eof { set errors {$errors + 1} ; puts "==> bailing out of command loop due to eof"; break; }
  }
  sleep 1
}
# pause a bit before we come back.
sleep 4
send "exit\r"

# close the handle created by the spawn command.
close $spawn_id

# don't try to return a number larger than we can; just signal an error.
if { $errors > 0 } { puts "Saw $errors errors during this run."; exit 1 }
exit 0


