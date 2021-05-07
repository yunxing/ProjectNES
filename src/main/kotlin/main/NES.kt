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
fun UShort.highByte(): UByte {
  vlog("high byte:" + this.toHex())
  return (this.toInt() shr 8).toUByte()
}

fun UShort.lowByte(): UByte {
  return (this.toInt() and 0x00FF).toUByte()
}

fun UShort.toHex(): String {
  return "0x" + this.toString(16).padStart(4, '0')
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

fun CreateROM(raw: UByteArray) {

}

data class ROM(val prgRom: UByteArray,
               val chrRom: UByteArray,
               val mapper: UByte,
               val screeMirroring: Mirroring) {
  companion object {
    fun create(raw: UByteArray): ROM {
      TODO("")
    }
  }

}