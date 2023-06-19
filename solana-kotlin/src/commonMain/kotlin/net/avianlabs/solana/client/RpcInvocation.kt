package net.avianlabs.solana.client

public data class RpcInvocation<T>(
  public val method: String,
  public val params: T?,
)
