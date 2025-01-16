import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
  alias(libs.plugins.multiplatform.swiftpackage)
}

group = "net.avianlabs.solana"
version = properties["version"] as String

kotlin {
  applyDefaultHierarchyTemplate()
  explicitApi()

  jvm {
    // set the target JVM version
    compilations.all {
      kotlinOptions {
        jvmTarget = "17"
      }
    }
  }

  iosArm64()
  iosSimulatorArm64()

  mingwX64()
  linuxX64()

  @OptIn(ExperimentalSwiftExportDsl::class)
  swiftExport {
    // Root module name
    moduleName = "SolanaKotlin"

    // Collapse rule
    flattenPackage = "net.avianlabs.solana"

//    // Export external modules
//    export(project(":tweetnacl-multiplatform")) {
//      // Exported module name
//      moduleName = "TweetNaCl"
//      // Collapse exported dependency rule
//      flattenPackage = "net.avianlabs.solana.tweetnacl"
//    }
    export(libs.ktorClientCore)
//    export(libs.ktorClientDarwin)
    export(libs.ktorHttp)
    export(libs.ktorEvents)
    export(libs.ktorIo)
    export(libs.coroutinesCore)
  }

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
        implementation(libs.skie.configurationAnnotations)
        implementation(libs.kotlinLogging)
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

    val linuxMain by getting {
      dependencies {
        implementation(libs.ktorClientCio)
      }
    }

    val mingwMain by getting {
      dependencies {
        implementation(libs.ktorClientWinHttp)
      }
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
  distributionMode { remote("https://github.com/avianlabs/solana-kotlin/releases/download/$version") }
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
