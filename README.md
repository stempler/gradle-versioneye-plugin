gradle-versioneye-plugin
========================

Plugin for [Gradle](http://www.gradle.org/) to update your project dependencies status on [VersionEye](https://www.versioneye.com) based on the resolved dependency configurations of your Gradle project.

It works quite similar to the [VersionEye plugin for Maven](https://github.com/versioneye/versioneye_maven_plugin).

[![Dependency Status](https://www.versioneye.com/java/org.standardout:gradle-versioneye-plugin/badge.svg)](https://www.versioneye.com/java/org.standardout:gradle-versioneye-plugin)

Usage
-----

The simplest way to apply the plugin to your Gradle build is using the latest release hosted on Maven Central:

```groovy
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'org.standardout:gradle-versioneye-plugin:1.0.0'
	}
}

apply plugin: 'org.standardout.versioneye'
```

*gradle-versioneye-plugin* has been tested with Gradle 1.11.

### API key

You need to provide your [VersionEye](https://www.versioneye.com) API key for the plugin to be able to communicate with the VersionEye API. You do this through a property, e.g. by specifying it in the **gradle.properties** file in **~/.gradle/** or the project directory, or via the command line. However, it is strongly recommended not to place it somewhere where it is publicly accessible (e.g. in a public GitHub repository).

```
versioneye.api_key=1234567890abcdef
```

If logged in to VersionEye, you can get or generate your API key [here](https://www.versioneye.com/settings/api).


### Gradle tasks

The **versioneye** plugin comes with two Gradle tasks that are relevant for you:

* ***versioneye-create*** - Creates a project on [VersionEye](https://www.versioneye.com) and write the project key and identifier to your project's **gradle.properties** (so they can be used with ***versioneye-update***)
* ***versioneye-update*** - Updates the dependencies for the project on [VersionEye](https://www.versioneye.com) that is identified by the project key and your API key

Example call creating a VersionEye project - in this case the API key is provided via the command line:
```
gradle -Pversioneye.api_key=1234567890abcdef -info versioneye-create
```

### Project configuration

#### VersionEye project

Once you create a VersionEye project with ***versioneye-create***, it will add the `versioneye.projectkey` and `versioneye.projectid` properties to the **gradle.properties** file in your project directory. But you can also provide these settings manually in any way Gradle supports specifying properties (e.g. if you already have an existing VersionEye project).

#### Which dependencies?

There are two main modes, you can use only the **declared** dependencies or additionally the **transitive** dependencies:

* **declared** - only first level dependencies are included (default)
* **transitive** - the declared and all transitive dependencies

Configuration example:
```groovy
versioneye {
  dependencies = transitive
}
```

To further customize which dependencies are analyzed, you can exclude specific configurations, for example to exclude the dependencies that are only needed for tests with the Gradle Java plugin:
```groovy
versioneye {
  exclude 'testCompile', 'testRuntime'
}
```

Please note that if you exclude a configuration that is extended by another configuration that you did not exclude, this will have no effect (e.g. if you exclude *runtime* but don't exclude *testRuntime*).

**Tip:** If there are dependencies showing up you have no idea where they are coming from, use `gradle -q dependencies` to get an overview of all configurations and the dependencies contained in them. Use it to identifiy the configurations that you don't want to include.

License
-----

[The MIT License (MIT)](http://opensource.org/licenses/MIT)
