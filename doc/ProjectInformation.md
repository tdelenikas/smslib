# Project Information

## Project structure

SMSLib sources are located [https://github.com/smslib](https://github.com/smslib).

We use the following git repository workflow: `master` contains the latest stable source, `dev` has the mainline development and other feature branches contain experimental code, etc.

The SMSLib project includes the following subprojects:

* `comm2ip` : The Comm2IP utility: [https://github.com/smslib/comm2ip](https://github.com/smslib/comm2ip).
* `smslib` : The SMSLib library: [https://github.com/smslib/smslib](https://github.com/smslib/smslib).

## How to build Comm2IP

Comm2IP is a .NET/C# application which can be easily built with Visual Studio 2010+.

## How to build SMSLib

For SMSLib, clone the source repository and follow the typical Maven cycles.

The `mvn package` cycle also builds the .NET Framework library. If you work on Linux, remember to edit the main `pom.xml` and remove the `xxx-dotnet` dependencies.

## Prebuilt binaries

Prebuilt binaries are published on the [SMSLib web site](http://smslib.org).

## Maven

If you are using Maven, SMSLib provices a private maven repository. Add the following dependency to your project:

```
<dependency>
   <groupId>org.smslib</groupId>
   <artifactId>smslib</artifactId>
   <version>dev-SNAPSHOT</version>
</dependency>
```

Don't forget to add the private SMSLib repository as well!

```
<repositories>
   ...
   <repository>
      <id>smslib-snapshots</id>
      <name>SMSLib Repository</name>
      <url>http://smslib.org/maven2/snapshots/</url>
   </repository>
   ...
</repositories>
```

## Discussion Group

Feel free to join [SMSLib Discussion Group](https://groups.google.com/d/forum/smslib) to ask for help, give feedback or just say hello!

## Issue Tracker

SMSLib tracker is located [here](https://github.com/smslib/smslib/issues).

## Contribute to SMSLib

You are very welcome to contibute to SMSLib!

If you are planning to implement something big (a new feature, for example), please open a ticket describing your idea and how you are planning to work for this. **Patches with hundreds of modified lines coming out of the blue, will probably not be accepted at all!** You can avoid this by opening an enhancement issue in advance, in order to discuss things before you start.

You can use the standard Pull Request functionality or just send in your patch files. **Remember to base your work on the `dev` branch!**

**Remember**: By contributing to SMSLib, **you implicitly accept** that your contribution will be re-distributed with the rest of the SMSLib library and/or related software under the terms of the SMSLib license (Apache v2).
