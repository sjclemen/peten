#!/bin/bash
for a in $(ls jface-jars/); do
	export GROUPID_64=$(echo $a |sed -e 's/_64_.*/_64/');
	export GROUPID=$(if [[ $(echo "$GROUPID_64" |sed -e 's/.*\.//') == "jar" ]] ; then echo $GROUPID_64 |sed -e 's/_.*//'; else echo $GROUPID_64; fi);
	export ARTIFACTID=$(echo $GROUPID |sed -e 's/.*\.//');
	export VERSION=$(echo $a |sed -e 's/.*_//' -e 's/\.jar$//');
#	mvn install:install-file -Dfile=jface-jars/$a -DgroupId=$GROUPID -DartifactId=$ARTIFACTID -Dversion=$VERSION -Dpackaging=jar -DlocalRepositoryPath=./jface-repository/;
	echo "	<dependency>"
	echo "		<groupId>$GROUPID</groupId>"
	echo "		<artifactId>$ARTIFACTID</artifactId>"
	echo "		<version>$VERSION</version>"
	echo "	</dependency>"
done
