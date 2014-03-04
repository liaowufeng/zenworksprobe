#!/bin/sh

PROBE_Version=11.3.0
directory=`pwd`;
PROBE_dir=$directory/probe
PROBE_BUILD=$PROBE_dir/build
PROBE_WAR_DIR=$directory/../web/target
if [ -d $PROBE_dir ]
	then
		rm -rf $PROBE_dir
fi
mkdir $PROBE_dir
mkdir -p $PROBE_RPM

mkdir -p $PROBE_BUILD/opt/novell/zenworks/share/tomcat/webapps
destdir_war=$PROBE_BUILD/opt/novell/zenworks/share/tomcat/webapps/


echo " Copying the files"
cp  $PROBE_WAR_DIR/zenworks-probe.war $destdir_war/zenworks-probe.war

echo "Creating the spec file "
echo "%define _prefix /opt/novell/zenworks" >$PROBE_dir/novell-zenworks-probe.spec
echo "Name : novell-zenworks-probe" >>$PROBE_dir/novell-zenworks-probe.spec
echo "Version : ${PROBE_Version}"  >>$PROBE_dir/novell-zenworks-probe.spec
echo "Release : 0"        >>$PROBE_dir/novell-zenworks-probe.spec
echo "Group : Applications/System" >>$PROBE_dir/novell-zenworks-probe.spec
echo "License : LGPL"   >>$PROBE_dir/novell-zenworks-probe.spec
echo "Vendor : Novell, Inc." >>$PROBE_dir/novell-zenworks-probe.spec
echo "URL : http://www.novell.com/products" >>$PROBE_dir/novell-zenworks-probe.spec
echo "Packager : Novell, Inc." >>$PROBE_dir/novell-zenworks-probe.spec
echo "AutoReqProv : no" >>$PROBE_dir/novell-zenworks-probe.spec
echo "BuildRoot : $PROBE_BUILD" >>$PROBE_dir/novell-zenworks-probe.spec
echo "BuildArch : noarch" >>$PROBE_dir/novell-zenworks-probe.spec
echo "Summary : Novell ZENworks Probe" >>$PROBE_dir/novell-zenworks-probe.spec
echo "%description" >>$PROBE_dir/novell-zenworks-probe.spec
echo "%files
%defattr(-, root, root)

%{_prefix}/share/tomcat/webapps" >>$PROBE_dir/novell-zenworks-probe.spec
rpmbuild -ba $PROBE_dir/novell-zenworks-probe.spec
if [ $? -eq 0 ]
	then

echo "successfully created"
fi
