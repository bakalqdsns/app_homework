plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.anifocus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.anifocus"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.appcompat.v170)
    implementation(libs.material.v1120)
    //noinspection UseTomlInstead
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel.v280)
    implementation(libs.lifecycle.livedata.v280)

    //noinspection GradleDependency
    implementation(libs.recyclerview.v140)

    implementation(libs.okhttp.v532)

    //noinspection GradleDependency
    implementation(libs.gson.v2132)

    implementation(libs.activity)
}
