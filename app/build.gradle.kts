plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.gtbluesky.superguard")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.gtbluesky.obscureapk"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    signingConfigs {
        register("release") {
            storeFile = file("${project.rootDir}${File.separator}release.jks")
            storePassword = "gt123456"
            keyAlias = "gtkey"
            keyPassword = "gt123456"
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

//    splits {
//        abi {
//            isEnable = true
//            reset()
//            include("armeabi-v7a", "arm64-v8a")
//            isUniversalApk = true
//        }
//    }
//
//    flavorDimensions.add("project")
//
//    productFlavors {
//        create("xiaomi") {
//            dimension = "project"
//        }
//
//        create("huawei") {
//            dimension = "project"
//        }
//    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

superGuard {
    fixedResName = "GT_PROTECTOR"
//    whiteList = listOf(
//        "R.layout.activity_main"
//    )
//    resDir = "haha"
    dictionary = "mt-dictionary.txt"
}

dependencies {
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}