import co.touchlab.cklib.gradle.CompileToBitcode.Language
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.cklib)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
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

  iosArm64()
  iosSimulatorArm64()

  mingwX64()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.ktorClientOkHttp)
        implementation(libs.tweetNaClJava)
        implementation(libs.bouncyCastle)
      }
    }
    val jvmTest by getting

    val commonMain by getting {
      dependencies {
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

    targets.withType<KotlinNativeTarget> {
      val main by compilations.getting

      main.cinterops {
        create("tweetnacl") {
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
  publishToMavenCentral(SonatypeHost.DEFAULT)
  signAllPublications()
}
