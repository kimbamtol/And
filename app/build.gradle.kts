plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.and"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.and"
        minSdk = 34
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
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true // 데이터 바인딩 활성화
        viewBinding = true
    }

}


dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.fragment:fragment-ktx:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity:1.8.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")    //mlkit
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jsoup:jsoup:1.14.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test-junit"))

    implementation ("com.kakao.sdk:v2-user:2.20.1")
    implementation("com.navercorp.nid:oauth:5.9.1") // jdk 11
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.0")





    //implementation 'com.google.android.gms:play-services-mlkit-text-recognition:18.0.1' <- Cannot recognize Korean
    // To recognize Korean script
    implementation("com.google.mlkit:text-recognition-korean:16.0.0-beta5")
    // CameraX core library using the camera2 implementation
    val camerax_version = "1.0.2"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:1.0.0-alpha31")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:1.0.0-alpha31")

    // Image Crop
    implementation("com.vanniktech:android-image-cropper:4.5.0")
//    implementation ("com.canhub.cropper:android-cropper:2.8.0")

}
