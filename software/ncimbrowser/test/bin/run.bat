@echo off
rem -------------------------------------------------------------------------------------
setlocal enabledelayedexpansion
set ocp=%CLASSPATH%

cd ..
set cp=.
set cp=%cp%;..\conf
set cp=%cp%;classes
for %%x in (..\lib\*.jar lib\*.jar) do (
  set cp=!cp!;%%x
)
set CLASSPATH=%cp%

set java=%JAVA_HOME%\bin\java
set class=gov.nih.nci.evs.browser.utils.test.Main
set args=

rem -------------------------------------------------------------------------------------
@echo on
"%java%" %class% %args%

@echo off
rem -------------------------------------------------------------------------------------
set CLASSPATH=%ocp%
@echo on
