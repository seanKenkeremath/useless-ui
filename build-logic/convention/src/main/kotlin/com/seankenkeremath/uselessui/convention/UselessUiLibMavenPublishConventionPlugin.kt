package com.seankenkeremath.uselessui.convention

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class UselessUiLibMavenPublishConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("com.vanniktech.maven.publish")

        val extension = project.extensions.create(
            "mavenPublishConfig",
            MavenPublishConfigExtension::class.java
        )

        project.afterEvaluate {
            project.extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
                coordinates(
                    groupId = project.findProperty("GROUP") as String,
                    artifactId = project.name,
                    version = extension.version.orNull ?: "0.0.1"
                )

                pom {
                    name.set(project.name)
                    description.set(extension.description.orElse("No description provided"))
                }
            }
        }
    }
}