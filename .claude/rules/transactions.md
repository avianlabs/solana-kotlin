# transaction building

## core types

### Transaction

immutable transaction with unsigned message

```kotlin
val tx = Transaction.Builder()
    .addInstruction(instruction)
    .setRecentBlockHash(blockhash)
    .setFeePayer(payer)
    .build()
```

### Message

contains the actual transaction data:
- `feePayer: PublicKey?` - pays transaction fees
- `recentBlockHash: String?` - recent blockhash for expiry
- `accountKeys: List<AccountMeta>` - all accounts involved
- `instructions: List<TransactionInstruction>` - program calls

builder pattern:

```kotlin
val message = Message.Builder()
    .setFeePayer(payer)
    .setRecentBlockHash(blockhash)
    .addInstruction(instruction)
    .build()
```

### SignedTransaction

transaction with signatures, ready to send

```kotlin
val signedTx: SignedTransaction = transaction.sign(signer)
```

properties:
- `originalMessage: Message` - the message that was signed
- `signedMessage: ByteArray` - serialized message bytes
- `signatures: Map<PublicKey, ByteArray>` - ed25519 signatures

## signing workflow

### single signer

```kotlin
val signer: Signer = Ed25519Keypair.fromBase58(secretKey)
val signedTx = transaction.sign(signer)
```

### multiple signers

```kotlin
val signers = listOf(payer, authority, additionalSigner)
val signedTx = transaction.sign(signers)
```

first signer becomes fee payer if not explicitly set:

```kotlin
// fee payer defaults to signers.first()
val tx = Transaction.Builder()
    .addInstruction(instruction)
    .setRecentBlockHash(blockhash)
    .build()
val signedTx = tx.sign(listOf(payer, ...))
// payer is now the fee payer
```

### additional signatures

add more signatures to already-signed transaction:

```kotlin
val partialSigned = transaction.sign(payer)
val fullySigned = partialSigned.sign(authority)
```

## serialization

### serialize for sending

```kotlin
val bytes: SerializedTransaction = signedTx.serialize()
val base64 = bytes.encodeBase64()
client.sendTransaction(signedTx)
```

### serialize with null signatures

useful for simulation:

```kotlin
val bytes = signedTx.serialize(includeNullSignatures = true)
```

format:
1. short-vec encoded signature count
2. 64-byte signatures (or zeros if null)
3. serialized message

## message serialization

internal serialization via `SerializeMessage.kt`:

uses okio `Buffer` for binary encoding:
- account keys with deduplication
- recent blockhash as base58 bytes
- instructions with program id indices

## account meta handling

`AccountMeta` specifies account permissions:

```kotlin
data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)
```

accounts are normalized in message building:
- signers come first
- writables sorted
- duplicates merged (most permissive wins)

## transaction instruction

program call specification:

```kotlin
data class TransactionInstruction(
    val keys: List<AccountMeta>,
    val programId: PublicKey,
    val data: ByteArray
)
```

typically created via program helpers:

```kotlin
val instruction = SystemProgram.transfer(
    fromPublicKey = sender,
    toPublicKey = recipient,
    lamports = 1_000_000
)
```

## builder pattern details

### newBuilder from existing

modify existing transaction:

```kotlin
val modified = transaction.newBuilder()
    .addInstruction(newInstruction)
    .build()
```

preserves existing state (feePayer, blockhash, instructions)

### message builder

internal use, constructs normalized account list:

```kotlin
Message.Builder()
    .setFeePayer(payer)
    .setRecentBlockHash(blockhash)
    .addInstruction(ix)
    .build()
```

normalizes accounts on `build()`:
- deduplicates by public key
- sorts by signer/writable flags
- fee payer always first

## common patterns

### simple transfer

```kotlin
val tx = Transaction.Builder()
    .addInstruction(
        SystemProgram.transfer(from, to, lamports)
    )
    .setRecentBlockHash(blockhash)
    .setFeePayer(from)
    .build()
    .sign(fromKeypair)

client.sendTransaction(tx)
```

### token transfer

```kotlin
val tx = Transaction.Builder()
    .addInstruction(
        TokenProgram.transfer(
            source = sourceTokenAccount,
            destination = destTokenAccount,
            owner = ownerPublicKey,
            amount = 1000u
        )
    )
    .setRecentBlockHash(blockhash)
    .setFeePayer(ownerPublicKey)
    .build()
    .sign(ownerKeypair)
```

### multi-instruction

```kotlin
val tx = Transaction.Builder()
    .addInstruction(ix1)
    .addInstruction(ix2)
    .addInstruction(ix3)
    .setRecentBlockHash(blockhash)
    .setFeePayer(payer)
    .build()
    .sign(signers)
```

## type safety

### Signer type alias

```kotlin
typealias Signer = Ed25519Keypair
```

keeps api clean while maintaining type compatibility

### consistent copy visibility

`@ConsistentCopyVisibility` prevents uncontrolled copying:

```kotlin
@ConsistentCopyVisibility
data class SignedTransaction internal constructor(...)
```

use `newBuilder()` instead of `copy()`

## blockhash management

blockhashes expire after ~2 minutes

get fresh blockhash:

```kotlin
val response = client.getLatestBlockhash()
val blockhash = response.result!!.blockhash
```

check validity:

```kotlin
val valid = client.isBlockHashValid(blockhash, commitment).result!!
```

## error handling

transaction building never throws, but:
- signing requires valid keypair (64 bytes)
- serialization requires all signers present
- sending requires valid blockhash and signatures

validate before sending:

```kotlin
val simResult = client.simulateTransaction(signedTx)
if (simResult.error != null) {
    // transaction would fail
}
```
