# SMSServer

## Introduction

SMSServer is a small application that you can use for sending and receiving SMS messages without writing your own code. SMSServer is basically driven through a database, so you can send/receive SMS messages via insert/reading rows into database tables.

SMSServer supports all SMSLib [Gateways](https://github.com/smslib/smslib/wiki/GatewayFeatureMap).

## Quick Start Guide

### SMSServer Application

SMSServer is built with the normal `pom` SMSLib files. It is built with the normal `mvn package` command which build the entire SMSLib sources. It's distributed as a separate `.jar` file, named:

* `smsserver-dep-VERSION-all.jar` which is a standalone `jar` file containing all dependencies.
* `smsserver-dotnet-dev-SNAPSHOT.exe` which is a respective .NET generated file.

### Database

SMSServer *requires* a database to work. It's tested with MySQL and Microsoft SQL Server, but it can easily work with other RDBMS as well, as long as JDBC drivers are available.

The latest database schema definition file is located in the `/misc/smsserver/database` folder. Make sure the database is created and available before you proceed!

### Running SMSServer

SMSServer can be execute like this (assuming you are located in the SMSLib root distribution directory):

```
java -jar lib\smsserver-dep-dev-SNAPSHOT-all.jar -url jdbc:mysql://localhost/smslib?autoReconnect=true -driver com.mysql.jdbc.Driver -username root -password root
```

For the .NET version:

```
lib\smsserver-dotnet-dev-SNAPSHOT.exe -url jdbc:mysql://localhost/smslib?autoReconnect=true -driver com.mysql.jdbc.Driver -username root -password root
```

**Note**: For the .NET version to run properly, all IKVM .dll files should be available in the same folder where the SMSServer executable is located!

### Stopping SMSServer

Press Ctrl-C or send a break signal to the application.

### Monitoring SMSServer

You can use the standard SMSLib monitoring page which is by default available at: http://localhost:8001/status

More information about this specific monitoring page can be found in [General Configuration](https://github.com/smslib/smslib/wiki/GeneralConfiguration).