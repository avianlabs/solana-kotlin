import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.DefaultArgumentInterop

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLib)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
  alias(libs.plugins.multiplatform.swiftpackage)
  alias(libs.plugins.skie)
}

group = "net.avianlabs.solana"
version = properties["version"] as String

kotlin {
  applyDefaultHierarchyTemplate()
  explicitApi()
  jvmToolchain(17)

  jvm()

  @Suppress("DEPRECATION") // AGP 9.x: 'android' overload is ambiguous with KGP's; androidLibrary still works
  androidLibrary {
    namespace = "net.avianlabs.solana"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    minSdk = libs.versions.androidMinSdk.get().toInt()
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    withHostTestBuilder { }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      binaryOption("bundleId", "net.avianlabs.solana")
      binaryOption("bundleVersion", version.toString())
      baseName = "SolanaKotlin"
      export(project(":tweetnacl-multiplatform"))
      isStatic = true
    }
  }

  mingwX64()
  linuxX64()

  sourceSets {
    val jvmAndroidMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.ktorClientOkHttp)
        implementation(libs.bouncyCastle)
      }
    }
    jvmMain { dependsOn(jvmAndroidMain) }
    androidMain { dependsOn(jvmAndroidMain) }

    commonMain {
      kotlin.srcDir("src/commonMain/generated")
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
    commonTest {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.ktorClientDarwin)
      }
    }

    linuxMain {
      dependencies {
        implementation(libs.ktorClientCio)
      }
    }

    mingwMain {
      dependencies {
        implementation(libs.ktorClientWinHttp)
      }
    }
  }
}

skie {
  features {
    group("net.avianlabs.solana") {
      DefaultArgumentInterop.Enabled(true)
    }
    group("net.avianlabs.solana.tweetnacl") {
      ClassInterop.CInteropFrameworkName("TweetNaClMultiplatform")
      DefaultArgumentInterop.Enabled(true)
    }
  }
  build {
    produceDistributableFramework()
  }
}

multiplatformSwiftPackage {
  swiftToolsVersion("5.10")
  targetPlatforms {
    iOS { v("17") }
  }
  packageName("SolanaKotlin")
  zipFileName("SolanaKotlin")
  distributionMode { remote("https://github.com/avianlabs/solana-kotlin/releases/download/$version") }
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
