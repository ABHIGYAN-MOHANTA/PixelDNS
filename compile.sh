#!/bin/sh
# compile.sh
# Script to compile the DNS server Java project

set -e # Exit early if any commands fail

# Ensure the compile steps are run within the repository directory
cd "$(dirname "$0")"

# Compile the Java project with Maven and package it into a JAR file
mvn -B package -Ddir=/tmp/dns-server-java
