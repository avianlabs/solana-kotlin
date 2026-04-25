plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLib)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
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
    namespace = "net.avianlabs.solana.arrow"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    minSdk = libs.versions.androidMinSdk.get().toInt()
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    withHostTestBuilder { }
  }

  mingwX64()
  linuxX64()

  sourceSets {
    commonMain {
      dependencies {
        api(project(":solana-kotlin"))
        implementation(libs.coroutinesCore)
        implementation(libs.kermit)
        implementation(libs.arrow.core)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
    }
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
        name = "Solana Kotlin Arrow Extensions"
        description = "Arrow extensions for Solana Kotlin"
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
