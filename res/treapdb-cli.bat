if "%1" EQU ""  (java -cp lib/libtreap-2.0.jar fx.sunjoy.client.TreapDBShellClient localhost 11811) else (java -cp lib/libtreap-2.0.jar fx.sunjoy.client.TreapDBShellClient %1 %2)

