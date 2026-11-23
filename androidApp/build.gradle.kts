import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Deployment configuration, never entered by a worker in the login screen.
val backendUrl = providers.gradleProperty("indumotos.backendUrl")
    .orElse(providers.environmentVariable("INDUMOTOS_BACKEND_URL"))
    .orElse("http://127.0.0.1:8089").get().trim().trimEnd('/')
val backendUri = URI(backendUrl)
require(backendUri.scheme in listOf("http", "https") && backendUri.host != null &&
    backendUri.userInfo == null && backendUri.query == null && backendUri.fragment == null &&
    backendUri.path.isNullOrEmpty()) {
    "El backend debe ser un origen HTTP(S), sin credenciales, ruta, consulta ni fragmento"
}
require(backendUri.scheme == "https" || backendUri.host in listOf("127.0.0.1", "localhost", "10.0.2.2")) {
    "HTTP solo se permite para desarrollo local; usa HTTPS para servidores remotos"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "pe.indumotos.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.indumotos.asapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "0.2.1-sprint2"
        buildConfigField("String", "BACKEND_URL", "\"$backendUrl\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
