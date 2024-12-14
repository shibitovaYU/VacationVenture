plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    kotlin("kapt") // Добавьте эту строку, если используете KAPT
}

android {
    namespace = "com.example.vacationventure"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packagingOptions {
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/*.version")
    }

    defaultConfig {
        applicationId = "com.example.vacationventure"
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
            buildConfigField("String", "API_KEY", "\"lYnUdhPP2R9q23aGo3mA3f1PUQnflzB9\"")
        }
        debug {
            buildConfigField("String", "API_KEY", "\"lYnUdhPP2R9q23aGo3mA3f1PUQnflzB9\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Зависимости Retrofit
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.8.0")
    implementation("com.google.code.gson:gson:2.8.8")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-database")
    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.6")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    kapt ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("androidx.room:room-runtime:2.5.2")
    kapt ("androidx.room:room-compiler:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation ("com.google.mlkit:translate:16.0.0")
    implementation ("com.google.mlkit:translate:2.2.0")
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)

    // Зависимости для тестирования
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation ("org.mockito:mockito-core:4.12.0")
    testImplementation ("org.mockito:mockito-inline:4.6.1")
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation ("org.mockito:mockito-core:4.8.0")
    testImplementation ("org.mockito:mockito-inline:4.8.0")
    testImplementation ("androidx.test.ext:junit:1.1.3")
    testImplementation ("androidx.test:core:1.4.0")

    testImplementation ("com.google.firebase:firebase-database-ktx:20.0.5")
    testImplementation ("com.google.firebase:firebase-auth:21.1.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0'")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}
