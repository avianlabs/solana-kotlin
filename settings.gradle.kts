pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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

rootProject.name = "solana-kotlin-sdk"

include(":solana-kotlin")
include(":solana-kotlin-arrow-extensions")
include(":tweetnacl-multiplatform")
include(":codegen")
