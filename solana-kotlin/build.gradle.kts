import co.touchlab.cklib.gradle.CompileToBitcode.Language
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.cklib)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
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

  js(IR) {
    browser()
  }

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

    val jsMain by getting
    val jsTest by getting

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

publishing {
  repositories {
    mavenLocal()
  }
}
