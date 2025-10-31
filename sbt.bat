@echo off
set JAVA_HOME=C:\Users\wsr\jdk\jdk-17.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
set SBT_HOME=%USERPROFILE%\.sbt
java -Xmx512M -jar "%SBT_HOME%\bin\sbt-launch.jar" %*

