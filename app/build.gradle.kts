plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.timeblock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.timeblock"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Downgraded to match Kotlin 1.9.22
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // The room (sqlite wrapper)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Explicitly add animation dependencies with the same version as other Compose components
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Task to run all checks, compile test classes, and generate a debug APK
tasks.register("buildAndCheck") {
    dependsOn(
        "clean",
        "lintDebug",
        "compileDebugSources",
        "compileDebugUnitTestSources",
        "compileDebugAndroidTestSources",
        "assembleDebug",
    )

    group = "verification"
    description = "Runs all checks, compiles test classes and builds a debug APK"

    doLast {
        println("\n======================================================")
        println("Build and Check Task Completed Successfully")
        println("Debug APK is available at: ${layout.buildDirectory.get()}/outputs/apk/debug/app-debug.apk")
        println("======================================================\n")
    }
}

// Task to only compile everything without generating APK
tasks.register("compileAll") {
    dependsOn(
        "compileDebugSources",
        "compileDebugUnitTestSources",
        "compileDebugAndroidTestSources"
    )

    group = "build"
    description = "Compiles all source sets to check for compilation errors"

    doLast {
        println("\n======================================================")
        println("All compilations completed successfully")
        println("======================================================\n")
    }
}

// Task to show detailed error reports
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = false
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xdebug"
        )
    }
}