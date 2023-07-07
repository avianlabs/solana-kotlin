package net.avianlabs.solana.domain.program

import kotlinx.serialization.Serializable
import net.avianlabs.solana.domain.core.PublicKey

@Serializable
public data class ProgramDerivedAddress(val address: PublicKey, val nonce: UByte)
