# Project Information

## Web site

The project's public web site is located at [http://smslib.org](http://smslib.org).

## Project structure

SMSLib sources are available at [https://github.com/smslib](https://github.com/smslib).

We use the following git repository workflow: `master` contains the latest stable source, `dev` has the mainline development and other feature branches contain experimental code, etc.

The SMSLib project includes the following subprojects:

* `comm2ip` : The Comm2IP utility: [https://github.com/smslib/comm2ip](https://github.com/smslib/comm2ip)
* `smslib` : The SMSLib library: [https://github.com/smslib/smslib](https://github.com/smslib/smslib)

## How to build Comm2IP

Comm2IP is a .NET/C# application which can be easily built with Visual Studio 2010+.

## How to build SMSLib

For SMSLib, clone the source repository and follow the typical Maven cycles.

The `mvn package` cycle also builds the .NET Framework library. If you work on Linux, remember to edit the main `pom.xml` and remove the `xxx-dotnet` dependencies.

## Maven

If you are using Maven, SMSLib development snapshots artifacts are published on [Sonatype](https://oss.sonatype.prg). The repository URL is [https://oss.sonatype.org/content/repositories/snapshots/org/smslib/](https://oss.sonatype.org/content/repositories/snapshots/org/smslib/)

Snapshot artifacts are **always** named `dev-SNAPSHOT` and are based on the `dev` branch. They are updated whenever there are changes in the `dev` branch.

## News

News are posted on the [SMSLib News](http://smslib.org/news/) page. You can also follow [@smslib](https://twitter.com/smslib).

## Discussion Group

Feel free to join [SMSLib Discussion Group](https://groups.google.com/d/forum/smslib) to ask for help, give feedback or just say hello!

## Issue Tracker

SMSLib tracker is located [here](https://github.com/smslib/smslib/issues).

## Contribute to SMSLib

You are very welcome to contibute to SMSLib!

If you are planning to implement something big (a new feature, for example), please open a ticket describing your idea and how you are planning to work for this. **Patches with hundreds of modified lines coming out of the blue, will probably not be accepted at all!** You can avoid this by opening an enhancement issue in advance, in order to discuss things before you start.

You can use the standard Pull Request functionality or just send in your patch files. **Remember to base your work on the `dev` branch!**

**Remember**: By contributing to SMSLib, **you implicitly accept** that your contribution will be re-distributed with the rest of the SMSLib library and/or related software under the terms of the SMSLib license (Apache v2).
