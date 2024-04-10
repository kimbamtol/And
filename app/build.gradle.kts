plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

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
        dataBinding =true // 데이터 바인딩 활성화
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    testImplementation(kotlin("test-junit"))
    implementation("org.jsoup:jsoup:1.14.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //mlkit
    //implementation 'com.google.android.gms:play-services-mlkit-text-recognition:18.0.1' <- Cannot recognize Korean
    // To recognize Korean script
    implementation("com.google.mlkit:text-recognition-korean:16.0.0-beta5")

    }