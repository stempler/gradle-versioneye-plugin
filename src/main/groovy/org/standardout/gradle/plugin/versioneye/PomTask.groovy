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
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
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
    
    // map of artifacts/dependencies to the respective configurations
    Map dependencyMap = [:]

    // project dependencies
    project.configurations.names.collect { String name ->
      // check if configuration should be included
      if (project.versioneye.acceptConfiguration(name)) {
        ResolvedConfiguration config = project.configurations.getByName(name).resolvedConfiguration
        addDependenciesToMap(name, config, dependencyMap)
      }
    }
    
    // project plugins
    if (project.versioneye.includePlugins) {
      project.buildscript.configurations.names.collect { String name ->
        //XXX are there any build script configurations that should not be included?
        ResolvedConfiguration config = project.buildscript.configurations.getByName(name).resolvedConfiguration
        addDependenciesToMap(name, config, dependencyMap, 'plugin')
      }
    }

    // build dependency list for
    def dependencyList = []
    dependencyMap.each { dependency, Set<String> configs ->
      Iterable<String> scopes = determineScopes(configs)
      scopes.each { scope ->
        if (dependency instanceof ResolvedArtifact) {
          ResolvedArtifact artifact = dependency
          dependencyList << [
            name: "${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}",
            version: artifact.moduleVersion.id.version,
            scope: scope
          ]
        }
        else if (dependency instanceof ResolvedDependency) {
          ResolvedDependency dep = dependency
          dependencyList << [
            name: "${dep.moduleGroup}:${dep.moduleName}",
            version: dep.moduleVersion,
            scope: scope
          ]
        }
      }
    }

    // create the pom.json descriptor
    def pomFile = (file as File)
    pomFile.parentFile.mkdirs()
    pomFile.withWriter { w ->
      def pom = new groovy.json.JsonBuilder()
      pom {
        name project.name
        group_id project.group
        // pom.version project.version
        artifact_id project.name
        language 'Java'
        prod_type 'Maven2'
        dependencies dependencyList
      }
      pom.writeTo(w)
    }
  }
  
  // helper methods
  
  def addDependenciesToMap(String configName, ResolvedConfiguration resolvedConfig, Map dependencyMap,
    String scope = null) {
    def deps
    if (project.versioneye.dependencies == VersionEyeExtension.transitive) {
      // transitive dependencies: use all resolved artifacts
      
      // retrieve all external dependencies
      //XXX we are ignoring the dependency relations here - does it matter for VersionEye?
      deps = resolvedConfig.lenientConfiguration.getArtifacts(EXTERNAL_DEPENDENCY)
    }
    else if (project.versioneye.dependencies == VersionEyeExtension.declared) {
      // declared dependencies: only use first level dependencies
      deps = resolvedConfig.lenientConfiguration.getFirstLevelModuleDependencies(EXTERNAL_DEPENDENCY)
    }
    else {
      deps = []
    }
    deps.each { dep ->
      // put in map with configuration name / scope
      Set configSet = dependencyMap.get(dep)
      if (configSet == null) {
        configSet = new HashSet<>()
        dependencyMap.put(dep, configSet)
      }
      if (scope) {
        // fixed scope independent of configuration name
        configSet.add(scope)
      }
      else {
        configSet.add(configName)
      }
    }
  }
  
  Iterable<String> determineScopes(Set<String> configs) {
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

}
