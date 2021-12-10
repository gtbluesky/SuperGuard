//val gradleVersion: String by rootProject.extra
//val guardVersion: String by rootProject.extra
//val debugMode: Boolean by rootProject.extra

val gradleVersion = "7.0.4"
val guardVersion = "0.0.1"
val debugMode = true

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(gradleApi())
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("com.android.tools.build:gradle:$gradleVersion")
    implementation("commons-io:commons-io:2.11.0")
}

gradlePlugin {
    plugins {
        register("superguard") {
            id = "com.gtbluesky.superguard"
            implementationClass = "com.gtbluesky.guard.SuperGuardPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            if (debugMode) {
                setUrl("${rootProject.buildDir}${File.separator}repo")
            } else {
                isAllowInsecureProtocol = true
                setUrl("http://10.8.0.119/repository/android-release/")
                credentials {
                    username = "zhaodongdong"
                    password = "zhaodongdong"
                }
            }
        }
    }
    publications {
        create<MavenPublication>("publishJar") {
            groupId = "com.gtbluesky.plugin"
            artifactId = "superguard"
            version = guardVersion
            from(components["java"])
        }
    }
}