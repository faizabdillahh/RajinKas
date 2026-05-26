plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.rajinkas"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rajinkas"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

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
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.room.testing)

    // WorkManager
    implementation(libs.work.runtime)

    // Security & Crypto
    implementation(libs.security.crypto)

    // Utilities
    implementation(libs.gson)
    implementation(libs.jbcrypt)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // PDF Generation
    implementation(libs.itext.core)

    // UI & Beautification
    implementation(libs.lottie)
    implementation(libs.shimmer)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
