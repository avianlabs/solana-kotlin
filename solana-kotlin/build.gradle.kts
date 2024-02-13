plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidLib)
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

  android {
    publishLibraryVariants("release")
  }

//  ios()
//  iosSimulatorArm64()

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
        implementation(libs.ktorClientCore)
        implementation(libs.ktorClientLogging)
        implementation(libs.serializationJson)
        implementation(libs.ktorClientContentNegotiation)
        implementation(libs.ktorSerializationKotlinxJson)
        implementation(libs.kermit)
        implementation(libs.okio)
        implementation(libs.khashSha256)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinTest)
        implementation(libs.coroutinesTest)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.ktorClientOkHttp)
        implementation(libs.tweetNaClJava)
        implementation(libs.bouncyCastle)
      }
    }

    val androidUnitTest by getting {
      dependencies {
      }
    }

    val jsMain by getting
    val jsTest by getting

//    val iosMain by getting {
//      dependencies {
//        implementation(libs.ktorClientDarwin)
//      }
//    }
//    val iosTest by getting {
//    }
  }
}

android {
  namespace = "net.avianlabs.solana"

  defaultConfig {
    minSdk = libs.versions.androidMinSdk.get().toInt()
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    consumerProguardFiles("src/androidMain/consumer-rules.pro")
  }

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  testOptions {
    unitTests {
      isReturnDefaultValues = true
    }
  }

  packagingOptions {
    exclude("META-INF/AL2.0")
    exclude("META-INF/LGPL2.1")
  }
//  packaging {
//    resources.excludes.addAll(
//      listOf(
//        "META-INF/AL2.0",
//        "META-INF/LGPL2.1",
//      )
//    )
//  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  add("coreLibraryDesugaring", libs.coreLibraryDesugaring)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}

mavenPublishing {
  repositories {
    mavenLocal()
  }
}
