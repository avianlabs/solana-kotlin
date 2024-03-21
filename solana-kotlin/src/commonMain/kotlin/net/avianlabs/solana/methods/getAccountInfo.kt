package net.avianlabs.solana.methods

import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

public suspend fun SolanaClient.getAccountInfo(
  publicKey: PublicKey,
  commitment: Commitment? = null,
): AccountInfo? {
  val result = invoke<RpcResponse.RPC<AccountInfo>>(
    method = "getAccountInfo",
    params = buildJsonArray {
      add(publicKey.toBase58())
      addJsonObject {
        put("encoding", "base64")
        commitment?.let {
          put("commitment", it.value)
        }
      }
    }
  )
  return result!!.value
}

/**
 * Account information
 * @param executable `true` if this account's data contains a loaded program (and is now read-only)
 * @param owner The public key of the program that owns the account
 * @param lamports The number of lamports assigned to the account
 * @param data The data held in this account
 * @param rentEpoch The epoch at which this account will next owe rent
 */
@Serializable
public data class AccountInfo(
  val executable: Boolean,
  val owner: String,
  val lamports: ULong,
  val data: List<String>,
  val rentEpoch: ULong,
) {
  public val ownerPublicKey: PublicKey
    get() = PublicKey.fromBase58(owner)

  public val dataBytes: ByteArray
    get() = data.first().decodeBase64Bytes()
}
