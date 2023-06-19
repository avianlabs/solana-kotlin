package net.avianlabs.solana.client

import kotlinx.serialization.Serializable

@Serializable
public data class RpcResponse<T>(
  val id: Int,
  val jsonrpc: String,
  val result: T? = null,
  val error: RpcError? = null,
) {
  @Serializable
  public data class RPC<T>(
    val context: Context?,
    val value: T? = null,
  ) {

    @Serializable
    public data class Context(
      val slot: Long,
      val apiVersion: String?,
    )
  }
}
