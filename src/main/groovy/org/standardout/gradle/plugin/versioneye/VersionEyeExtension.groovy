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

import org.gradle.api.Project;

/**
 * Extension for configuring VersionEye plugin.
 * 
 * @author Simon Templer
 */
class VersionEyeExtension {
	
	// dependencies values
	public static final String transitive = 'transitive'
	public static final String declared = 'declared'
	
	final Project project
	
	VersionEyeExtension(Project project) {
		this.project = project
	}
	
	/**
	 * States which dependencies are analyzed - only the defined or all resolved dependencies.
	 */
	String dependencies = declared
	
	/**
	 * Configuration to exclude when calculating the dependencies.
	 */
	final Set<String> excludeConfigurations = new HashSet<String>()
	
	/**
	 * Specify configurations to exclude.
	 */
	void exclude(String... configs) {
		(configs as List).each {
			excludeConfigurations << it
		}
	}
	
	// internal
	
	/**
	 * Test if the given configuration should be excluded.
	 */
	boolean acceptConfiguration(String name) {
		!excludeConfigurations.contains(name)
	}

}
