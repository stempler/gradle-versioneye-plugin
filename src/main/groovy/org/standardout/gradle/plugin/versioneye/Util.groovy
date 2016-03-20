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

import groovyx.net.http.HTTPBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Utility methods.
 * 
 * @author Simon Templer
 */
class Util {

  static def logResult(Project project, def json) {
    json.dependencies?.each {
      if (it.outdated) {
        project.logger.lifecycle "Consider updating $it.name from $it.version_requested to $it.version_current"
      } else if (!it.unknown) {
        project.logger.lifecycle "$it.name is up-to-date"
      }
    }
    json.dep_number?.with{ project.logger.lifecycle "$it dependencies overall" }
    json.out_number?.with{ project.logger.lifecycle "$it outdated dependencies" }

    // cache result for further analysis
    project.versioneye.lastVersionEyeResponse = json
  }

  static HTTPBuilder createHttpBuilder(Project project) {
    def http = new HTTPBuilder(project.versioneye.baseUrl)

    http.handler.failure = { resp, data ->
      def msg = "Unexpected failure accessing VersionEye API: ${resp.statusLine}"
      if (data?.error) {
        msg += " (${data.error})"
      }
      fail(project, msg)
    }

    http
  }

  static void fail(Project project, String msg) {
    project.logger.error(msg)
    throw new GradleException(msg)
  }
  
  static void saveProjectId(Project project, File propertiesFile, def projectId) {
    def lines
    if (propertiesFile.exists()) {
      lines = propertiesFile.readLines()
    }
    else {
      lines = []
    }
    
    // there should be no special chars in the project ID, so building the property like this should be OK
    def projectIdLine = VersionEyePlugin.PROP_PROJECT_ID + '=' + projectId
    
    boolean replaced = false 
    lines = lines.collect { line ->
      if (line.startsWith(VersionEyePlugin.PROP_PROJECT_ID + '=')) {
        project.logger.warn "Replacing existing project ID in $propertiesFile"
        replaced = true
        projectIdLine
      }
      else {
        line
      }
    }
    if (!replaced) {
      // add project ID at the end
      lines << '# VersionEye project ID added by Gradle VersionEye plugin'
      lines << projectIdLine
    }

    propertiesFile.withPrintWriter { writer ->
      lines.each {
        writer.println(it)
      }
    }
    project.logger.warn 'Saved project ID to ' + propertiesFile.name
  }
  
  static String getApiKey(Project project) {
    def key = null
    // try Gradle property first
    if (project.hasProperty(VersionEyePlugin.PROP_API_KEY)) {
      key = project.properties[VersionEyePlugin.PROP_API_KEY]
    }
    // try environment variable
    else {
      key = System.getenv('VERSIONEYE_API_KEY')
    }
    
    assert key, 'No API key defined'
    
    // just in case
    key = key.trim()
    
    key
  }

}
