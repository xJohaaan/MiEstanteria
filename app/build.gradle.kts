import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// URL del servidor. Se puede cambiar sin tocar codigo poniendo en local.properties:
//   api.url.debug=http://192.168.1.50:8080/api/v1/
//   api.url.release=https://mi-estanteria.up.railway.app/api/v1/
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
// 10.0.2.2 es la direccion del PC visto desde el emulador de Android
val apiUrlDebug: String = localProps.getProperty("api.url.debug") ?: "http://10.0.2.2:8080/api/v1/"
val apiUrlRelease: String = localProps.getProperty("api.url.release") ?: apiUrlDebug

android {
    namespace = "com.example.miestanteria"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.miestanteria"
        minSdk = 24 // Android 7.0 (RNF04)
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK release: los datos estan en local.properties (no se suben a git)
    val firmaArchivo = localProps.getProperty("release.storeFile")
    signingConfigs {
        if (firmaArchivo != null) {
            create("release") {
                storeFile = rootProject.file(firmaArchivo)
                storePassword = localProps.getProperty("release.storePassword")
                keyAlias = localProps.getProperty("release.keyAlias")
                keyPassword = localProps.getProperty("release.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"$apiUrlDebug\"")
        }
        release {
            buildConfigField("String", "API_URL", "\"$apiUrlRelease\"")
            if (firmaArchivo != null) signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
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

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // Red: Retrofit + OkHttp (HTTPS / JSON)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Base de datos local (funcionamiento sin conexion)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Portadas de libros
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
