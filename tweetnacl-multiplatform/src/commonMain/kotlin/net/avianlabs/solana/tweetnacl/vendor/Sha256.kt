package net.avianlabs.solana.tweetnacl.vendor

/**
 * Digest Class for SHA-256.
 * Original Java version at https://github.com/meyfa/java-sha256/blob/master/src/main/java/net/meyfa/sha256/Sha256.java
 */
public object Sha256 {

  private val K = intArrayOf(
    0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b, 0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
    -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
    -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039, -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, -0x7e3d36d2, -0x6d8dd37b,
    -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d, -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf8, -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e
  )

  private val H0 = intArrayOf(
    0x6a09e667, -0x4498517b, 0x3c6ef372, -0x5ab00ac6,
    0x510e527f, -0x64fa9774, 0x1f83d9ab, 0x5be0cd19
  )

  /**
   * Hashes the given message with SHA-256 and returns the digest.
   *
   * @param message The bytes to digest.
   * @return The digest's bytes.
   */
  public fun digest(message: ByteArray): ByteArray {
    // wiyarmir: Moved the arrays inside the function to avoid the need to synchronize them
    // Working arrays
    val W = IntArray(64)
    val H = IntArray(8)
    val TEMP = IntArray(8)

    // Let H = H0
    H0.copy(0,
      H, 0, H0.size)

    // Initialize all words
    val words = padMessage(message).toIntArray()

    // Enumerate all blocks (each containing 16 words)
    var i = 0
    val n = words.size / 16
    while (i < n) {

      // initialize W from the block's words
      words.copy(i * 16, W, 0, 16)
      for (t in 16 until W.size) {
        W[t] = (smallSig1(W[t - 2]) + W[t - 7] + smallSig0(
          W[t - 15]
        ) + W[t - 16])
      }

      // Let TEMP = H
      H.copy(0,
        TEMP, 0, H.size)

      // Operate on TEMP
      for (t in W.indices) {
        val t1 = (TEMP[7] + bigSig1(TEMP[4]) + ch(
          TEMP[4],
          TEMP[5],
          TEMP[6]
        ) + K[t] + W[t])
        val t2 = bigSig0(TEMP[0]) + maj(
          TEMP[0],
          TEMP[1],
          TEMP[2]
        )
        TEMP.copy(0,
          TEMP, 1, TEMP.size - 1)
        TEMP[4] += t1
        TEMP[0] = t1 + t2
      }

      // Add values in TEMP to values in H
      for (t in H.indices) {
        H[t] += TEMP[t]
      }

      ++i
    }

    return H.toByteArray()
  }

  /**
   * Internal method, no need to call. Pads the given message to have a length
   * that is a multiple of 512 bits (64 bytes), including the addition of a
   * 1-bit, k 0-bits, and the message length as a 64-bit integer.
   *
   * @param message The message to padMessage.
   * @return A new array with the padded message bytes.
   */
  internal fun padMessage(message: ByteArray): ByteArray {
    val blockBits = 512
    val blockBytes = blockBits / 8

    // new message length: original + 1-bit and padding + 8-byte length
    var newMessageLength = message.size + 1 + 8
    val padBytes = blockBytes - newMessageLength % blockBytes
    newMessageLength += padBytes

    // copy message to extended array
    val paddedMessage = ByteArray(newMessageLength)
    message.copy(0, paddedMessage, 0, message.size)

    // write 1-bit
    paddedMessage[message.size] = 128.toByte()

    // skip padBytes many bytes (they are already 0)

    // write 8-byte integer describing the original message length
    val lenPos = message.size + 1 + padBytes

    paddedMessage.putLong(lenPos, message.size * 8.toLong())

    return paddedMessage
  }

  private fun ch(x: Int, y: Int, z: Int): Int {
    return x and y or (x.inv() and z)
  }

  private fun maj(x: Int, y: Int, z: Int): Int {
    return x and y or (x and z) or (y and z)
  }

  private fun bigSig0(x: Int): Int {
    return (x.rotateRight(2) xor x.rotateRight(13) xor x.rotateRight(22))
  }

  private fun bigSig1(x: Int): Int {
    return (x.rotateRight(6) xor x.rotateRight(11) xor x.rotateRight(25))
  }

