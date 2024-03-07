package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.Sha256
import okio.Buffer

public interface Program {

  public val programId: PublicKey

  public companion object {

    internal fun createTransactionInstruction(
      programId: PublicKey,
      keys: List<AccountMeta>,
      data: ByteArray,
    ): TransactionInstruction = TransactionInstruction(programId, keys, data)

    internal fun findProgramAddress(
      seeds: List<ByteArray>,
      programId: PublicKey,
    ): ProgramDerivedAddress {
      val address: PublicKey
      var bumpSeed = UByte.MAX_VALUE
      while (bumpSeed > 0.toUByte()) {
        address = try {
          createProgramAddress(seeds + byteArrayOf(bumpSeed.toByte()), programId)
        } catch (e: Exception) {
          bumpSeed--
          continue
        }
        return ProgramDerivedAddress(address, bumpSeed)
      }
      throw Exception("Unable to find a viable program address nonce")
    }

    internal fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
      if (seeds.size > 16) {
        throw RuntimeException("max seed length exceeded: ${seeds.size}")
      }
      seeds.forEach { seed ->
        if (seed.size > 32) {
          throw RuntimeException("max seed length exceeded: ${seed.size}")
        }
      }

      val bytes = Buffer()
        .apply {
          seeds.forEach { seed ->
            write(seed)
          }
        }
        .write(programId.bytes)
        .write("ProgramDerivedAddress".encodeToByteArray())
        .readByteArray()

      val hash = Sha256.digest(bytes)

      if (PublicKey(hash).isOnCurve()) {
        throw RuntimeException("Invalid seeds, address must fall off the curve")
      }
      return PublicKey(hash)
    }
  }
}
