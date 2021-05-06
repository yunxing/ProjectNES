package main

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
class Bus {
  companion object {
    val RAM_START : UShort = 0x0000.toUShort()
    val RAM_END : UShort = 0x1FFF.toUShort()
    val PPU_START : UShort = 0x2000.toUShort()
    val PPU_END : UShort = 0x3FFF.toUShort()
  }
  var mem = UByteArray(0xFFFF)

  // Handle NES address mirroring
  // See https://bugzmanov.github.io/nes_ebook/chapter_4.html
  private fun mirrorAddress(addr: UShort): UShort {
    return when(addr) {
      in RAM_START..RAM_END -> {
        addr and 0b00000111_11111111u

      }
      in PPU_START..PPU_END -> {
        addr and 0b00100000_00000111u
      }
      // Special address for initial pc
      0xFFFC.toUShort(), 0xFFFD.toUShort() -> {
        addr
      }
      else -> {
        TODO("Invalid memory access" + addr.toHex())
      }
    }
  }

  operator fun get(index: Int) : UByte {
    return memRead(index.toUShort())
  }

  operator fun set(index: Int, data: UByte) {
    return memWrite(index.toUShort(), data)
  }

  fun memRead(addr: UShort): UByte {
    return mem[mirrorAddress(addr).toInt()]
  }

  fun memWrite(addr: UShort, data: UByte) {
    mem[mirrorAddress(addr).toInt()] = data
  }
}