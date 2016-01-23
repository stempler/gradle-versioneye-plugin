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

import groovyx.net.http.ContentType;
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method;
import groovyx.net.http.RESTClient
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody;
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;
import static Util.logResult

/**
 * Updates the VersionEye project with the Gradle project dependencies.
 * 
 * @author Simon Templer
 */
class UpdateTask extends DefaultTask {
	
	def dependencies
	
	@TaskAction
	def update() {
		assert dependencies as File && (dependencies as File).exists()
		def projectId = project.properties[VersionEyePlugin.PROP_PROJECT_ID]
		assert projectId, 'No project ID defined - either define a project ID manually or run the versioneye-create task'
		def apiKey = project.properties[VersionEyePlugin.PROP_API_KEY]
		assert apiKey, 'No API key defined'
		
		def http = new HTTPBuilder(project.versioneye.baseUrl)
		http.request( Method.POST, ContentType.JSON ) { req ->
		  uri.path = '/api/v2/projects/' + projectId
		  uri.query = [ api_key: apiKey ]
		  requestContentType = 'multipart/form-data'
		  MultipartEntity entity = new MultipartEntity()
		  entity.addPart("project_file", new FileBody(dependencies as File, 'pom.xml', 'application/xml', null))
		  req.entity = entity
		  
		  response.success = {
			  resp, json ->
			  assert json, 'Invalid response'
			  project.logger.lifecycle "Updated VersionEye project $json.name successfully"
			  logResult(project, json)
		  }
		}
	}

}
