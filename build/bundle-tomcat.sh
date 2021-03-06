#!/bin/bash

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

start_tomcat() {
    # start Tomcat up
    "${TOMCAT}/bin/startup.sh" \
        || die "Tomcat failed to startup."
    # wait for Tomcat to be up
    until curl --silent --show-error --connect-timeout 1 -I $MANAGER | grep "Coyote" >/dev/null; do
        echo Waiting for Tomcat to be up...
        sleep 2
    done
}

stop_tomcat() {
    "${TOMCAT}/bin/shutdown.sh" \
        || die "Tomcat failed to shutdown."
}

VERSION="$1"
# the Servlex version number
if [[ -z "${VERSION}" ]]; then
    die "ERROR: The Servlex version number must be passed as first param!";
fi

# the IzPack "compile" script
IZPACK=/Applications/IzPack/bin/compile

# the Tomcat base name (both .tar.gz and dir must have the same name)
TOMCAT_NAME=apache-tomcat-7.0.47

# the dir containing this script
BASEDIR=`dirname $0`
if [[ ! -d "${BASEDIR}" ]]; then
    die "INTERNAL ERROR: The current directory is not a directory?!?";
fi

# the Tomcat dir
TOMCAT=${BASEDIR}/${TOMCAT_NAME}

# untar the archive
rm -r "${TOMCAT}"
( cd "${BASEDIR}"; tar zxf "${TOMCAT_NAME}.tar.gz" )

# creating the repo and the profiling directory
mkdir "${TOMCAT}/repo"
mkdir "${TOMCAT}/repo/.expath-web"
mkdir "${TOMCAT}/profiling"

# empty webapps.xml file
cp "${BASEDIR}/webapps.xml" "${TOMCAT}/repo/.expath-web/"

# deploy the webapp manager
xrepo --repo "${TOMCAT}/repo" install "${BASEDIR}/apps/webapp-manager-0.3.0.xaw"
xrepo --repo "${TOMCAT}/repo" install "${BASEDIR}/apps/expath-http-client-saxon-0.11.0dev.xar"
xrepo --repo "${TOMCAT}/repo" install "${BASEDIR}/apps/expath-zip-saxon-0.7.0pre1.xar"

# adding properties to conf/catalina.properties
PROPS="${TOMCAT}/conf/catalina.properties"
echo >> "${PROPS}"
echo >> "${PROPS}"
echo "# Added by Servlex bundler for Tomcat" >> "${PROPS}"
echo "# " >> "${PROPS}"
echo "# The processors implementation class to use" >> "${PROPS}"
echo 'org.expath.servlex.processors=org.expath.servlex.processors.saxon.SaxonCalabash' >> "${PROPS}"
echo "# The location of the repository" >> "${PROPS}"
echo 'org.expath.servlex.repo.dir=${INSTALL_PATH}/repo' >> "${PROPS}"
echo "# Uncomment to have Calabash generating profiling data" >> "${PROPS}"
echo '# org.expath.servlex.profile.dir=${INSTALL_PATH}/profiling' >> "${PROPS}"
echo "# Uncomment to log (in trace level) the actual content of requests/responses" >> "${PROPS}"
echo "# org.expath.servlex.trace.content=true" >> "${PROPS}"
echo "# Uncomment to set the default charset of requests (if not set in a request)" >> "${PROPS}"
echo "# org.expath.servlex.default.charset=UTF-8" >> "${PROPS}"

# changing the port numbers in conf/server.xml
# TODO: Set URIEncoding="UTF-8" on the connector as well?
( cd "${TOMCAT}"; patch -p0 < ../bundle-tomcat-server.patch )

# setting the users and roles
cp "${BASEDIR}/bundle-tomcat-users.xml" "${TOMCAT}/conf/tomcat-users.xml"

# remove existing webapps
rm -r "${TOMCAT}"/webapps/*

# unzip the WAR into webapps/ROOT
mkdir "${TOMCAT}/webapps/ROOT"
( cd "${TOMCAT}/webapps/ROOT"; unzip ../../../../servlex/dist/servlex.war )

# replace the Servlex version number
perl -e "s|<appversion>([-.0-9a-z]+)</appversion>|<appversion>${VERSION}</appversion>|g;" \
    -pi izpack-tomcat.xml
perl -e "s|servlex-([-.0-9a-z]+)/hello-world/|servlex-${VERSION}/hello-world/|g;" \
    -pi izpack-tomcat.xml
perl -e "s|servlex-([-.0-9a-z]+)/hello-world-([-.0-9a-z]+).xaw|servlex-${VERSION}/hello-world-${VERSION}.xaw|g;" \
    -pi izpack-tomcat.xml
perl -e "s|apache-tomcat-[.0-9]+/|${TOMCAT_NAME}/|g;" \
    -pi izpack-tomcat.xml

# create the installer
# TODO: IzPack outputs a lot of helpless warnings, hope it will be fixed in a future version...
"${IZPACK}" izpack-tomcat.xml -o "servlex-installer-${VERSION}.jar" 2>&1 \
    | grep -v "com.sun.java.util.jar.pack.Utils$Pack200Logger warning"   \
    | grep -v "bytes of LocalVariableTable attribute in"
