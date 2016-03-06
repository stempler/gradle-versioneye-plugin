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
 * Creates a VersionEye project and stores the project key in the
 * gradle.properties of a Gradle project.
 * 
 * @author Simon Templer
 */
class CreateTask extends DefaultTask {
	
	def dependencies
	def properties
	
	@TaskAction
	def create() {
		File propertiesFile = properties as File 
		assert propertiesFile
		assert dependencies as File && (dependencies as File).exists()
		def apiKey = project.properties[VersionEyePlugin.PROP_API_KEY]
		assert apiKey, 'No API key defined'
		def keyAlreadyThere = project.properties[VersionEyePlugin.PROP_PROJECT_ID]
		assert !keyAlreadyThere, 'There is already a project ID defined, run versioneye-update to update the existing project instead'
		
		def http = createHttpBuilder(project)
    
		http.request( Method.POST, ContentType.JSON ) { req ->
		  uri.path = '/api/v2/projects'
		  uri.query = [ api_key: apiKey ]
		  requestContentType = 'multipart/form-data'
		  MultipartEntity entity = new MultipartEntity()
		  entity.addPart("upload", new FileBody(dependencies as File, 'pom.xml', 'application/xml', null))
		  req.entity = entity
		  
		  response.success = {
			  resp, json ->
			  // extract project ID
			  assert json, 'Invalid response'
			  def projectId = json.id
			  assert projectId, 'Project ID could not be determined from response'
			  project.logger.lifecycle "Project created with project ID $projectId"
			  
			  // save project key in properties
			  Properties props = new Properties()
			  if (propertiesFile.exists()) {
				  propertiesFile.withReader {
					  props.load(it)
				  }
			  }
			  props.getProperty(VersionEyePlugin.PROP_PROJECT_ID)?.with {
				  project.logger.warn "Replacing existing project ID $it in $propertiesFile"
			  }
			  props.setProperty(VersionEyePlugin.PROP_PROJECT_ID, projectId)

			  propertiesFile.withWriter {
				  props.store(it, null)
			  }
			  project.logger.warn 'Saved project ID to ' + propertiesFile.name 
			  
			  logResult(project, json)
		  }
		}
	}

}