  private fun smallSig0(x: Int): Int {
    return (x.rotateRight(7) xor x.rotateRight(18) xor x.ushr(3))
  }

  private fun smallSig1(x: Int): Int {
    return (x.rotateRight(17) xor x.rotateRight(19) xor x.ushr(10))
  }
}

/**
 * Writes a long split into 8 bytes.
 * @param [offset] start index
 * @param [value] the value to insert
 * Thanks to manu0466
 */
internal fun ByteArray.putLong(offset: Int, value: Long) {
  for (i in 7 downTo 0) {
    val temp = (value ushr (i * 8)).toUByte()
    this[offset + 7 - i] = temp.toByte()
  }
}

/**
 * Converts the given byte array into an int array via big-endian conversion (4 bytes become 1 int).
 * @throws IllegalArgumentException if the byte array size is not a multiple of 4.
 */
internal fun ByteArray.toIntArray(): IntArray {
  if (this.size % INT_BYTES != 0) {
    throw IllegalArgumentException("Byte array length must be a multiple of $INT_BYTES")
  }

  val array = IntArray(this.size / INT_BYTES)
  for (i in array.indices) {
    val integer = arrayOf(this[i * INT_BYTES], this[i * INT_BYTES + 1], this[i * INT_BYTES + 2], this[i * INT_BYTES + 3])
    array[i] = integer.toInt()
  }
  return array
}

/**
 * Copies an array from the specified source array, beginning at the
 * specified position, to the specified position of the destination array.
 */
internal fun ByteArray.copy(srcPos: Int, dest: ByteArray, destPos: Int, length: Int) {
  this.copyInto(dest, destPos, srcPos, srcPos + length)
}

internal const val INT_BYTES = 4

/**
 * Converts the first 4 bytes into their integer representation following the big-endian conversion.
 * @throws NumberFormatException if the array size is less than 4
 */
internal fun Array<Byte>.toInt(): Int {
  if (this.size < 4) throw NumberFormatException("The array size is less than 4")
  return (this[0].toUInt() shl 24) + (this[1].toUInt() shl 16) + (this[2].toUInt() shl 8) + (this[3].toUInt() shl 0)
}

/**
 * Converts a Byte into an unsigned Int.
 * Source: https://stackoverflow.com/questions/38651192/how-to-correctly-handle-byte-values-greater-than-127-in-kotlin
 *
 * TODO: Remove this once Kotlin 1.3 is released as it brings support for UInt
 */
internal fun Byte.toUInt() = when {
  (toInt() < 0) -> 255 + toInt() + 1
  else -> toInt()
}

/**
 * Converts the given int array into a byte array via big-endian conversion
 * (1 int becomes 4 bytes).
 * @return The converted array.
 */
internal fun IntArray.toByteArray(): ByteArray {
  val array = ByteArray(this.size * 4)
  for (i in this.indices) {
    val bytes = this[i].toBytes()
    array[i * 4] = bytes[0]
    array[i * 4 + 1] = bytes[1]
    array[i * 4 + 2] = bytes[2]
    array[i * 4 + 3] = bytes[3]
  }
  return array
}

/**
 * Copies an array from the specified source array, beginning at the
 * specified position, to the specified position of the destination array.
 */
internal fun IntArray.copy(srcPos: Int, dest: IntArray, destPos: Int, length: Int) {
  this.copyInto(dest, destPos, srcPos, srcPos + length)
}

/**
 * Returns the value obtained by rotating the two's complement binary representation of the specified [Int] value
 * right by the specified number of bits.
 * (Bits shifted out of the right hand, or low-order, side reenter on the left, or high-order.)
 */
internal fun Int.rotateRight(distance: Int): Int {
  return this.ushr(distance) or (this shl -distance)
}

/**
 * Converts an [Int] to an array of [Byte] using the big-endian conversion.
 * (The [Int] will be converted into 4 bytes)
 */
internal fun Int.toBytes(): Array<Byte> {
  val result = ByteArray(4)
  result[0] = (this shr 24).toByte()
  result[1] = (this shr 16).toByte()
  result[2] = (this shr 8).toByte()
  result[3] = this.toByte()
  return result.toTypedArray()
}
