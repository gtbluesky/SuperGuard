package com.gtbluesky.guard

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.gtbluesky.guard.extension.SuperGuardExtension
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import java.io.File
import java.nio.file.Paths

/**
 * 混淆资源路径和文件名
 * 混淆四大组件（Activity、Service、Broadcast Receiver 和 Content Provider）
 * 混淆自定义Application、自定义View等在xml中所引用的类
 */
class SuperGuardPlugin : Plugin<Project> {

    companion object {
        private val CONSUMER_PROGURAD_INFIX_AAR = "transformed${File.separator}jetified-"
        private val CONSUMER_PROGURAD_INFIX_JAR =
            "transformed${File.separator}rules${File.separator}lib${File.separator}META-INF${File.separator}proguard${File.separator}"
    }

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
        if (variant.buildType.name == "debug") {
            return
        }
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

//        project.tasks.findByName("merge${variantName}NativeLibs")?.doLast {
//            inputs.files.forEach {
//                println("doLast inputs path=${it.absolutePath}")
//            }
//            outputs.files.forEach {
//                println("doLast outputs path=${it.absolutePath}")
//            }
//        }
        val extension = project.extensions.getByType(SuperGuardExtension::class.java)
        if (extension.ignoreProguards.isNullOrEmpty()) {
            return
        }
        val proguardFilesTempDir = Paths.get(
            project.buildDir.path,
            "intermediates",
            "proguard_files_temp"
        ).toFile()
        val proguardFilesMap = mutableMapOf<String, String>()
        FileUtils.forceMkdir(proguardFilesTempDir)
        minifyWithR8Task?.doFirst {
            inputs.files.filter {
                (it.absolutePath.endsWith("proguard.txt")
                        && it.absolutePath.contains(
                    CONSUMER_PROGURAD_INFIX_AAR
                )) || (it.absolutePath.endsWith(".pro")
                        && it.absolutePath.contains(
                    CONSUMER_PROGURAD_INFIX_JAR
                ))
            }.forEach outer@ {
                if (extension.ignoreProguards?.size == proguardFilesMap.size) return@doFirst

                extension.ignoreProguards?.forEach { proguard ->
                    if (it.absolutePath.contains("${CONSUMER_PROGURAD_INFIX_AAR}$proguard")
                        || it.absolutePath.contains("${CONSUMER_PROGURAD_INFIX_JAR}$proguard")) {
                        proguardFilesMap[proguard] = it.absolutePath
                        FileUtils.moveFile(it, File(proguardFilesTempDir.absolutePath, "$proguard.txt"))
                        return@outer
                    }
                }
            }
        }
        minifyWithR8Task?.doLast {
            if (proguardFilesMap.isNullOrEmpty()) {
                return@doLast
            }
            proguardFilesMap.forEach { (name, path) ->
                println("path=$path")
                FileUtils.moveFile(File(proguardFilesTempDir.absolutePath, "$name.txt"), File(path))
            }
        }
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