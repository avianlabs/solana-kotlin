package net.avianlabs.solana.client

public class RequestIdGenerator {

  private var lastIdx = 0

  public fun next(): Int {
    lastIdx += 1
    return lastIdx
  }
}
