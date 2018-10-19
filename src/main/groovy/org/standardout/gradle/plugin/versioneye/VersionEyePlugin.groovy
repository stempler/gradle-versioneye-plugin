/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Simon Templer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.standardout.gradle.plugin.versioneye

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.jar.*

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedArtifact

import static Util.fail

/**
 * VersionEye plugin for Gradle.
 * 
 * @author Simon Templer
 */
public class VersionEyePlugin implements Plugin<Project> {

  @Deprecated
  public static final String PROP_PROJECT_KEY = 'versioneye.projectkey'

  public static final String PROP_PROJECT_ID = 'versioneye.projectid'
  public static final String PROP_API_KEY = 'versioneye.api_key'
  
  public static final String PROP_ORGANISATION = 'versioneye.organisation'
  public static final String PROP_TEAM = 'versioneye.team'

  private Project project

  private File dependenciesFile

  private File gradleProperties

  @Override
  public void apply(Project project) {
    this.project = project

    // create extension
    project.extensions.create('versioneye', VersionEyeExtension, project)

    // dependencies file to create
    dependenciesFile = new File(project.buildDir, 'pom.json')

    // project specific properties file
    gradleProperties = new File(project.projectDir, 'gradle.properties')
    
    def groupName = 'VersionEye'

    // task creating the POM representation of the dependencies
    Task jsonTask = project.task('versioneye-pom', type: PomTask) {
      file = dependenciesFile
    }
    jsonTask.description = 'Generate a pom.json file with the project\'s dependencies'
    jsonTask.group = groupName

    // task creating a version eye project
    Task createTask = project.task('versioneye-create', type: CreateTask) {
      dependencies = dependenciesFile
      properties = gradleProperties
    }
    createTask.dependsOn(jsonTask)
    createTask.description = 'Create a VersionEye project via the VersionEye API'
    createTask.group = groupName

    // task updating the version eye project
    Task updateTask = project.task('versioneye-update', type: UpdateTask) {
      dependencies = dependenciesFile
    }
    updateTask.dependsOn(jsonTask)
    updateTask.description = 'Update the associated VersionEye project via the VersionEye API'
    updateTask.group = groupName

    // Check tasks based on VersionEye response

    Task licenseCheckTask = project.task('versionEyeLicenseCheck', dependsOn: updateTask).doFirst {
      def json = project.versioneye.lastVersionEyeResponse
      if (json.licenses_red) {
        fail(project, "${json.licenses_red} dependencies violate the license whitelist!")
      }
      if (project.versioneye.licenseCheckBreakByUnknown && json.licenses_unknown) {
        fail(project, "${json.licenses_unknown} dependencies are without any license!")
      }
    }
    licenseCheckTask.description = 'Check license violations in project dependencies'
    licenseCheckTask.group = groupName
    
    Task securityCheckTask = project.task('versionEyeSecurityCheck', dependsOn: updateTask).doFirst {
      def json = project.versioneye.lastVersionEyeResponse
      if (json.sv_count) {
        json.dependencies?.findAll { it.security_vulnerabilities != null }.each {
          project.logger.error("Security vulnerability in $it.name: $it.security_vulnerabilities\n")
        }
        fail(project, "${json.sv_count} dependencies have known security vulnerabilities!")
      }
    }
    securityCheckTask.description = 'Check security vulnerabilities in project dependencies'
    securityCheckTask.group = groupName
    
    Task securityAndLicenseCheckTask = project.task('versionEyeSecurityAndLicenseCheck',
      dependsOn: [securityCheckTask, licenseCheckTask])
    securityAndLicenseCheckTask.description = 'Check security vulnerabilities and license violations'
    securityAndLicenseCheckTask.group = groupName

    // task aliases using CamelCase (#4)
    Task createAlias = project.task('versionEyeCreate').dependsOn(createTask)
    createAlias.description = createTask.description + ' (Alias for original task)'
    createAlias.group = groupName
    
    Task updateAlias = project.task('versionEyeUpdate').dependsOn(updateTask)
    updateAlias.description = updateTask.description  + ' (Alias for original task)'
    updateAlias.group = groupName
  }

}
