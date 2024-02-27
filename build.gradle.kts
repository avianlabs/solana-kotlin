plugins {
  alias(libs.plugins.kotlinMultiplatform).apply(false)
  alias(libs.plugins.androidLib).apply(false)
}

if (rootProject.findProperty("snapshot") != "false") {
  allprojects {
    version = "$version-SNAPSHOT"
  }
}
