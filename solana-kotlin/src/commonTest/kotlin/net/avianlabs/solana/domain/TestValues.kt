package net.avianlabs.solana.domain

import net.avianlabs.solana.tweetnacl.TweetNaCl
import kotlin.random.Random

fun randomKey() = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
