#!/bin/sh

echo "removing of .m2 directory"
rm -rf /home/buildmgr/.m2

PROBE_HOME=`pwd`
cd $PROBE_HOME

mvn install:install-file -Dfile=$PROBE_HOME/lib/ojdbc14.jar -DpomFile=$PROBE_HOME/ojdbc14-pom.xml

cd $PROBE_HOME

mvn package

PSI_BUILD=$PROBE_HOME/web/target

mv $PSI_BUILD/probe.war	$PSI_BUILD/zenworks-probe.war

cd $PROBE_HOME/build

./buildProbeRpm.sh

cp  /usr/src/packages/RPMS/noarch/novell-zenworks-probe*.rpm /continuum/zcminstallbase/ZCM_11.3.0/PSI_PROBE
