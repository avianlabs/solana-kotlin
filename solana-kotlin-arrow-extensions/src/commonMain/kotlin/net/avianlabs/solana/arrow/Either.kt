package net.avianlabs.solana.arrow

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import net.avianlabs.solana.arrow.SolanaKotlinError.RpcError.*
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.RpcError

public fun <T> Response<T>.toEither(): Either<SolanaKotlinError, T> =
  if (error != null) {
    error!!.toRpcError().left()
  } else if (result != null) {
    result!!.right()
  } else {
    SolanaKotlinError.MalformedResponse("both error and result are null").left()
  }

private fun RpcError.toRpcError(): SolanaKotlinError = when (code) {
  -32700 -> ParseError(message)
  -32600 -> InvalidRequest(message)
  -32601 -> MethodNotFound(message)
  -32602 -> InvalidParams(message)
  -32603 -> InternalError(message)
  in -32000 downTo -32099 -> ServerError(message)
  else -> SolanaKotlinError.UnknownError(message)
}
