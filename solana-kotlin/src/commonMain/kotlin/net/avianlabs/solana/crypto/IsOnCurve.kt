package net.avianlabs.solana.crypto

import net.avianlabs.solana.domain.core.PublicKey

public fun PublicKey.isOnCurve(): Boolean = defaultCryptoEngine.isOnCurve(this.bytes)
