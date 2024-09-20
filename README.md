[![Version](https://img.shields.io/maven-central/v/org.wildfly.clustering/wildfly-clustering?style=for-the-badge&logo=redhat&logoColor=ee0000&label=javadoc)](https://javadoc.io/doc/org.wildfly.clustering/wildfly-clustering)
[![License](https://img.shields.io/github/license/wildfly-clustering/wildfly-clustering?style=for-the-badge&color=darkgreen&logo=apache&logoColor=d22128)](https://www.apache.org/licenses/LICENSE-2.0)
[![Project Chat](https://img.shields.io/badge/zulip-chat-lightblue.svg?style=for-the-badge&logo=zulip&logoColor=ffffff)](https://wildfly.zulipchat.com/#narrow/stream/wildfly-clustering)

# WildFly Clustering

WildFly clustering is a set of modules providing distributed services to application servers and applications.

This project serves as upstream to the following projects:
* [WildFly](https://github.com/wildfly/wildfly)
* Tomcat via [wildfly-clustering-tomcat](https://github.com/wildfly-clustering/wildfly-clustering-tomcat)
* Spring via [wildfly-clustering-spring](https://github.com/wildfly-clustering/wildfly-clustering-spring)

## Building

### Prerequisites

Building this project requires the following software:

* JDK 11+
* Maven 3.9+

Additionally, the integration tests contained in this project require a Docker-API compatible container runtime.

See: https://java.testcontainers.org/supported_docker_environment/

#### Using rootless Podman

For those using the latest version of Podman, you should be able to run the remote infinispan integration tests without root permissions.

See: https://github.com/containers/podman/blob/main/docs/tutorials/rootless_tutorial.md

### Build instructions

1.	Clone this repository.

		$ git clone git@github.com:wildfly-clustering/wildfly-clustering.git
		$ cd wildfly-clustering

1.	Build via maven.

		$ mvn clean install

#### Integration test execution options

Since the integration tests take some time to execute, you can skip integration test execution via:

		$ mvn clean install -DskipITs

By default, the remote Infinispan integration tests launch docker using "bridge" network mode.
If running a Linux distribution, and encounter issues with connectivity between you test client and the Infinispan server instance running in the container, try using "host" network mode via:

		$ mvn clean install -Ddocker.network.mode=host

By default, remote Infinispan integration tests will use the Infinispan server docker image published at `quay.io` corresponding to the `${version.org.infinispan}` version configured by this project's pom.
You can override this to use an arbitrary Infinispan server docker image and user via system properties.
e.g.

		$ mvn clean install -Dinfinispan.server.image=quay.io/infinispan/server:14.0 -Dinfinispan.server.username=foo -Dinfinispan.server.password=bar
