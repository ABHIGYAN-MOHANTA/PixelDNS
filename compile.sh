#!/bin/sh
# compile.sh
# Script to compile the DNS server Java project with a specified Java version

set -e # Exit early if any commands fail

# Ensure the compile steps are run within the repository directory
cd "$(dirname "$0")"

# Check if a Java version argument is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <java-version>"
    exit 1
fi

JAVA_VERSION="$1"

# Validate Java version argument (simple numeric check)
if ! echo "$JAVA_VERSION" | grep -E '^[0-9]+$' > /dev/null; then
    echo "Invalid Java version format. Please provide a numeric version."
    exit 1
fi

# Update the pom.xml with the specified Java version
POM_FILE="pom.xml"
TEMP_POM_FILE="pom.xml.tmp"

# Update the Java version in the pom.xml
awk -v java_version="$JAVA_VERSION" '
    /<maven.compiler.source>/ { print "<maven.compiler.source>" java_version "</maven.compiler.source>"; next }
    /<maven.compiler.target>/ { print "<maven.compiler.target>" java_version "</maven.compiler.target>"; next }
    /<java.version>/ { print "<java.version>" java_version "</java.version>"; next }
    { print }
' "$POM_FILE" > "$TEMP_POM_FILE" && mv "$TEMP_POM_FILE" "$POM_FILE"

# Compile the Java project with Maven and package it into a JAR file
mvn -B package -Ddir=/tmp/dns-server-java
