pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // JitPack 저장소 추가

    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // 다른 설정 저장소 추가
        maven { url = uri("https://jitpack.io") } // JitPack 저장소 추가

    }
}
rootProject.name = "And"
include(":app")
