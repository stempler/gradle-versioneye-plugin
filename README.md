gradle-versioneye-plugin
========================

Plugin for [Gradle](http://www.gradle.org/) to update your project dependencies status on [VersionEye](https://www.versioneye.com) based on the resolved dependency configurations of your Gradle project.

It works quite similar to the [VersionEye plugin for Maven](https://github.com/versioneye/versioneye_maven_plugin).

[![Dependency Status](https://www.versioneye.com/user/projects/53511f70fe0d0774a8000b25/badge.png)](https://www.versioneye.com/user/projects/53511f70fe0d0774a8000b25)

Usage
-----

The plugin is not yet released to Maven Central, so you need to build it locally using `gradlew install` and add it as a dependency to your Gradle project's build script like this:

```groovy
buildscript {
	repositories {
		mavenLocal()
	}
	dependencies {
		classpath 'org.standardout:gradle-versioneye-plugin:0.1-SNAPSHOT'
	}
}

apply plugin: 'versioneye'
```

*gradle-versioneye-plugin* has been tested with Gradle 1.11.

### API key

You need to provide your [VersionEye](https://www.versioneye.com) API key for the plugin to be able to communicate with the VersionEye API. You do this through a property, e.g. by specifying it **gradle.properties** in **~/.gradle/** or the project directory, or via the command line. However, it is strongly recommended not to place it somewhere where it is publicly accessible (e.g. in a public GitHub repository).

```
versioneye.api_key=1234567890abcdef
```

### Tasks

The **versioneye** plugin comes with two Gradle tasks that are relevant for you:

* ***versioneye-create*** - Create a project on [VersionEye](https://www.versioneye.com) and write the project key and identifier to your project's **gradle.properties** (so they can be used with ***versioneye-update***)
* ***versioneye-update*** - Update the dependencies for the project on [VersionEye](https://www.versioneye.com) that is identified by the project key and your API key
 
### Project configuration

Once you create a VersionEye project with ***versioneye-create***, it will add the `versioneye.projectkey` and `versioneye.projectid` properties to the **gradle.properties** file in your project directory. But you can also provide these settings manually in any way Gradle supports specifying properties (e.g. if you already have an existing VersionEye project).
