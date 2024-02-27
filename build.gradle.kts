plugins {
  alias(libs.plugins.kotlinMultiplatform).apply(false)
  alias(libs.plugins.androidLib).apply(false)
  alias(libs.plugins.nmcp)
}

if (rootProject.findProperty("snapshot") != "false") {
  allprojects {
    version = "$version-SNAPSHOT"
  }
}

nmcp {
  publishAllProjectsProbablyBreakingProjectIsolation {
    username = findProperty("mavenCentralUsername") as? String
    password = findProperty("mavenCentralPassword") as? String
    publicationType = "AUTOMATIC"
  }
}
