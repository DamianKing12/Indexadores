
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.lagradost.cloudstream3.gradle")
}

android {
    namespace = "com.DamianKing12.cuevana"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 21
        targetSdk = 35
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

cloudstream {
    authors = listOf("DamianKing12")
    description = "Plugins indexadores de búsqueda (SeriesKao, Cuevana, etc)"
    requiresResources = true
    language = "es"
    iconUrl = "https://www.google.com/s2/favicons?domain=cuevana3.nu&sz=%size%"
}




dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // dependecia corregidas por chatgpt
    implementation("com.github.recloudstream:cloudstream:master-SNAPSHOT")
    
    implementation(kotlin("stdlib"))
    implementation("com.github.Blatzar:NiceHttp:0.4.11")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
}
