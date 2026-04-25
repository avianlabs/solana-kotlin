import co.touchlab.cklib.gradle.CompileToBitcode.Language
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLib)
  alias(libs.plugins.cklib)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.dokka)
  signing
  alias(libs.plugins.skie)
}

group = "net.avianlabs.solana"
version = properties["version"] as String

kotlin {
  applyDefaultHierarchyTemplate()
  explicitApi()
  jvmToolchain(21)

  jvm()

  @Suppress("DEPRECATION") // AGP 9.x: 'android' overload is ambiguous with KGP's; androidLibrary still works
  androidLibrary {
    namespace = "net.avianlabs.solana.tweetnacl"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    minSdk = libs.versions.androidMinSdk.get().toInt()
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    withHostTestBuilder { }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "TweetNaClMultiplatform"
      binaryOption("bundleId", "net.avianlabs.solana.tweetnacl")
      binaryOption("bundleVersion", version.toString())
      isStatic = true
    }
  }

  mingwX64()
  linuxX64()

  sourceSets {
    val jvmAndroidMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.tweetNaClJava)
        implementation(libs.bouncyCastle)
      }
    }
    jvmMain { dependsOn(jvmAndroidMain) }
    androidMain { dependsOn(jvmAndroidMain) }

    commonMain {
      dependencies {
        implementation(libs.skie.configurationAnnotations)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
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

dokka {
  dokkaSourceSets.configureEach {
    documentedVisibilities.set(setOf(org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public))
    skipEmptyPackages.set(true)
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
