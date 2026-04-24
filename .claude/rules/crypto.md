# cryptographic layer

## TweetNaCl interface

multiplatform crypto interface with platform-specific implementations

```kotlin
interface TweetNaCl {
    interface Signature { ... }
    interface SecretBox { ... }
}
```

## Ed25519 signatures

### generate keypair

from random seed (32 bytes):

```kotlin
val seed: ByteArray = secureRandom.nextBytes(32)
val keypair: Ed25519Keypair = TweetNaCl.Signature.generateKey(seed)
```

from base58 secret key:

```kotlin
val keypair = Ed25519Keypair.fromBase58(secretKeyString)
```

from secret key bytes:

```kotlin
// 32 bytes: seed only
val keypair = Ed25519Keypair.fromSecretKeyBytes(seed32)

// 64 bytes: seed + public key
val keypair = Ed25519Keypair.fromSecretKeyBytes(secretKey64)
```

### sign messages

```kotlin
val message: ByteArray = "hello".encodeToByteArray()
val signature: ByteArray = keypair.sign(message)
// or
val signature = TweetNaCl.Signature.sign(message, keypair.secretKey)
```

signature is 64 bytes (ed25519)

### keypair structure

```kotlin
data class Ed25519Keypair(
    val publicKey: PublicKey,
    val secretKey: ByteArray // 64 bytes
)
```

secret key format:
- bytes 0-31: seed
- bytes 32-63: public key

### keypair safety

`toString()` redacts secret key:

```kotlin
keypair.toString() 
// -> "Ed25519Keypair(publicKey=..., secretKey=*****)"
```

## PublicKey

32-byte ed25519 public key

```kotlin
data class PublicKey(val bytes: ByteArray)
```

### creation

from base58:

```kotlin
val pubkey = PublicKey.fromBase58("11111111111111111111111111111111")
```

from bytes:

```kotlin
val pubkey = PublicKey(bytes)
```

### encoding

```kotlin
val base58: String = pubkey.toBase58()
val bytes: ByteArray = pubkey.toByteArray()
```

toString() returns base58:

```kotlin
println(pubkey) // prints base58 string
```

### curve validation

check if key is on ed25519 curve:

```kotlin
val valid: Boolean = pubkey.isOnCurve()
```

pda addresses are intentionally off-curve (no private key exists)

## Base58 encoding

solana uses base58 for addresses and keys

encode:

```kotlin
val base58: String = bytes.encodeToBase58String()
```

decode:

```kotlin
val bytes: ByteArray = base58String.decodeBase58()
```

## SecretBox (xsalsa20-poly1305)

authenticated encryption

### create box

```kotlin
val secretKey: ByteArray = secureRandom.nextBytes(32)
val box = TweetNaCl.SecretBox(secretKey)
```

### encrypt

```kotlin
val message: ByteArray = "secret".encodeToByteArray()
val nonce: ByteArray = secureRandom.nextBytes(24)
val encrypted: ByteArray? = box.box(message, nonce)
```

returns null if encryption fails

### decrypt

```kotlin
val decrypted: ByteArray? = box.open(encrypted, nonce)
```

returns null if:
- authentication fails
- nonce mismatch
- corrupted ciphertext

### nonce requirements

- 24 bytes
- must be unique for each message with same key
- never reuse nonce with same key
- can be random or counter-based

## platform implementations

### jvm

uses third-party libraries:
- tweetnacl-java for ed25519
- bouncycastle for additional crypto

```kotlin
// jvmMain
actual fun signInternal(...): ByteArray {
    return TweetNaClFast.Signature(...).sign(...)
}
```

### ios/darwin

uses native commonCrypto and security framework

```kotlin
// iosMain
actual fun signInternal(...): ByteArray {
    // uses platform apis
}
```

### native (linux/windows)

uses c bindings to tweetnacl.c via cinterop

```kotlin
// nativeMain
actual fun signInternal(...): ByteArray {
    return tweetnacl_sign(...)
}
```

## constants

```kotlin
TweetNaCl.Signature.SEED_BYTES = 32
TweetNaCl.Signature.SECRET_KEY_BYTES = 64
TweetNaCl.Signature.PUBLIC_KEY_BYTES = 32
TweetNaCl.Signature.SIGNATURE_BYTES = 64

TweetNaCl.SecretBox.NONCE_BYTES = 24
TweetNaCl.SecretBox.KEY_BYTES = 32
```

## common patterns

### create new wallet

```kotlin
val seed = secureRandom.nextBytes(32)
val keypair = Ed25519Keypair.fromSecretKeyBytes(seed)
val address = keypair.publicKey.toBase58()
```

### import wallet from secret key

```kotlin
val keypair = Ed25519Keypair.fromBase58(secretKeyBase58)
```

### sign transaction

```kotlin
val tx: Transaction = ...
val signer: Signer = keypair // Signer is typealias for Ed25519Keypair
val signed = tx.sign(signer)
```

### verify key ownership

```kotlin
// sign challenge
val challenge = "prove ownership".encodeToByteArray()
val signature = keypair.sign(challenge)

// verify (external verification needed)
// solana uses ed25519_verify on-chain
```

## security considerations

### seed generation

use cryptographically secure random:

```kotlin
// DO THIS
val seed = SecureRandom().generateSeed(32)

// NOT THIS
val seed = Random.nextBytes(32) // predictable!
```

### secret key storage

- never log secret keys
- encrypt at rest
- use platform keychains when possible
- clear from memory after use

### nonce generation

for secretbox, use secure random:

```kotlin
val nonce = SecureRandom().generateSeed(24)
```

or maintain counter (never reuse)

## testing

test keypair with known seed:

```kotlin
val seed = ByteArray(32) { it.toByte() }
val keypair = TweetNaCl.Signature.generateKey(seed)
// deterministic public key
```

## integration with solana

### Signer type

```kotlin
typealias Signer = Ed25519Keypair
```

seamless integration with transaction signing

### PublicKey compatibility

tweetnacl `PublicKey` used throughout:

```kotlin
val pubkey: net.avianlabs.solana.tweetnacl.ed25519.PublicKey = ...
SystemProgram.transfer(fromPublicKey = pubkey, ...)
```

### signature format

ed25519 signatures are 64 bytes, directly compatible with solana's signature format

no additional encoding needed
