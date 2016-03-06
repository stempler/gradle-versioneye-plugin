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

}
