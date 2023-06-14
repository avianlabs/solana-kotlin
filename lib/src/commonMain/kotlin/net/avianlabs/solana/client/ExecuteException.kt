package net.avianlabs.solana.client

public data class ExecuteException(val error: RpcError) : RuntimeException(error.toString())
