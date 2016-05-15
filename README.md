gradle-versioneye-plugin
========================

Plugin for [Gradle](http://www.gradle.org/) to update your project dependencies status on [VersionEye](https://www.versioneye.com) based on the resolved dependency configurations of your Gradle project.

It works quite similar to the [VersionEye plugin for Maven](https://github.com/versioneye/versioneye_maven_plugin).

[![Dependency Status](https://www.versioneye.com/java/org.standardout:gradle-versioneye-plugin/badge.svg)](https://www.versioneye.com/java/org.standardout:gradle-versioneye-plugin)

Usage
-----

The simplest way to apply the plugin to your Gradle build is to use the **Gradle 2.1** plugin mechanism:

```groovy
plugins {
    id "org.standardout.versioneye" version "1.4.0"
}
```

For **Gradle 1.x and 2.0** add the artifact to your buildscript classpath via jCenter or Maven Central:

```groovy
buildscript {
	repositories {
		jcenter()
	}
	dependencies {
		classpath 'org.standardout:gradle-versioneye-plugin:1.4.0'
	}
}

apply plugin: 'org.standardout.versioneye'
```

### API key

You need to provide your [VersionEye](https://www.versioneye.com) API key for the plugin to be able to communicate with the VersionEye API. You do this through a Gradle property, e.g. by specifying it in the **gradle.properties** file in **~/.gradle/** or the project directory, or via the command line. However, it is strongly recommended not to place it somewhere where it is publicly accessible (e.g. in a public GitHub repository).

```
versioneye.api_key=1234567890abcdef
```

If logged in to VersionEye, you can get or generate your API key [here](https://www.versioneye.com/settings/api).


#### Environment variable

Starting from version 1.3, if no Gradle property for the API key is defined, the plugin will fall back to the value found in the `VERSIONEYE_API_KEY` environment variable.


### Gradle tasks

The **versioneye** plugin comes with two main Gradle tasks that are relevant for you:

* ***versioneye-create*** - Creates a project on [VersionEye](https://www.versioneye.com) and write the project ID to your project's **gradle.properties** (so they can be used with ***versioneye-update***)
* ***versioneye-update*** - Updates the dependencies for the project on [VersionEye](https://www.versioneye.com) that is identified by the project ID and your API key
 
Alternatively you can use the CamelCase versions of these tasks, ***versionEyeCreate*** and ***versionEyeUpdate*** which can be abbreviated on the command line (see [the Gradle documentation](http://www.gradle.org/docs/current/userguide/tutorial_gradle_command_line.html)), e.g. `gradle vEU` or `gradle vEyeU` for executing ***versionEyeUpdate***.

Example call creating a VersionEye project - in this case the API key is provided via the command line:

```
gradle -Pversioneye.api_key=1234567890abcdef -info versioneye-create
```

#### Additional checks

Based on the information retrieved from VersionEye you can do a number of additional checks with the following tasks:

* ***versionEyeLicenseCheck*** (since 1.3) - Check if there are any violations of your license white list
* ***versionEyeSecurityCheck*** (since 1.3) - Check if there are any dependencies with known security vulnerabilities
* ***versionEyeSecurityAndLicenseCheck*** (since 1.3) - Check both security vulnerabilities and license violations

Executing any of these tasks will update the project on VersionEye.


### Project configuration

#### VersionEye project

Once you create a VersionEye project with ***versioneye-create***, it will add the `versioneye.projectid` property to the **gradle.properties** file in your project directory. But you can also provide these settings manually in any way Gradle supports specifying properties (e.g. if you already have an existing VersionEye project).


#### Organisation and Team

When creating a project you can directly associate it with a specific organisation and team.
All you need to do is provide the corresponding Gradle properties for ***versioneye-create***:

* ***versioneye.organisation*** - the organisation namespace
* ***versioneye.team*** - the team name

These properties can be defined via **gradle.properties** file or via the command line, for example:

```
gradle -Pversioneye.api_key=1234567890abcdef -Pversioneye.organisation=myorg -Pversioneye.team=myteam versioneye-create
```


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

**Tip:** If there are dependencies showing up you have no idea where they are coming from, use `gradle dependencies` to get an overview of all configurations and the dependencies contained in them. Use it to identifiy the configurations that you don't want to include.

Since version 1.3, the plugins that you use for your build script are also included in the dependencies reported to VersionEye. If you don't want that, you can disable this feature in the configuration:

```groovy
versioneye {
  includePlugins = false
}
```


##### Multi-project builds (since 1.4)

If you have a multi-build project that you want to handle as one single VersionEye project, you should apply the plugin only to the root project and configure the plugin to include dependencies from sub-projects as well:

```groovy
versioneye {
  includeSubProjects = true
}
```


#### Unknown licenses

If you want the license check to fail when dependencies with unknown license are encountered, you need to enable it in the configuration like this:

```groovy
versioneye {
  licenseCheckBreakByUnknown = true
}
```


#### VersionEye Enterprise

If you want to connect to a VersionEye Enterprise installation instead of *versioneye.com*, you can adapt the base URL used to access the API:

```groovy
versioneye {
  baseUrl = 'https://www.versioneye.com' // this is the default
}
```


#### Dependency scopes

The dependency scope in VersionEye is used to organize dependencies in different groups, for instance compile time dependencies or test dependencies.

Your project dependencies in Gradle are organised in dependency configurations, for instance in most projects there is a `compile` configuration. The dependency scope is determined based on the information in which configurations it is present.
The default strategy tries to identify the primary configuration of a dependency and use that as a scope.
This works best for standard project setups, so if you feel that you can provide a more optimal grouping, you can provide your own implementation.

The `DEFAULT` strategy is configured if you do not override the setting. Another provided strategy is the `CONFIGURATIONS` strategy.
It uses the configuration associations as is. You can enable it like this:

```groovy
versioneye {
  determineScopeStrategy = CONFIGURATIONS
}
```

You can provide your own implementation by providing a closure that calculates an Iterable of scope names from the Set of configuration names.
Build script dependencies here have the configuration name `'plugin'` associated.

```groovy
versioneye {
  determineScopeStrategy = { Set<String> configs ->
    def scopes = []

    //TODO determine scopes based on the configuration names

    scopes
  }
}
```


### Using the current SNAPSHOT

If you want to test the latest version with changes that have not been released yet, you can configure your project to use the latest SNAPSHOT:

```groovy
buildscript {
  repositories {
    maven {
      url 'http://oss.sonatype.org/content/repositories/snapshots/'
    }
    jcenter()
  }
  dependencies {
    classpath 'org.standardout:gradle-versioneye-plugin:1.4.0-SNAPSHOT'
  }
}

apply plugin: 'org.standardout.versioneye'
```


License
-----

[The MIT License (MIT)](http://opensource.org/licenses/MIT)
