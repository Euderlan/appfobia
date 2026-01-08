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

        // ✅ necessário em muitos projetos para resolver artifacts do Sceneform maintained
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "AppFobia"
include(":app")
