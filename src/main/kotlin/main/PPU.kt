package main

data class AddrRegister {
  // High 8 bits of address.
  val high : UByte
  // Low 8 bits of address.
  val low  : UByte
  val hiPtr : Boolean
  constructor() {
    high = 0u
    low  = 0u
    hiPtr = true
  }

  fun set(data : UShort) {
    high = data.highByte()
    low = data.lowByte()
  }

  fun update(data : UByte) {
    if (this.hiPtr) {
      high = data
    } else {
      low = data
    }

    if (get() > 0x3fff) {
      set(get() and 0x3FFF)
    }
    hiPtr = !hiPtr
  }

  fun increment(inc : UByte) {
    val low_old = low
    low = low.inc()
    if (low_old > low) {
      high = high.inc()
    }

    if (get() > 0x3FFFu) {
      set(get() and 0x3FFFu)
    }
  }

  fun reset() {
    hiPtr = true
  }

  fun get() : UShort {
    return ((value.first.toUInt() shl 8) or (value.second.toUInt())).toUShort()
  }
}

data class ControlRegister {
  val bits : UByte = 0u

  companion object {
    // 7  bit  0
    // ---- ----
    // VPHB SINN
    // |||| ||||
    // |||| ||++- Base nametable address
    // |||| ||    (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
    // |||| |+--- VRAM address increment per CPU read/write of PPUDATA
    // |||| |     (0: add 1, going across; 1: add 32, going down)
    // |||| +---- Sprite pattern table address for 8x8 sprites
    // ||||       (0: $0000; 1: $1000; ignored in 8x16 mode)
    // |||+------ Background pattern table address (0: $0000; 1: $1000)
    // ||+------- Sprite size (0: 8x8 pixels; 1: 8x16 pixels)
    // |+-------- PPU master/slave select
    // |          (0: read backdrop from EXT pins; 1: output color on EXT pins)
    // +--------- Generate an NMI at the start of the
    //            vertical blanking interval (0: off; 1: on)
    val NAMETABLE1: UByte              = 0b00000001u
    val NAMETABLE2: UByte              = 0b00000010u
    val VRAM_ADD_INCREMENT: UByte      = 0b00000100u
    val SPRITE_PATTERN_ADDR: UByte     = 0b00001000u
    val BACKGROUND_PATTERN_ADDR: UByte = 0b00010000u
    val SPRITE_SIZE: UByte             = 0b00100000u
    val MASTER_SLAVE_SELECT: UByte     = 0b01000000u
    val GENERATE_NIM: UByte            = 0b10000000u
  }

  fun vramAddrIncrement() : UByte {
    if (bits and VRAM_ADD_INCREMENT == 0u) {
      return 1u
    } else {
      return 32u
    }
  }

  fun update(data : UByte) {
    bits = data
  }
}

class PPU {
  val chrROM: UByteArray
  val paletteTable: UByteArray
  val vram: UByteArray
  val oamData: UByteArray
  val mirroring : Mirroring

  // Registers
  val addr: AddrRegister
  val ctrl: ControlRegister

  val internalBuffer: UByte

  constructor(chrROM: UByteArray, mirroring: Mirroring) {
    this.chrROM = chrROM
    this.mirroring = mirroring
    paletteTable = UByteArray(32) {0u}
    vram = UByteArray(2048) {0u}
    oamData = UByteArray(256) {0u}
  }

  fun incrementVramAddr() {
    addr.increment(ctrl.vramAddrIncrement())
  }

  fun mirrorVramAddr(addr: UShort) : UShort {
    val mirroredVram = addr and 0x2FFF // Mirror 0x3000-0x3eff to 0x2000-0x2eff
    val vramIndex = (mirroredVram - 0x2000).toUShort()
    val nameTableIndex = vramIndex / 0x400u
    if (mirroring == Mirroring.VERTICAL) {
      if (nameTableIndex == 2 || nameTableIndex == 3) {
        return (vramIndex - 0x800u).toUInt()
      }
    }
    if (mirroring == Mirroring.HORIZONTAL) {
      if (nameTableIndex == 1) {
        return (vramIndex - 0x400u).toUInt()
      }
      if (nameTableIndex == 2) {
        return (vramIndex - 0x400u).toUInt()
      }
      if (nameTableIndex == 3) {
        return (vramIndex - 0x800u).toUInt()
      }
    }
    return vramIndex
  }

  fun readData() : UByte {
    val addr = addr.get()
    incrementVramAddr()

    when (addr) {
      in 0..0x1FFF -> {
        val result = internalBuffer
        internalBuffer = chrROM[addr.toUInt()]
        return result
      }
      in 0x2000..0x2FFF -> {
        val result = internalBuffer
        internalBuffer = vram[mirrorVramAddr(addr).toUInt()]
        return result
      }
      in 0x3000..0x3EFF -> {
        TODO("Unexpected read: " + addr.toHex())
      }
      in 0x3F00..0x3FFF -> {
        paletteTable[addr - 0x3F00u]
      }
      else -> {
        TODO("Unexpected read: " + addr.toHex())
      }
    }
  }

  fun writeToAddr(value : UByte) {
    addr.update(value)
  }

  fun writeToCtrl(value : UByte) {
    ctrl.update(value)
  }
}