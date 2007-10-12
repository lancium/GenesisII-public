@ECHO OFF

java -Xms32M -Xmx128M -classpath "C:\Program Files\Genesis II\ext\bouncycastle\bcprov-jdk15-133.jar;C:\Program Files\Genesis II\lib\GenesisII-security.jar;C:\Program Files\Genesis II\lib\morgan-utilities.jar;C:\Program Files\Genesis II\lib;C:\Program Files\Genesis II\security" -Dlog4j.configuration=genesisII.log4j.properties "-Djava.library.path=C:\Program Files\Genesis II\jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=C:\Program Files\Genesis II" org.morgan.util.launcher.Launcher "C:\Program Files\Genesis II\jar-desc.xml" %*
