package net.avianlabs.solana.domain.program

import kotlinx.serialization.Serializable
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

@Serializable
public data class ProgramDerivedAddress(val address: PublicKey, val nonce: UByte)
