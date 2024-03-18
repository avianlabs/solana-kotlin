import co.touchlab.cklib.gradle.CompileToBitcode.Language
import co.touchlab.skie.configuration.ClassInterop
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.cklib)
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
      baseName = "TweetNaClMultiplatform"
      isStatic = true
    }
  }

  mingwX64()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.tweetNaClJava)
        implementation(libs.bouncyCastle)
      }
    }
    val jvmTest by getting

    val commonMain by getting {
      dependencies {
        implementation(libs.serializationCore)
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
      }
    }
    val iosTest by getting {
    }

    val nativeMain by getting {
    }

    targets.withType<KotlinNativeTarget> {
      val main by compilations.getting

      main.cinterops {
        create("TweetNaCl") {
          header(file("vendor/tweetnacl/tweetnacl.h"))
          packageName("net.avianlabs.solana.tweetnacl")
        }
      }
    }
  }
}

cklib {
  config.kotlinVersion = libs.versions.kotlin.get()
  create("tweetnacl") {
    language = Language.C
    srcDirs = project.files(file("vendor/tweetnacl"))
    compilerArgs.addAll(
      listOf(
        "-DKONAN_MI_MALLOC=1",
        "-Wno-unknown-pragmas",
        "-Wno-unused-function",
        "-Wno-error=atomic-alignment",
        "-Wno-sign-compare",
        "-Wno-unused-parameter" /* for windows 32 */
      )
    )
  }
}

multiplatformSwiftPackage {
  swiftToolsVersion("5.9")
  targetPlatforms {
    iOS { v("16") }
  }
  packageName("TweetNaClMultiplatform")
  distributionMode { local() }
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
        name = "TweetNaCl Multiplatform"
        description = "Kotlin Multiplatform bindings for TweetNaCl"
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
