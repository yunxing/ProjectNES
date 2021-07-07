package main

class AddrRegister {
  // High 8 bits of address.
  private var high : UByte = 0u
  // Low 8 bits of address.
  private var low  : UByte = 0u
  private var hiPtr : Boolean = true

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

    if (get() > 0x3fffu) {
      set(get() and 0x3FFFu)
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
    return ((high.toUInt() shl 8) or (low.toUInt())).toUShort()
  }
}

class ControlRegister {
  private var bits : UByte = 0u

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
    if (bits and VRAM_ADD_INCREMENT == 0.toUByte()) {
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
  val addrReg = AddrRegister()
  val ctrlReg = ControlRegister()

  var internalBuffer: UByte = 0.toUByte()

  constructor(chrROM: UByteArray, mirroring: Mirroring) {
    this.chrROM = chrROM
    this.mirroring = mirroring
    paletteTable = UByteArray(32) {0u}
    vram = UByteArray(2048) {0u}
    oamData = UByteArray(256) {0u}
  }

  fun incrementVramAddr() {
    addrReg.increment(ctrlReg.vramAddrIncrement())
  }

  fun mirrorVramAddr(addr: UShort) : UShort {
    val mirroredVram = addr and 0x2FFF.toUShort() // Mirror 0x3000-0x3eff to 0x2000-0x2eff
    val vramIndex = (mirroredVram - 0x2000u).toUShort()
    val nameTableIndex = (vramIndex / 0x400u).toInt()
    if (mirroring == Mirroring.VERTICAL) {
      if (nameTableIndex == 2 || nameTableIndex == 3) {
        return (vramIndex - 0x800u).toUShort()
      }
    }
    if (mirroring == Mirroring.HORIZONTAL) {
      if (nameTableIndex == 1) {
        return (vramIndex - 0x400u).toUShort()
      }
      if (nameTableIndex == 2) {
        return (vramIndex - 0x400u).toUShort()
      }
      if (nameTableIndex == 3) {
        return (vramIndex - 0x800u).toUShort()
      }
    }
    return vramIndex
  }

  fun readData() : UByte {
    val addr = addrReg.get()
    incrementVramAddr()

    when (addr) {
      in 0u..0x1FFFu -> {
        val result = internalBuffer
        internalBuffer = chrROM[addr.toInt()]
        return result
      }
      in 0x2000u..0x2FFFu -> {
        val result = internalBuffer
        internalBuffer = vram[mirrorVramAddr(addr).toInt()]
        return result
      }
      in 0x3000u..0x3EFFu -> {
        TODO("Unexpected read: " + addr.toHex())
      }
      0x3F10.toUShort(), 0x3f14.toUShort(),
      0x3f18.toUShort(), 0x3f1c.toUShort() -> {
        val addr_mirror = addr - 0x10u
        return paletteTable[(addr_mirror - 0x3F00u).toInt()]
      }
      in 0x3F00u..0x3FFFu -> {
        return paletteTable[(addr - 0x3F00u).toInt()]
      }
      else -> {
        TODO("Unexpected read: " + addr.toHex())
      }
    }
  }

  fun writeData(data: UByte) {
    val addr = addrReg.get()
    incrementVramAddr()

    when (addr) {
      in 0u..0x1FFFu -> {
        TODO("Readonly ")
      }
      in 0x2000u..0x2FFFu -> {
        vram[mirrorVramAddr(addr).toInt()] = data
      }
      in 0x3000u..0x3EFFu -> {
        TODO("Unexpected read: " + addr.toHex())
      }
      0x3F10.toUShort(), 0x3f14.toUShort(),
      0x3f18.toUShort(), 0x3f1c.toUShort() -> {
        val addr_mirror = addr - 0x10u
        paletteTable[(addr_mirror - 0x3F00u).toInt()] = data
      }
      in 0x3F00u..0x3FFFu -> {
        paletteTable[(addr - 0x3F00u).toInt()] = data
      }
      else -> {
        TODO("Unexpected read: " + addr.toHex())
      }
    }
  }

  fun writeToAddrReg(value : UByte) {
    addrReg.update(value)
  }

  fun writeToCtrlReg(value : UByte) {
    ctrlReg.update(value)
  }
}