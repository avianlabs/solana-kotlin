package net.avianlabs.solana.domain.program

import io.ktor.utils.io.core.*
import net.avianlabs.solana.crypto.isOnCurve
import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import org.komputing.khash.sha256.Sha256

public abstract class Program(
  public val programId: PublicKey,
) {

  public companion object {

    public fun createTransactionInstruction(
      programId: PublicKey,
      keys: List<AccountMeta>,
      data: ByteArray,
    ): TransactionInstruction = TransactionInstruction(programId, keys, data)

    public fun findProgramAddress(
      seeds: List<ByteArray>,
      programId: PublicKey,
    ): ProgramDerivedAddress {
      var nonce = 255
      val address: PublicKey
      val seedsWithNonce = mutableListOf<ByteArray>()
      seedsWithNonce.addAll(seeds)
      while (nonce != 0) {
        address = try {
          seedsWithNonce.add(byteArrayOf(nonce.toByte()))
          createProgramAddress(seedsWithNonce, programId)
        } catch (e: Exception) {
          seedsWithNonce.removeAt(seedsWithNonce.size - 1)
          nonce--
          continue
        }
        return ProgramDerivedAddress(address, nonce)
      }
      throw Exception("Unable to find a viable program address nonce")
    }

    public fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
      val bytes = (seeds + programId.bytes + "ProgramDerivedAddress".toByteArray()).let {
        // join into one single byte array
        val buffer = ByteArray(it.sumOf { it.size })
        var offset = 0
        for (seed in it) {
          seed.copyInto(buffer, offset)
          offset += seed.size
        }
        buffer
      }
      val hash = Sha256.digest(bytes)

      if (PublicKey(hash).isOnCurve()) {
        throw RuntimeException("Invalid seeds, address must fall off the curve")
      }
      return PublicKey(hash)
    }
  }

}
