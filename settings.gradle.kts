pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
//  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google {
      content {
        includeGroupByRegex("""com\.android\..*""")
        includeGroupByRegex("""androidx\..*""")
        includeGroupByRegex("""com\.google\..*""")
      }
    }
    mavenCentral()
    maven {
      url = uri("https://jitpack.io")
      content {
      }
    }
  }
}

include(":solana-kotlin")
include(":solana-kotlin-arrow-extensions")
include(":tweetnacl-multiplatform")
include(":codegen")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("libs.versions.toml"))
    }
  }
}
