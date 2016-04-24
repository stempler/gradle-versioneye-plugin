### 1.4.0

 - Support for including sub-project dependencies

### 1.3.0

 - Added tasks for security and license checks
 - Support association to an organisation and team when creating a VersionEye project
 - Don't override comments in `gradle.properties` when creating a VersionEye project
 - Support for determining dependencies to Gradle plugins
 - Generate a `pom.json` instead of a `pom.xml` for upload to VersionEye
 - Derive dependency scopes from dependency configuration names
 - Define API key via `VERSIONEYE_API_KEY` environment variable (only if Gradle property is not defined)
 - Added plugin task group and task descriptions
 - Improved error reporting for failed requests to the VersionEye API

### 1.2.0

 - Support customizing base URL for use with VersionEye Enterprise

### 1.1.0

 - Fix for breaking VersionEye API change (Project key is obsolete)

### 1.0.0

 - Qualified plugin ID (`org.standardout.versioneye`) for use with new Gradle plugin repository
 - [#2](https://github.com/stempler/gradle-versioneye-plugin/pull/2) Increased log level to lifecycle and added more logging on update


### 0.1.1

 - Fixed failure when build directory was not present

### 0.1.0 - initial version
