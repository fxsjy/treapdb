JAVA_OPTS="\
 -server\
 -Xmx1G\
 -XX:+HeapDumpOnOutOfMemoryError\
 -XX:+UseParNewGC\
 -XX:+UseConcMarkSweepGC\
 -XX:+CMSParallelRemarkEnabled\
 -XX:SurvivorRatio=8\
 -XX:MaxTenuringThreshold=1\
 -XX:CMSInitiatingOccupancyFraction=75\
 -XX:+UseCMSInitiatingOccupancyOnly
"

LIBS="\
lib/libthrift.jar:\
lib/libtreap-2.0.jar:\
lib/log4j-1.2.15.jar:\
lib/slf4j-api-1.5.8.jar:\
lib/slf4j-log4j12-1.5.8.jar"

java $JAVA_OPTS -cp $LIBS fx.sunjoy.TreapDB conf/TreapDBConf_Slave.xml
