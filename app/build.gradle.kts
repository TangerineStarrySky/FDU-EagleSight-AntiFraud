plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.example.smsdetection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smsdetection"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.4"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.foundation.layout.android)
    implementation(libs.foundation.android)
    implementation(libs.material3.android)
    implementation(libs.navigation.runtime.ktx)
    implementation(libs.ui.tooling.preview.android)
    implementation(project(":mlc4j"))
    implementation(libs.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.alibaba","dashscope-sdk-java", "2.16.4")
    implementation ("org.jetbrains.kotlinx","kotlinx-serialization-json","1.6.3")
    implementation(kotlin("stdlib"))
    implementation ("androidx.activity","activity-compose","1.3.1")
}