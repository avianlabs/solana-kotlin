package net.avianlabs.solana.client

import kotlinx.serialization.Serializable

@Serializable
public data class Response<T>(
  val id: Int,
  val jsonrpc: String,
  val result: T? = null,
  val error: RpcError? = null,
) {
  @Serializable
  public data class RPC<T>(
    val context: Context,
    val value: T,
  ) {

    @Serializable
    public data class Context(
      val slot: ULong,
      val apiVersion: String?,
    )
  }
}
