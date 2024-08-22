# PixelDNS

PixelDNS is a fully functional DNS server built from scratch in Java, inspired by the Codecrafters "Build Your Own DNS Server" guide. This project aims to provide a deeper understanding of DNS protocols and server implementation by creating a custom DNS server from the ground up.

## Features

- **Custom DNS Server**: A DNS server implemented in Java, capable of handling DNS queries and responses.
- **Question and Answer Handling**: Supports parsing and responding to DNS queries with appropriate DNS answers.
- **Configurable**: Easy to configure and run with different DNS resolver IPs and ports.
- **Built with Maven**: Managed and built using Maven for dependency management and packaging.

## Prerequisites

Before you start, ensure you have the following installed:

- **Java JDK 17** or the version you prefer.
- **Maven** for building the project.

## Installation

1. **Clone the Repository**:

    ```sh
    git clone https://github.com/ABHIGYAN-MOHANTA/PixelDNS
    cd PixelDNS
    ```

2. **Compile the Project**:

   By default, the project is set to use Java 17. To compile with a different Java version, use:

    ```sh
    chmod +x ./compile.sh
    ./compile.sh <java-version>
    ```

   Replace `<java-version>` with the desired version, e.g., `22`, `17`.

## Usage

1. **Run the DNS Server**:

   To start the DNS server, use the `run.sh` script and provide the resolver IP and port as arguments:

    ```sh
    chmod +x ./pixel_dns.sh
    ./pixel_dns.sh
    ```

   This command will start the DNS server, listening on port `2053` and forwarding queries to the specified resolver.

## Configuration

The server listens for DNS queries on port `2053` by default. You can change this port in the `Main.java` and `pixel_dns.sh` file if needed.

## Troubleshooting

- **Java Version Issues**: Ensure that the Java version specified in `pom.xml` and the version you use to run the project match.
- **Exceptions**: Check for exceptions and error messages in the terminal to debug issues related to DNS queries or network problems.

## Contributing

Feel free to fork the repository and submit pull requests with improvements or bug fixes. Contributions are welcome!

## Acknowledgments

- **Codecrafters**: For the insightful guide on building a DNS server.
- **Maven**: For making dependency management and build automation seamless.
