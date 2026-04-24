# rpc client patterns

## entry point

`SolanaClient` is the main interface to the solana rpc api

```kotlin
val client = SolanaClient(
    client = RpcKtorClient(url = "https://api.mainnet-beta.solana.com"),
    headerProviders = mapOf()
)
```

ios-friendly constructor with callback-based auth:

```kotlin
val client = SolanaClient(
    url = "https://api.mainnet-beta.solana.com",
    authorizationHeaderProvider = { completion ->
        // fetch token async
        completion(token)
    }
)
```

## rpc method pattern

all rpc methods are suspend extension functions on `SolanaClient` in the `methods/` package

```kotlin
suspend fun SolanaClient.getBalance(
    publicKey: PublicKey,
    commitment: Commitment = Commitment.Finalized
): Response<Long> = invoke(
    method = "getBalance",
    params = buildJsonArray {
        add(publicKey.toBase58())
        add(buildJsonObject {
            put("commitment", commitment.value)
        })
    }
)
```

pattern breakdown:
1. suspend function (all rpc calls are async)
2. extension on `SolanaClient`
3. strongly-typed params
4. returns `Response<T>` wrapper
5. calls internal `invoke<T>()` with json params

## available methods

- `getAccountInfo` - fetch account data
- `getBalance` - get lamport balance
- `getLatestBlockhash` - fetch recent blockhash for tx
- `getRecentBlockhash` - deprecated, use getLatestBlockhash
- `getFeeForMessage` - estimate transaction fee
- `getMinimumBalanceForRentExemption` - rent exemption amount
- `getNonce` - fetch durable nonce account
- `getSignaturesForAddress` - transaction history
- `getTokenAccountBalance` - spl token balance
- `getTransaction` - fetch transaction details
- `isBlockHashValid` - check if blockhash is still valid
- `requestAirdrop` - devnet/testnet only
- `sendTransaction` - submit signed transaction
- `simulateTransaction` - dry-run transaction

## response handling

`Response<T>` is a sealed wrapper:

```kotlin
data class Response<T>(
    val result: T?,
    val error: RpcError?
)
```

check error first:

```kotlin
val response = client.getBalance(publicKey)
if (response.error != null) {
    // handle error
} else {
    val balance = response.result!!
}
```

## error types

`RpcError` contains:

```kotlin
data class RpcError(
    val code: Int,
    val message: String
)
```

standard json-rpc error codes:
- -32700: parse error
- -32600: invalid request
- -32601: method not found
- -32602: invalid params
- -32603: internal error
- -32000 to -32099: server errors

## arrow extensions

for functional error handling, use arrow-extensions module:

```kotlin
val result: Either<SolanaKotlinError, Long> = 
    client.getBalance(publicKey).toEither()

result.fold(
    ifLeft = { error -> /* handle error */ },
    ifRight = { balance -> /* use balance */ }
)
```

## implementation details

### ktor http client

- uses ktor for multiplatform http
- platform-specific engines (okhttp for jvm, darwin for ios, etc)
- json content negotiation via kotlinx.serialization
- configurable logging level

### request/response flow

1. `SolanaClient.invoke<T>()` - type-safe wrapper
2. `RpcKtorClient.invoke()` - builds json-rpc request
3. `RpcRequest` - request id generation
4. `RpcInvocation` - method + params + headers
5. ktor post to endpoint
6. deserialize `Response<T>`

### request id generation

sequential ids via `RequestIdGenerator`:

```kotlin
internal class RequestIdGenerator {
    private val counter = AtomicInt(0)
    fun next(): RequestId = counter.incrementAndGet()
}
```

## adding new methods

1. create file in `methods/` package
2. define suspend extension function
3. build json params with `buildJsonArray`
4. call `invoke<ReturnType>(method, params)`
5. return `Response<T>`

example:

```kotlin
suspend fun SolanaClient.myMethod(
    param: String
): Response<MyResult> = invoke(
    method = "myMethod",
    params = buildJsonArray {
        add(param)
    }
)
```
