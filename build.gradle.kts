// build.gradle 파일

repositories {
    google() // Google Maven 저장소 추가
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // JitPack 저장소 추가
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

buildscript {
    repositories {
        google() // 플러그인을 위한 Google Maven 저장소 추가
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // JitPack 저장소 추가
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // 예시로 안드로이드 플러그인 버전 8.2.0 추가
    }
}

allprojects {
    repositories {
        google() // Google Maven 저장소 추가
        mavenCentral()
    }
}
