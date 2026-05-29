plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("org.lsposed.lsparanoid") version "0.5.2"
}

apply(from = "../signing.gradle")

android {
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
    }

    namespace = "com.thunder"
    compileSdk = 34
    ndkVersion = "24.0.8215888"

    defaultConfig {
        applicationId = "com.thunder"
        minSdk = 24
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = false

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits.abi.apply {
        isEnable = true
        reset()
        include("arm64-v8a")
       // include("armeabi-v7a", "arm64-v8a")
        isUniversalApk = true
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            defaultConfig.applicationId = "com.Captainj"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }

        debug {
            defaultConfig.applicationId = "com.Captain"
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }
    }

    packaging.resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
        excludes += "/META-INF/DEPENDENCIES"
        excludes += "/META-INF/LICENSE"
        excludes += "/META-INF/LICENSE.txt"
        excludes += "/META-INF/license.txt"
        excludes += "/META-INF/NOTICE"
        excludes += "/META-INF/NOTICE.txt"
        excludes += "/META-INF/notice.txt"
        excludes += "/META-INF/ASL2.0"
        excludes += "/META-INF/*.kotlin_module"
        excludes += "/META-INF/gradle/incremental.annotation.processors"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/cpp/Android.mk")
        }
    }
}

dependencies {

    implementation(fileTree("libs").include("*.aar"))
    
    implementation("com.airbnb.android:lottie-compose:6.7.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // 🔥 FIXED: AGP 8.5 compatible Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // JSON
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    implementation("net.lingala.zip4j:zip4j:2.11.5") {
        exclude(group = "net.java.dev.jna", module = "jna")
        exclude(group = "net.java.dev.jna", module = "jna-platform")
    }

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    
    //vbox library Black Reflection
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
    implementation("com.github.tiann:FreeReflection:3.2.2")
    implementation("com.github.CodingGay.BlackReflection:core:1.1.2")
    annotationProcessor("com.github.CodingGay.BlackReflection:compiler:1.1.2")
}