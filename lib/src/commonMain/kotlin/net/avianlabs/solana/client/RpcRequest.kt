package net.avianlabs.solana.client

public data class RpcRequest<T>(
  val id: Int? = null,
  val invocation: RpcInvocation<T>,
)
