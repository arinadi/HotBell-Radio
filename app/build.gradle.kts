plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.hotbell.radio"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hotbell.radio"
        minSdk = 26
        targetSdk = 34
        
        // Git-based Versioning
        fun getGitCommitCount(): Int {
            return try {
                val process = ProcessBuilder("git", "rev-list", "--count", "HEAD").start()
                process.inputStream.bufferedReader().readText().trim().toInt()
            } catch (e: Exception) {
                1 // Fallback
            }
        }

        fun getGitCommitHash(): String {
            return try {
                val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
                process.inputStream.bufferedReader().readText().trim()
            } catch (e: Exception) {
                "unknown"
            }
        }

        val commitCount = getGitCommitCount()
        val commitHash = getGitCommitHash()

        versionCode = commitCount
        versionName = "1.$commitCount.$commitHash"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: "hotbell_password"
            keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "hotbell"
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: "hotbell_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "HotBell-Radio-${variant.versionName}-${variant.name}.apk"
            }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Media3 (ExoPlayer)
    val media3_version = "1.3.0"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-session:$media3_version")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Extended Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Gemini Generative AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.8.0")
}
