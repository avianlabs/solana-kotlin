# solana-kotlin

### Usage

Add it in your root build.gradle.kts at the end of repositories:

```
repositories {
  mavenCentral()
}

dependencies {
  implementation("net.avianlabs.solana:solana-kotlin:<version>")
}
```

### Snapshot releases

You can get snapshot releases from [GitHub Packages](https://github.com/orgs/avianlabs/packages?repo_name=solana-kotlin).

```
repositories {
  maven {
    url = uri("https://maven.pkg.github.com/avianlabs/solana-kotlin")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
    }
  }
}
```
