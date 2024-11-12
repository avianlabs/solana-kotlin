package net.avianlabs.solana.vendor

import kotlin.test.Test
import kotlin.test.assertContentEquals

class ShortvecEncodingTest {
  @Test
  fun encodeLength() {
    assertContentEquals(
      byteArrayOf(0) /* [0] */,
      ShortVecEncoding.encodeLength(0)
    )
    assertContentEquals(
      byteArrayOf(1) /* [1] */,
      ShortVecEncoding.encodeLength(1)
    )
    assertContentEquals(
      byteArrayOf(5) /* [5] */,
      ShortVecEncoding.encodeLength(5)
    )
    assertContentEquals(
      byteArrayOf(127)/* [0x7f] */,
      ShortVecEncoding.encodeLength(127) // 0x7f
    )
    assertContentEquals(
      byteArrayOf(-128, 1) /* [0x80, 0x01] */,
      ShortVecEncoding.encodeLength(128) // 0x80
    )
    assertContentEquals(
      byteArrayOf(-1, 1) /* [0xff, 0x01] */,
      ShortVecEncoding.encodeLength(255) // 0xff
    )
    assertContentEquals(
      byteArrayOf(-128, 2)/* [0x80, 0x02] */,
      ShortVecEncoding.encodeLength(256) // 0x100
    )
    assertContentEquals(
      byteArrayOf(-1, -1, 1)/* [0xff, 0xff, 0x01] */,
      ShortVecEncoding.encodeLength(32767) // 0x7fff
    )
    assertContentEquals(
      byteArrayOf(-128, -128, -128, 1)/* [0x80, 0x80, 0x80, 0x01] */,
      ShortVecEncoding.encodeLength(2097152) // 0x200000
    )
  }
}
