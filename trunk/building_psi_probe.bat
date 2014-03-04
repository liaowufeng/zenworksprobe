SET SHARE_DRIVE=Z:
SET INSTALL_BASE_DIR=%SHARE_DRIVE%\zcminstallbase\ZCM_11.3.0
SET BUILD_PATH="/data/zcminstallbase/ZCM_11.3.0"
SET PSI_BUILD_PATH=%SHARE_DRIVE%\zcminstallbase\ZCM_11.3.0\PSI_PROBE
SET NEW_BUILD_SYSTEM="buildmgr@164.99.177.53"
SET NEW_BUILD_REL_PATH="/builds/ZCM/Feature_branches/CET_11.3"
SET NEW_BUILD_PATH="buildmgr@164.99.177.53:/builds/ZCM/Feature_branches/CET_11.3/PSI_PROBE"
SET SSH_KEY="C:\Documents and Settings\buildmgr\ssh\pvtkey.ppk"

echo "Cleaning the maven repository for the build"
rd /S /Q "C:\\Users\\buildmgr\\.m2"

SET CONNECTION_STRING="buildmgr@164.99.177.85"

call mvn install:install-file -Dfile=C:\psi_probe\lib\ojdbc14.jar -DpomFile=C:\psi_probe\ojdbc14-pom.xml

rem call E:\psi_probe\setcurrentdate.bat

cd C:\psi_probe

call mvn package

SET PSI_BUILD=C:\psi_probe\web\target

move %PSI_BUILD%\probe.war %PSI_BUILD%\zenworks-probe.war

cd C:\psi_probe\build

call buildProbeMsi.bat

cd %PSI_BUILD%

xcopy /Y C:\psi_probe\build\novell-zenworks-probe*.msi %INSTALL_BASE_DIR%\PSI_PROBE

rem echo "Transfering the jar to 164.99.177.53 build system"

xcopy /Y C:\psi_probe\web\target\zenworks-probe.war %INSTALL_BASE_DIR%\PSI_PROBE

rem plink %CONNECTION_STRING% -i %SSH_KEY% scp %BUILD_PATH%/PSI_PROBE/zenworks-probe.war %NEW_BUILD_PATH%






