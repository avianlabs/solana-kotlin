plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.serializationJson)
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation(libs.okio)
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
        "solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/SystemProgram.kt",
        "solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/TokenProgram.kt",
        "solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/ComputeBudgetProgram.kt",
        "solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/AssociatedTokenProgram.kt"
    )
    
    doLast {
        logger.lifecycle("✓ Generated code is up-to-date")
    }
}
