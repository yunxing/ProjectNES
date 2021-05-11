package main

import kotlin.reflect.KMutableProperty0


fun vlog(msg:String) {
  val debug = true
  if (debug) {
    println(msg)
  }
}

@ExperimentalUnsignedTypes
fun UByte.bitIsSetAt(bit: Int): Boolean {
  val mask = (0b1 shl bit)
  return this.toInt() and mask != 0
}

@ExperimentalUnsignedTypes
fun UByte.setBitAt(bit: Int, set: Boolean): UByte {
  val mask = (0b1 shl bit)
  return (this or mask.toUByte()).toUByte()
}

fun UByte.shiftRight(bit: Int): UByte {
  return (this.toUInt() shl bit).toUByte()
}

@ExperimentalUnsignedTypes
fun UShort.highByte(): UByte {
  vlog("high byte:" + this.toHex())
  return (this.toInt() shr 8).toUByte()
}

fun UShort.lowByte(): UByte {
  return (this.toInt() and 0x00FF).toUByte()
}
fun Int.toHex(): String {
  return this.toString(16).padStart(4, '0').toUpperCase()
}

fun UShort.toHex(): String {
  return this.toString(16).padStart(4, '0').toUpperCase()
}

@ExperimentalUnsignedTypes
fun UByte.toHex(): String {
  return this.toUShort().toHex()
}


@ExperimentalUnsignedTypes
fun Boolean.toUShort() = if (this) 1.toUShort() else 0.toUShort()
fun Boolean.toUByte() = if (this) 1.toUByte() else 0.toUByte()

@ExperimentalUnsignedTypes
fun KMutableProperty0<UShort>.incBy(amount: Int) {
  for (i in 1..amount) {
    set(get().inc())
  }
}

fun uByteListOf(vararg elements: Int): List<UByte> {
  return elements.map(Int::toUByte)
}

enum class AddressingMode {
  Immediate,
  Accumulator,
  ZeroPage,
  ZeroPage_X,
  ZeroPage_Y,
  Absolute,
  Absolute_X,
  Absolute_Y,
  Indirect,
  Indirect_X,
  Indirect_Y,
  Relative,
  NoneAddressing,
}
enum class Mirroring {
  VERTICAL,
  HORIZONTAL,
  FOUR_SCREEN
}

// Public version of memory read, needed for js interop.
@JsName("runROM")
@JsExport
fun runROM(raw_js: ByteArray) : CPU {
  val raw = UByteArray(raw_js.size)
  for (i in 0 until raw_js.size) {
    raw[i] = raw_js[i].toUByte()
  }
  val rom = ROM.create(raw)
  val cpu = CPU()
  cpu.loadROM(rom)
  return cpu
}

@OptIn(kotlin.ExperimentalUnsignedTypes::class)
data class ROM(val prgRom: UByteArray,
               val chrRom: UByteArray,
               val mapper: UByte,
               val screeMirroring: Mirroring) {
  companion object {
    val NES_TAG = uByteListOf(0x4E, 0x45, 0x53, 0x1A)
    val PRG_ROM_PAGE_SIZE = 16384;
    val CHR_ROM_PAGE_SIZE =  8192;
    fun create(raw: UByteArray): ROM {
      if (raw.slice(0..3).toList() != NES_TAG) {
        TODO("Not a nes file")
      }
      val mapper = (raw[7] and 0b1111_0000u) or (raw[6].toInt() ushr 4).toUByte()
      if (mapper != 0.toUByte()) {
        TODO("Not supported mapper " + mapper.toString())
      }
      val inesVer = (raw[7].toInt() ushr 2).toUByte() and 0b11u
      if (inesVer != 0.toUByte()) {
        TODO("Not supported ines version " + inesVer.toString())
      }

      var fourScreen = raw[6] and 0b1000u != 0.toUByte()
      var verticalMirroring = raw[6] and 0b1u != 0.toUByte()

      var screeMirroring = if (fourScreen) {
        Mirroring.FOUR_SCREEN
      } else if (verticalMirroring) {
        Mirroring.VERTICAL
      } else {
        Mirroring.HORIZONTAL
      }
      var prgRomSize = raw[4].toInt() * PRG_ROM_PAGE_SIZE
      vlog("prgRomSize: " + prgRomSize.toHex())
      var chrRomSize = raw[5].toInt() * CHR_ROM_PAGE_SIZE
      var skipTrainer = raw[6] and 0b100u != 0.toUByte()
      var prgRomStart = 16 + if (skipTrainer) { 512 } else { 0 }
      var chrRomStart = prgRomStart + prgRomSize

      vlog("Creating rom")
      return ROM(raw.slice(prgRomStart until (prgRomStart + prgRomSize)).toUByteArray(),
                 raw.slice(chrRomStart until (chrRomStart + chrRomSize)).toUByteArray(),
                 mapper,
                 screeMirroring)
    }

  }

}