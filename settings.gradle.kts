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
        maven { url = uri("https://jitpack.io") } // JitPack 저장소 유지
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")}
    }
}
rootProject.name = "And"
include(":app")
