@echo off
echo Setting up Shadownet Nexus environment...

REM Use embedded Maven
set MAVEN_HOME=c:/Users/zain/Downloads/shadownet-nexus/apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;node;%PATH%

REM Set JAVA_HOME for Temurin 17
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM Assume Windows Java in PATH or portable
echo Using system Java/Maven for dev
echo Maven: 
call mvn --version
echo NPM: 
npm --version

echo Setup complete! (JAVA_HOME optional for dev)

