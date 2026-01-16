plugins {
  alias(libs.plugins.kotlinMultiplatform)
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

  jvm {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
  }

  mingwX64()
  linuxX64()

  sourceSets {
    val jvmMain by getting

    val jvmTest by getting

    val commonMain by getting {
      dependencies {
        api(project(":solana-kotlin"))
        implementation(libs.coroutinesCore)
        implementation(libs.kermit)
        implementation(libs.arrow.core)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
    }

    val nativeMain by getting

    val linuxMain by getting

    val mingwMain by getting
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
