// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val gradleVersion by extra("7.0.4")
    val kotlinVersion by extra("1.5.31")
    val guardVersion by extra("0.0.1")
    val debugMode by extra(true)

    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
//        maven("${rootProject.buildDir}${File.separator}repo")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$gradleVersion")
        classpath(kotlin("gradle-plugin", kotlinVersion))
//        classpath("com.gtbluesky.plugin:resguard:$resguardVersion")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}