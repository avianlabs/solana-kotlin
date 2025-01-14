package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.PublicKeyBase58Serializer
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.Response.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * Returns all SPL Token accounts by the given owner.
 *
 * @param account The account to query for token accounts.
 * @param mint The mint to filter token accounts by.
 * @param commitment The block commitment to query.
 */
public suspend fun SolanaClient.getTokenAccountsByOwnerAndMint(
  account: PublicKey,
  mint: PublicKey,
  commitment: Commitment? = null,
): Response<RPC<List<TokenAccountsInfo>>> = getTokenAccountsByOwner(
  account = account,
  mint = mint,
  programId = null,
  commitment = commitment,
)

/**
 * Returns all SPL Token accounts by the given owner.
 *
 * @param account The account to query for token accounts.
 * @param programId The program ID to filter token accounts by.
 * @param commitment The block commitment to query.
 */
public suspend fun SolanaClient.getTokenAccountsByOwnerAndProgramId(
  account: PublicKey,
  programId: PublicKey,
  commitment: Commitment? = null,
): Response<RPC<List<TokenAccountsInfo>>> = getTokenAccountsByOwner(
  account = account,
  mint = null,
  programId = programId,
  commitment = commitment,
)

private suspend fun SolanaClient.getTokenAccountsByOwner(
  account: PublicKey,
  mint: PublicKey?,
  programId: PublicKey?,
  commitment: Commitment? = null,
): Response<RPC<List<TokenAccountsInfo>>> = invoke(
  method = "getTokenAccountsByOwner",
  params = buildJsonArray {
    add(account.toBase58())
    addJsonObject {
      mint?.let { put("mint", mint.toBase58()) }
      programId?.let { put("programId", programId.toBase58()) }
    }
    addJsonObject {
      put("encoding", "jsonParsed")
      commitment?.let {
        put("commitment", it.value)
      }
    }
  }
)

@Serializable
public data class TokenAccountsInfo(
  @Serializable(with = PublicKeyBase58Serializer::class)
  public val pubkey: PublicKey,
  public val account: Account,
) {

  @Serializable
  public data class Account(
    public val data: Data,
    public val executable: Boolean,
    @Serializable(with = PublicKeyBase58Serializer::class)
    public val owner: PublicKey,
    public val rentEpoch: ULong,
    public val space: Long,
  )

  @Serializable
  public data class Data(
    public val parsed: Parsed,
    public val program: String,
    public val space: Long,
  )

  @Serializable
  public data class Parsed(
    public val info: Info,
    public val type: String,
  )

  @Serializable
  public data class Info(
    public val isNative: Boolean,
    @Serializable(with = PublicKeyBase58Serializer::class)
    public val mint: PublicKey,
    @Serializable(with = PublicKeyBase58Serializer::class)
    public val owner: PublicKey,
    public val state: String,
    public val tokenAmount: TokenAmount,
  )

  @Serializable
  public data class TokenAmount(
    public val amount: String,
    public val decimals: Int,
    public val uiAmount: Double,
    public val uiAmountString: String,
  )
}
