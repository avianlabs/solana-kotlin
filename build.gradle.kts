plugins {
  alias(libs.plugins.kotlinMultiplatform).apply(false)
  alias(libs.plugins.androidKotlinMultiplatformLib).apply(false)
  alias(libs.plugins.binaryCompatibilityValidator)
  alias(libs.plugins.dokka)
  alias(libs.plugins.nmcp)
}

if (rootProject.findProperty("snapshot") == "true") {
  allprojects {
    version = "$version-SNAPSHOT"
  }
}

apiValidation {
  ignoredProjects += listOf("codegen")
  @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
  klib {
    enabled = true
  }
}

dokka {
  dokkaPublications.html {
    moduleName.set(rootProject.name)
    outputDirectory.set(layout.buildDirectory.dir("docs/html"))
  }
}

dependencies {
  dokka(project(":solana-kotlin"))
  dokka(project(":tweetnacl-multiplatform"))
  dokka(project(":solana-kotlin-arrow-extensions"))
}

nmcp {
  publishAllProjectsProbablyBreakingProjectIsolation {
    username = findProperty("mavenCentralUsername") as? String
    password = findProperty("mavenCentralPassword") as? String
    publicationType = "AUTOMATIC"
  }
}
