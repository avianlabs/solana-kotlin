package net.avianlabs.solana.codegen.generator

internal fun String.toPascalCase(): String {
  return split('_', '-')
    .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
}

internal fun String.toCamelCase(): String {
  val pascal = toPascalCase()
  return pascal.replaceFirstChar(Char::lowercaseChar)
}

internal fun String.toScreamingSnakeCase(): String {
  return replace(Regex("([a-z])([A-Z])"), "$1_$2")
    .split('_', '-')
    .joinToString("_") { it.uppercase() }
}
