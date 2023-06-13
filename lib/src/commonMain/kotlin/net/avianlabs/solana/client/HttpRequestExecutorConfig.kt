package net.avianlabs.solana.client

import io.ktor.http.Url

public data class HttpRequestExecutorConfig(
  val baseURL: Url,
) {

  public companion object {

    public const val version: String = "2.0"
  }
}
