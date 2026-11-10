pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // TODO(Pleos): Pleos Connect SDK 저장소를 추가하세요 (Pleos Playground 배포 안내 참고).
        // maven { url = uri("https://<pleos-maven-repo>") }
    }
}

rootProject.name = "Autograph"
include(":app")
