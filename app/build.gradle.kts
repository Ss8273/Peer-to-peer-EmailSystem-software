plugins {
    alias(libs.plugins.android.application)
    kotlin("kapt") version "1.8.0"
}

android {
    namespace = "com.app.emailsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.emailsystem"
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
}

dependencies {
    // Core dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //导入webrtc依赖 2025-3-24
    implementation("org.webrtc:google-webrtc:1.0.32006")
    implementation("com.google.code.gson:gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:3.9.0")

    // Activity KTX
    //implementation("androidx.activity:activity-ktx:1.3.1")

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("androidx.annotation:annotation:1.3.0") {
            exclude(group = "androidx.annotation", module = "annotation-jvm")
        }
    implementation ("androidx.cardview:cardview:1.0.0")



    // Room components
    //implementation("androidx.room:room-runtime:2.5.2")
    //implementation("androidx.room:room-ktx:2.4.2")
    //implementation(libs.annotation.jvm)

    // Test dependencies
    testImplementation("androidx.room:room-testing:2.4.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}