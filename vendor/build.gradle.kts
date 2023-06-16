plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

group = "net.avianlabs.solana"

kotlin {
  targetHierarchy.default()
  explicitApi()

  jvm {
    withJava()
    // set the target JVM version
    compilations.all {
      kotlinOptions {
        jvmTarget = "18"
      }
    }
  }

  ios()
  iosSimulatorArm64()

  js(IR) {
    browser()
  }

  sourceSets {
    val jvmMain by getting
    val jvmTest by getting

    val commonMain by getting
    val commonTest by getting

    val jsMain by getting
    val jsTest by getting

    val iosMain by getting
    val iosTest by getting
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "18"
  }
}
