@set JAVA_OPTS=^
 -server^
 -XX:TargetSurvivorRatio=90^
 -XX:+AggressiveOpts^
 -XX:+UseParNewGC^
 -XX:+UseConcMarkSweepGC^
 -XX:+CMSParallelRemarkEnabled^
 -XX:+HeapDumpOnOutOfMemoryError^
 -XX:SurvivorRatio=128^
 -XX:MaxTenuringThreshold=0
rem echo %JAVA_OPTS%
@set LIBS=^
lib\libthrift.jar;^
lib\libtreap-1.0.jar;^
lib\log4j-1.2.15.jar;^
lib\slf4j-api-1.5.8.jar;^
lib\slf4j-log4j12-1.5.8.jar

java %JAVA_OPTS% -cp bin;%LIBS% fx.sunjoy.TreapDB 11811 "./data/dbhere" 64 128
