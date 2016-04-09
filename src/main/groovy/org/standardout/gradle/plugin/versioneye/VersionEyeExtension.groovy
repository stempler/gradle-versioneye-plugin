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

import java.util.Set;

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
   * If the license check should fail when unknown licenses are encountered. 
   */
  boolean licenseCheckBreakByUnknown = false

  /**
   * If Gradle plugins should be included in the dependencies.
   * 
   * Gradle plugin dependencies are determined via the build script dependency
   * configurations.
   */
  boolean includePlugins = true

  /**
   * Specify configurations to exclude.
   */
  void exclude(String... configs) {
    (configs as List).each {
      excludeConfigurations << it
    }
  }

  /**
   * Base URL of the VersionEye API.
   */
  String baseUrl = 'https://www.versioneye.com'
  
  /**
   * Default implementation of determining scopes from
   * a dependency's configuration names.
   * 
   * - always retains special 'plugin' configuration as scope
   * - preferably uses scope 'compile', 'test' or 'runtime' based
   *   on standard Gradle project setups
   * - as fallback uses all configuration names as they are 
   */
  public static final Closure DEFAULT = { Set<String> configs ->
    Set<String> result = new HashSet<>()
    
    // plugin scope
    if (configs.remove('plugin')) {
      result.add('plugin')
    }
    
    // best guess based on standard configuration names like for example in the Java plugin
    
    // prefer compile...
    if (configs.any { it.startsWith('compile') }) {
      result.add('compile')
    }
    // ...before test...
    else if (configs.any { it.startsWith('test') }) {
      result.add('test')
    }
    // ...before runtime...
    else if (configs.any { it.startsWith('runtime') }) {
      result.add('runtime')
    }
    // ...otherwise just use configuration names
    else {
      result.addAll(configs)
    }
    
    result
  }
  
  /**
   * Strategy for determining scopes that simply uses the
   * dependency configuration names, as well as the 'plugin'
   * scope for any build script dependencies.
   */
  public static final Closure CONFIGURATIONS = { Set<String> configs ->
    configs
  }
  
  /**
   * Strategy to determine dependency scopes.
   * 
   * Input is the set of configuration names a dependency was found in,
   * output should be an Iterable of Strings that holds the corresponding
   * scope names for the dependency.
   * Note that for all build script dependencies the configuration
   * name given here is 'plugin'. 
   * 
   * See the default implementation for an example.
   */
  Closure determineScopeStrategy = DEFAULT 

  // internal

  /**
   * Test if the given configuration should be excluded.
   */
  boolean acceptConfiguration(String name) {
    !excludeConfigurations.contains(name)
  }

  /**
   * Cache the response received from VersionEye with create or update.
   */
  def lastVersionEyeResponse

}
