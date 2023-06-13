package net.avianlabs.solana.client

public data class ExecuteException(val error: RpcError) : Throwable(error.toString())
