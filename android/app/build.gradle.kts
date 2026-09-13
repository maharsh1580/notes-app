plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.notes"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.example.notes"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        val apiUrl = providers.gradleProperty("NOTES_API_URL")
            .getOrElse("http://127.0.0.1:8000/")
        require(apiUrl.endsWith("/")) { "NOTES_API_URL must end with /" }
        require(apiUrl.matches(Regex("https?://[A-Za-z0-9.:/\\-_]+"))) {
            "NOTES_API_URL must be a plain HTTP(S) base URL"
        }
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
