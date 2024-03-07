package net.avianlabs.solana.crypto

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.crypto.PublicKey

public fun PublicKey.isOnCurve(): Boolean = TweetNaCl.Signature.isOnCurve(this.bytes)
