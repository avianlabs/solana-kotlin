import java.net.URI

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.serializationJson)
    implementation(libs.kotlinPoet)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit5api)
    testRuntimeOnly(libs.junit5Engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("syncIdl") {
    group = "codegen"
    description = "Download latest IDL files from Solana program repos"
    
    doLast {
        val idlDir = file("idl")
        idlDir.mkdirs()
        
        val idls = mapOf(
            "system.json" to "https://raw.githubusercontent.com/solana-program/system/main/program/idl.json",
            "token.json" to "https://raw.githubusercontent.com/solana-program/token/main/program/idl.json",
            "compute-budget.json" to "https://raw.githubusercontent.com/solana-program/compute-budget/main/program/idl.json"
        )
        
        idls.forEach { (filename, url) ->
            val outputFile = idlDir.resolve(filename)
            logger.lifecycle("Downloading $filename from $url")
            
            URI(url).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                logger.lifecycle("  ✓ Downloaded ${outputFile.length()} bytes")
            } else {
                throw GradleException("Failed to download $filename")
            }
        }
    }
}

tasks.register("generateSolanaCode", JavaExec::class) {
    group = "codegen"
    description = "Generate Solana program code from IDL files"
    
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.avianlabs.solana.codegen.GenerateProgramsKt")
    workingDir = rootProject.projectDir
}

tasks.register<Exec>("checkGeneratedCode") {
    group = "verification"
    description = "Verify that generated Solana code is up-to-date"
    
    dependsOn("generateSolanaCode")
    
    commandLine("git", "diff", "--exit-code",
        "solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/"
    )
    
    doLast {
        logger.lifecycle("✓ Generated code is up-to-date")
    }
}
