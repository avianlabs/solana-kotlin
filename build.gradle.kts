plugins {
  alias(libs.plugins.kotlinMultiplatform).apply(false)
  alias(libs.plugins.androidLib).apply(false)
  alias(libs.plugins.binaryCompatibilityValidator)
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

nmcp {
  publishAllProjectsProbablyBreakingProjectIsolation {
    username = findProperty("mavenCentralUsername") as? String
    password = findProperty("mavenCentralPassword") as? String
    publicationType = "AUTOMATIC"
  }
}
