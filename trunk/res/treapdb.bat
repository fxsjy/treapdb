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
java %JAVA_OPTS% -cp bin fx.sunjoy.TreapDB 11811 "./data/treapdb" 64 128
