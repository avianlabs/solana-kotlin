plugins {
  alias(libs.plugins.kotlinMultiplatform).apply(false)
  alias(libs.plugins.androidLib).apply(false)
  alias(libs.plugins.nmcp)
}

nmcp {
  publishAllProjectsProbablyBreakingProjectIsolation {
    username = findProperty("mavenCentralUsername") as? String
    password = findProperty("mavenCentralPassword") as? String
    publicationType = "AUTOMATIC"
  }
}
