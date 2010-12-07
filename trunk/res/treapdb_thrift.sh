JAVA_OPTS="\
 -server\
 -XX:TargetSurvivorRatio=90\
 -XX:+AggressiveOpts\
 -XX:+UseParNewGC\
 -XX:+UseConcMarkSweepGC\
 -XX:+CMSParallelRemarkEnabled\
 -XX:+HeapDumpOnOutOfMemoryError\
 -XX:SurvivorRatio=128\
 -XX:MaxTenuringThreshold=0"


LIBS="\
lib/libthrift.jar:\
lib/libtreap-1.1.jar:\
lib/log4j-1.2.15.jar:\
lib/slf4j-api-1.5.8.jar:\
lib/slf4j-log4j12-1.5.8.jar"

java $JAVA_OPTS -cp $LIBS fx.sunjoy.FastTreapDB 11811 "./data/dbhere" 64 128
