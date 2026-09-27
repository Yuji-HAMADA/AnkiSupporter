import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Compose Compiler Gradle Plugin
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.yuji.ankisupporter"
    compileSdk = 35 // 36から35（安定版）へ

    defaultConfig {
        applicationId = "com.yuji.ankisupporter"
        minSdk = 31
        targetSdk = 35 // 35に合わせる
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        if (project.rootProject.file("local.properties").exists()) {
            properties.load(project.rootProject.file("local.properties").reader())
        }
        buildConfigField("String", "myGoogleCustomSearchKey", "\"${properties.getProperty("myGoogleCustomSearchKey", "")}\"")
        buildConfigField("String", "myGoogleSearchEngineID", "\"${properties.getProperty("myGoogleSearchEngineID", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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

    // ★重要: composeOptions は削除 (Kotlin Compose Pluginが自動管理するため)
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.fragment.ktx)
    
    implementation(libs.retrofit)
    implementation(libs.retrofit.scalars)
    implementation(libs.jsoup)
    implementation(libs.translate)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// dependencies は現状の alias 利用で問題ありません
