package com.gtbluesky.guard

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.gtbluesky.guard.extension.SuperGuardExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import java.nio.file.Paths

/**
 * 混淆资源路径和文件名
 * 混淆四大组件（Activity、Service、Broadcast Receiver 和 Content Provider）
 * 混淆自定义Application、自定义View等在xml中所引用的类
 */
class SuperGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val isAndroidProject = project.plugins.hasPlugin("com.android.application")
        if (!isAndroidProject) {
            throw GradleException("The Gradle plugin can only be used on Android application project")
        }
        if (agpVersion.major < 4 || (agpVersion.major == 4 && agpVersion.minor < 2)) {
            throw GradleException("Only AGP 4.2 and above are supported")
        }
        project.extensions.create("superGuard", SuperGuardExtension::class.java)

        val android = project.extensions.getByName("android") as AppExtension
        project.afterEvaluate {
            android.applicationVariants.all {
                createGuardTask(project, this)
            }
        }
    }

    private fun createGuardTask(project: Project, variant: ApplicationVariant) {
//        if (variant.buildType.name.equals("debug")) {
//            return
//        }
        val variantName = variant.name.capitalize()
        val processResourcesTaskName = "process${variantName}Resources"
        val optimizeResourcesTaskName = "optimize${variantName}Resources"
        val minifyWithR8TaskName = "minify${variantName}WithR8"
        val minifyWithR8Task = project.tasks.findByName(minifyWithR8TaskName)
        val processResourcesTask = project.tasks.findByName(processResourcesTaskName)
        val optimizeResourcesTask = project.tasks.findByName(optimizeResourcesTaskName)
        val guardTaskName = "obscure${variantName}Resources"
        val guardTask = if (project.tasks.findByName(guardTaskName) == null) {
            project.tasks.create(guardTaskName, SuperGuardTask::class.java)
        } else {
            project.tasks.getByName(guardTaskName) as SuperGuardTask
        }.also {
            it.variant = variant
        }
        processResourcesTask?.doLast {
            val aaptRulesPath = Paths.get(
                project.buildDir.path,
                "intermediates",
                "aapt_proguard_file",
                variant.name,
                "aapt_rules.txt"
            )
            if (aaptRulesPath.toFile().exists()) {
                aaptRulesPath.toFile().delete()
                aaptRulesPath.toFile().createNewFile()
            }
        }
        // 由于 optimizeResourcesTask 和 minifyWithR8Task 是同时进行的，两者结束没有固定的先后顺序
        // 但是我们需在这两个Task都完成后才进行资源混淆，所以采用以下方式
        optimizeResourcesTask?.finalizedBy(guardTask)
        minifyWithR8Task?.finalizedBy(guardTask)
    }
}

val agpVersion: VersionNumber
    get() {
        return try {
            val clazz = Class.forName("com.android.builder.model.Version")
            val version = clazz.fields.first { it.name == "ANDROID_GRADLE_PLUGIN_VERSION" }
                .get(null) as String
            return VersionNumber.parse(version)
        } catch (e: ClassNotFoundException) {
            VersionNumber.UNKNOWN
        }
    }