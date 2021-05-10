package main

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
class Bus() {
  companion object {
    val RAM_START : UShort = 0x0000.toUShort()
    val RAM_END : UShort = 0x1FFF.toUShort()
    val PPU_START : UShort = 0x2000.toUShort()
    val PPU_END : UShort = 0x3FFF.toUShort()
    val PRG_START : UShort = 0x8000.toUShort()
    val PRG_END : UShort = 0xFFFF.toUShort()
  }
  var mem = UByteArray(0xFFFF)
  var rom : ROM? = null

  enum class Unit {
    Mem,
    PRG_ROM,
    PPU
  }
  // Handle NES address mirroring
  // See https://bugzmanov.github.io/nes_ebook/chapter_4.html
  private fun mirrorAddress(addr: UShort): Pair<UShort, Unit> {
    return when(addr) {
      in RAM_START..RAM_END -> {
        Pair(addr and 0b00000111_11111111u, Unit.Mem)
      }
      in PPU_START..PPU_END -> {
        Pair(addr and 0b00100000_00000111u, Unit.PPU)
      }
      in PRG_START..PRG_END -> {
        // None-rom mode for test.
        if (rom == null) {
          // Special address for initial pc
          if (addr == 0xFFFC.toUShort() || addr == 0xFFFD.toUShort()) {
            addr to Unit.Mem
          } else {
            TODO("Invalid memory access" + addr.toHex())
          }
        } else {
          var rom_addr : UShort = (addr - 0x8000u).toUShort()
          if (rom!!.prgRom.size == 0x4000 && addr >= 0x4000u) {
            rom_addr = (addr % 0x4000u).toUShort()
          }
          rom_addr to Unit.PRG_ROM
        }
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
    val (unit_addr, unit) = mirrorAddress(addr)
    when (unit) {
      Unit.Mem -> {
        return mem[unit_addr.toInt()]
      }
      Unit.PRG_ROM -> {
        return rom!!.prgRom[unit_addr.toInt()]
      }
      else -> {
        TODO("NOt supported read unit: " + unit.name)
      }
    }

  }

  fun memWrite(addr: UShort, data: UByte) {
    val (unit_addr, unit) = mirrorAddress(addr)
    when (unit) {
      Unit.Mem -> {
        mem[unit_addr.toInt()] = data
      }
      else -> {
        TODO("Not supported write unit: " + unit.name)
      }
    }
  }
}