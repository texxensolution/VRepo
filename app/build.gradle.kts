import com.android.build.api.dsl.ApkSigningConfig
import org.gradle.kotlin.dsl.release

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

android {
    namespace = "com.spmadrid.vrepo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spmadrid.vrepo"
        minSdk = 29
        targetSdk = 34
        versionCode = 10
        versionName = "4.0.3-revert"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "repo-ai"
            keyPassword = "spm123"
            storeFile = file(System.getProperty("user.home") + "/keystores/repoai.jks")
            storePassword = "spm123"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release") // <<< ADD THIS
        }
        release {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        jniLibs {
            excludes += listOf("lib/x86/**", "lib/x86_64/**", "lib/armeabi-v7a/**")
        }
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.security.crypto.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // camerax
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // tflite
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.gpu.delegate.plugin)
    implementation(libs.tensorflow.lite.gpu.api)
    implementation(libs.tensorflow.lite.api)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.select.tf.ops)
    implementation(libs.tensorflow.lite.metadata)
    implementation("com.auth0:java-jwt:4.4.0")
    // mlkit text recognition
    implementation(libs.text.recognition)

    // hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(libs.fastjson)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.websockets)

    // GMS - Google Mobile Services
    implementation(libs.play.services.location)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.androidx.security.crypto)

    implementation(libs.font.awesome)

    implementation(libs.osmdroid.android)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)

}