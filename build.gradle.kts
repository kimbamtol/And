plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

buildscript {
    repositories {
        google() // Plugin for Google Maven repository added
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // JitPack repository added
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0") // Correct plugin version added
    }
}
