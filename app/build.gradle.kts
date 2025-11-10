plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.pinehook.plus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pinehook.plus"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    tasks.whenTaskAdded {
        if (name in listOf(
                "mergeReleaseJniLibFolders",
                "mergeReleaseNativeLibs",
                "stripReleaseDebugSymbols",
                "extractReleaseNativeSymbolTables",
                "mergeReleaseNativeDebugMetadata"
            )) {
            enabled = false
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        prefab = false
    }

    packaging {
        jniLibs.keepDebugSymbols.add("**/*.so")
    }

    sourceSets {
        getByName("main") {
            resources.srcDirs("src/hook")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.pine)
    implementation(libs.pine.exposed)

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}