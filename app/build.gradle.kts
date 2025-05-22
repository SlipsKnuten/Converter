
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // If you used @GlideModule and need annotation processing:
    // id("kotlin-kapt")
}

android {
    namespace = "com.example.converter"
    compileSdk = 34 // Or your preferred SDK, e.g., 33, 35

    defaultConfig {
        applicationId = "com.example.converter"
        minSdk = 21
        targetSdk = 34 // Match compileSdk or lower
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Should be like: com.google.android.material:material:1.x.x
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout) // If defined in libs.versions.toml

    // Glide for GIF loading
    implementation("com.github.bumptech.glide:glide:4.16.0") // Check latest version
    // If using @GlideModule for custom Glide configuration (not strictly needed for basic use):
    // kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Coroutines (if ParticleView was still in use, not for GIF version unless other async tasks)
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
