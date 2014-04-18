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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;

/**
 * Creates a pom.xml containing the resolved dependencies of the Gradle project.
 * 
 * @author Simon Templer
 */
class PomTask extends DefaultTask {
	
	/**
	 * Spec that accepts only external dependencies.
	 */
	private static class ExternalDependencySpec implements Spec<Dependency> {
		public boolean isSatisfiedBy(Dependency element) {
			return element instanceof ExternalDependency
		}
	}
	private static final ExternalDependencySpec EXTERNAL_DEPENDENCY = new ExternalDependencySpec()
	
	def file
	
	@TaskAction
	def create() {
		assert file as File
		
		def artifacts = project.configurations.names.collect { String name ->
			//TODO check if configuration should be included
			def resolvedConfig = project.configurations.getByName(name).resolvedConfiguration
			// retrieve all external dependencies
			//XXX we are ignoring the dependency relations here - does it matter for VersionEye?
			resolvedConfig.lenientConfiguration.getArtifacts(EXTERNAL_DEPENDENCY)
		}.flatten() as Set
		
		// create the pom.xml
		(file as File).withWriter { w ->
			def pom = new groovy.xml.MarkupBuilder(w)
			pom.project {
				pom.name project.name
				pom.groupId project.group
				pom.version project.version
				pom.artifactId project.name
				pom.dependencies {
					artifacts.each { ResolvedArtifact artifact ->
						pom.dependency {
							groupId artifact.moduleVersion.id.group
							artifactId artifact.moduleVersion.id.name
							version artifact.moduleVersion.id.version
						}
					}
				}
			}
	   }
	}

}
