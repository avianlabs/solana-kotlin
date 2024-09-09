package net.avianlabs.solana.arrow

public sealed interface SolanaKotlinError {
  public sealed interface RpcError : SolanaKotlinError {
    public data class ParseError(val message: String) : RpcError
    public data class InvalidRequest(val message: String) : RpcError
    public data class MethodNotFound(val message: String) : RpcError
    public data class InvalidParams(val message: String) : RpcError
    public data class InternalError(val message: String) : RpcError
    public data class ServerError(val message: String) : RpcError
  }

  public data class MalformedResponse(val message: String) : SolanaKotlinError
  public data class UnknownError(val message: String) : SolanaKotlinError
}
