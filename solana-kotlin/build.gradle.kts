plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
  id("io.github.luca992.multiplatform-swiftpackage") version "2.2.2"
}

group = "net.avianlabs.solana"
version = properties["version"] as String

kotlin {
  targetHierarchy.default()
  explicitApi()

  jvm {
    // set the target JVM version
    compilations.all {
      kotlinOptions {
        jvmTarget = "17"
      }
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
    ).forEach { iosTarget ->
      iosTarget.binaries.framework {
        baseName = "SolanaKotlin"
        isStatic = true
      }
    }

  mingwX64()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.ktorClientOkHttp)
        implementation(libs.bouncyCastle)
      }
    }
    val jvmTest by getting

    val commonMain by getting {
      dependencies {
        api(project(":tweetnacl-multiplatform"))
        implementation(libs.coroutinesCore)
        implementation(libs.ktorClientCore)
        implementation(libs.ktorClientLogging)
        implementation(libs.serializationJson)
        implementation(libs.ktorClientContentNegotiation)
        implementation(libs.ktorSerializationKotlinxJson)
        implementation(libs.kermit)
        implementation(libs.okio)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
    }

    val iosMain by getting {
      dependencies {
        implementation(libs.ktorClientDarwin)
      }
    }
    val iosTest by getting {
    }

    val nativeMain by getting {
    }
  }
}

multiplatformSwiftPackage {
  swiftToolsVersion("5.9")
  targetPlatforms {
    iOS { v("16") }
  }
  packageName("SolanaKotlin")
  zipFileName("SolanaKotlin")
  distributionMode { remote("https://github.com/avianlabs/solana-kotlin/releases/download/0.1.5") }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}

signing {
  useGpgCmd()
}

publishing {
  repositories {
    mavenLocal()
    repositories {
      maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/avianlabs/solana-kotlin")
        credentials {
          username = System.getenv("GITHUB_ACTOR")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }
  publications {
    withType<MavenPublication> {
      pom {
        name = "Solana Kotlin"
        description = "Kotlin Multiplatform library to interface with the Solana network"
        licenses {
          license {
            name = "MIT"
            url = "https://opensource.org/licenses/MIT"
          }
        }
        url = "https://github.com/avianlabs/solana-kotlin"
        issueManagement {
          system = "GitHub"
          url = "https://github.com/avianlabs/solana-kotlin"
        }
        scm {
          connection = "https://github.com/avianlabs/solana-kotlin.git"
          url = "https://github.com/avianlabs/solana-kotlin"
        }
        developers {
          developer {
            name = "Avian Labs Engineers"
            email = "engineering@avianlabs.net"
          }
        }
      }
    }
  }
}

mavenPublishing {
  if (rootProject.findProperty("signPublications") != "false") {
    signAllPublications()
  }
}
