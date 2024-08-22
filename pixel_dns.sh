#!/bin/sh
# run.sh
# Script to run the DNS server Java project

set -e # Exit early if any commands fail

# Ensure the IP and port are properly set or passed
resolver_ip="127.0.0.1"  # Example IP address
resolver_port="2053"     # Example port

# Run the Java application with arguments
java -jar target/build-your-own-dns-1.0.jar "$resolver_ip:$resolver_port"
