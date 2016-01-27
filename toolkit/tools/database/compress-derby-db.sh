#!/bin/bash

##########################################################################
#  Author: Vanamala Venkataswamy

# This script can be used to compress GenesisII Container Database. 
# Specifically Queue container Dabatase as it tends to grow over time 
# and take up large amount of disk space.
##########################################################################

if [ -z "$DERBY_HOME" -o -z "$GENII_USER_DIR" ]; then
  echo "This script requires two variables in the environment, the user state"
  echo "directory (GENII_USER_DIR) and the Derby installation directory"
  echo "(DERBY_HOME)."
  exit 1
else
  export PATH=$DERBY_HOME/bin:$PATH
  export CLASSPATH=$DERBY_HOME/lib/derby.jar:$DERBY_HOME/lib/derbytools.jar:.
fi

java org.apache.derby.tools.sysinfo >/dev/null

response=""
if [ $? == 0 ]
then
  echo "Compressing DB now... This could take a long time."
  sed -e "s%GENII_USER_DIR%${GENII_USER_DIR}%g" compressfull.db > compressfull.db.temp
  response=`java org.apache.derby.tools.ij compressfull.db.temp`
  #response=`java -Dij.outfile=results.txt org.apache.derby.tools.ij compressfull.db.temp` #UNCOMMENT this if you do not want o/p on terminal
  rm compressfull.db.temp
  if echo $response | grep -q "Another instance of Derby may have already booted the database"
  then
    echo "Please stop the container before running the script...Exiting"
    exit
  elif echo $response | grep -q "Unable to establish connection"
  then
    echo "Failed to connect to $GENII_USER_DIR/derby-db database...Exiting"
    exit
  elif echo $response | grep -q "call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('SA', 'WSNSUBSCRIPTIONS', 0); 0 rows inserted/updated/deleted"
  then
    echo "Finished compressing DB. You can now start the container"
    exit
  fi
else
  echo "Something went wrong, please check the DERBY_HOME settings."
fi

exit
