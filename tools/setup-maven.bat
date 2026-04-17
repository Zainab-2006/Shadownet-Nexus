@echo off
REM Download and setup portable Maven
if exist "apache-maven-3.9.9" goto :verify
echo Downloading Maven 3.9.9...
powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' -OutFile 'maven.zip'"
powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.' -Force"
del maven.zip
:verify
echo Maven ready at tools/apache-maven-3.9.9
tools\maven.bat -version

