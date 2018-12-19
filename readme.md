# Redirect Detector
This application reads domains from the database schema and makes HTTP and HTTPS calls to the domain to get

1. Whether the domain is up, responding
2. Whether the domain redirects

The times, status codes, and redirects, if any, are written to the database.

# Installation

This application is built using Maven and you must have Java and Maven installed. Java 8 is the minimum required.
First get the dependency of [domainentry](https://github.com/crazyguyonabike/domainentry.git) and install it:

`$M2_HOME/bin/mvn clean install`

Then build this application:

`$M2_HOME/bin/mvn clean compile`

Set up a MySQL or MariaDB database and fill in the database name, database user and database password in redirect.properties.template
then copy redirect.properties.template to redirect.properties

Create the schema

```
CREATE TABLE `domain_entry` (
  `id` bigint(20) NOT NULL,
  `domain` varchar(128) DEFAULT NULL,
  `category` varchar(32) DEFAULT NULL,
  `working` datetime DEFAULT NULL,
  `http_last_time` datetime DEFAULT NULL,
  `https_last_time` datetime DEFAULT NULL,
  `http_redirect` varchar(128) DEFAULT NULL,
  `https_redirect` varchar(128) DEFAULT NULL,
  `http_status` int(11) DEFAULT NULL,
  `https_status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`,`category`),
) ENGINE=InnoDB
```

Then you can run the application with:

`$M2_HOME/bin/mvn exec:exec`

