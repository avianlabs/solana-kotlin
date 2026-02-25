# CLAUDE.md

## Build & Test

```bash
./gradlew build              # Full build + tests
./gradlew check              # All checks
./gradlew allTests           # Aggregated test report
./gradlew jvmTest            # JVM tests only (fastest)
./gradlew iosSimulatorArm64Test
./gradlew linuxX64Test
./gradlew mingwX64Test
```

Integration tests (`RPCIntegrationTest`) are `@Ignore`d by default — they require a local `solana-test-validator` running.

## Project Structure

Three Gradle modules under `net.avianlabs.solana`:

| Module | Purpose |
|--------|---------|
| `solana-kotlin` | Core: RPC client, transaction building, domain models, programs |
| `tweetnacl-multiplatform` | Ed25519 crypto via TweetNaCl (JVM jar + native cinterop) |
| `solana-kotlin-arrow-extensions` | Optional Arrow `Either`-based error handling wrappers |

KMP targets: **JVM** (17+), **iosArm64**, **iosSimulatorArm64**, **linuxX64**, **mingwX64**.

Dependency versions are managed centrally in `libs.versions.toml`.

## Code Style

Configured in `.editorconfig`:
- 2-space indent, 100-char line length, LF endings, UTF-8
- **Explicit API mode** enforced — all public declarations must have explicit `public` visibility

## Key Patterns

**RPC methods** are `suspend` extension functions on `SolanaClient`, one per file in `solana-kotlin/.../methods/`:
```kotlin
public suspend fun SolanaClient.getBalance(
  account: PublicKey,
  commitment: Commitment? = null,
): Response<RPC<Long>>
```

**Transaction building** uses a Builder pattern:
```kotlin
Transaction.Builder()
  .addInstruction(SystemProgram.transfer(from, to, lamports))
  .setRecentBlockHash(blockhash)
  .setFeePayer(keypair.publicKey)
  .build()
  .sign(keypair)
```

**Programs** (SystemProgram, TokenProgram, etc.) are singleton objects implementing `Program`, with methods returning `TransactionInstruction`.

**Serialization**: `kotlinx.serialization` for JSON-RPC payloads; `okio.Buffer` with little-endian encoding for binary transaction/instruction data.

## Gotchas

- **Memory**: Gradle needs `-Xmx3g` for native compilation (set in `gradle.properties`)
- **iOS targets**: Require macOS with Xcode; builds on other OSes skip iOS automatically (`kotlin.native.ignoreDisabledTargets=true`)
- **Explicit API**: All public symbols need explicit `public` modifier — the compiler will reject implicit visibility
- **cinterop**: `tweetnacl-multiplatform` compiles C code via CKLib plugin; `.def` files live in `src/nativeInterop/cinterop/`

## Testing

Uses `kotlin.test` (multiplatform) + JUnit 5 on JVM. Tests live in `commonTest` source sets.
