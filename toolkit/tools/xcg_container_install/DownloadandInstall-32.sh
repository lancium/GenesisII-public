#!/bin/bash

str1=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-6});

str2=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-6});

name1="name1.tmp"
touch $name1 
echo $str1 >> $name1
echo $str1 >> $name1
echo "xcgmain" >> $name1

adduser xcgmain < $name1

if [ -d /home/xcgmain ];then

      echo "go on"

name2="name2.tmp"
touch $name2 
echo $str2 >> $name2
echo $str2 >> $name2
echo "xcgjob" >> $name2

adduser xcgjob <$name2

wget http://www.cs.virginia.edu/~vcgr/xcg3install/script2-32.sh

mv script2-32.sh /home/xcgmain

chmod +x /home/xcgmain/script2-32.sh

HOME=/home/xcgmain sudo -u xcgmain bash /home/xcgmain/script2-32.sh


else 

       echo "Please execute the file using sudo!"
       exit
fi





