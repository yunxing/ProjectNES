package main

import kotlin.reflect.KMutableProperty0

// val lda = 0xA9.toUByte()
val tax = 0xAA.toUByte()
val inx = 0xE8.toUByte()
val brk = 0x00.toUByte()

fun UByte.bitIsSetAt(bit: Int) : Boolean {
  val mask = (0b1 shl bit)
  return this.toInt() and mask != 0
}

fun KMutableProperty0<UShort>.incBy(amount: Int) {
  for (i in 1..amount) {
    set(get().inc())
  }
}

enum class AddressingMode {
  Immediate,
  ZeroPage,
  ZeroPage_X,
  ZeroPage_Y,
  Absolute,
  Absolute_X,
  Absolute_Y,
  Indirect_X,
  Indirect_Y,
  NoneAddressing,
}

val lda_ref = CPU::lda

@ExperimentalUnsignedTypes
class CPU {
  var regA: UByte = 0.toUByte()
  var regX: UByte = 0.toUByte()
  var regY: UByte = 0.toUByte()
  var status: HashMap<Char, Boolean> = hashMapOf()
  var mem = UByteArray(0xFFFF)
  var pc: UShort = 0.toUShort()

  private fun memRead(addr: UShort) : UByte {
    return mem[addr.toInt()]
  }

  fun memRead16(addr: UShort) : UShort {

    val lo = memRead(addr).toUShort()

    val hi = memRead(addr.inc()).toUShort()

    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }
  fun memRead16ZeroPage(addr: UByte) : UShort {
    val lo = memRead(addr.toUShort()).toUShort()
    val hi = memRead(addr.inc().toUShort()).toUShort()

    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }
  fun memWrite(addr: UShort, data: UByte) {
    mem[addr.toInt()] = data
  }

  fun memWrite16(addr: UShort, data: UShort){
    val hi = (data.toInt() shr 8).toUByte()
    val lo = (data.toInt() and 0xff).toUByte()
    memWrite(addr, lo)
    memWrite(addr.inc(), hi)
  }

  fun load(program: List<UByte>) {
    var i = 0x8000
    for (b in program) {
      mem[i] = b
      i += 1
    }
    memWrite16(0xFFFC.toUShort(), 0x8000.toUShort())
  }

  fun reset() {
    regA = 0.toUByte()
    regX = 0.toUByte()
    regY = 0.toUByte()
    status = hashMapOf(
      'C' to false,
      'Z' to false,
      'I' to false,
      'D' to false,
      'B' to false,
      'V' to false,
      'N' to false
    )
    pc = memRead16(0xFFFC.toUShort())
  }
  fun execute(program: List<UByte>) {
    load(program)
    reset()
    run()
  }

  fun getOpAddress(mode: AddressingMode) : UShort {
    return when (mode) {
      AddressingMode.Immediate -> pc
      AddressingMode.ZeroPage -> memRead(pc).toUShort()
      AddressingMode.Absolute -> memRead16(pc)
      AddressingMode.ZeroPage_X -> (memRead(pc) + regX).toUShort()
      AddressingMode.ZeroPage_Y -> (memRead(pc) + regY).toUShort()
      AddressingMode.Absolute_X -> (memRead16(pc) + regX).toUShort()
      AddressingMode.Absolute_Y -> (memRead16(pc) + regY).toUShort()
      AddressingMode.Indirect_X -> {
        val base = memRead(pc)
        val addr = (base + regX).toUShort()
        memRead16(addr)
      }
      AddressingMode.Indirect_Y -> {
        val base = memRead(pc)
        val derefBase = memRead16ZeroPage(base)
        (regY + derefBase).toUShort()
      }
      AddressingMode.NoneAddressing -> {
        TODO("Mode not supported")
      }
    }
  }

  fun updatePc(mode: AddressingMode) {
    when (mode) {
      AddressingMode.Immediate -> ::pc.incBy(1)
      AddressingMode.ZeroPage -> ::pc.incBy(1)
      AddressingMode.Absolute -> ::pc.incBy(2)
      AddressingMode.ZeroPage_X -> ::pc.incBy(1)
      AddressingMode.ZeroPage_Y -> ::pc.incBy(1)
      AddressingMode.Absolute_X -> ::pc.incBy(2)
      AddressingMode.Absolute_Y -> ::pc.incBy(2)
      AddressingMode.Indirect_X -> ::pc.incBy(1)
      AddressingMode.Indirect_Y -> ::pc.incBy(1)
      AddressingMode.NoneAddressing -> {
        TODO("Mode not supported")
      }
    }
  }

  fun getOpAddressAndUpdatePc(mode: AddressingMode) : UShort {
    val result = getOpAddress(mode)
    updatePc(mode)
    return result
  }

  companion object Static {
    // Companion objects can also have names.

  }

  private fun run() {
    println("run")
    while (true) {
      val opcode = mem[pc.toInt()]
      pc = pc.inc()
      when (opcode.toInt()) {
        0xA9 ->lda_ref(this, AddressingMode.Immediate)
        0xA5 ->lda(AddressingMode.ZeroPage)
        0xB5 ->lda(AddressingMode.ZeroPage_X)
        0xAD ->lda(AddressingMode.Absolute)
        0xBD ->lda(AddressingMode.Absolute_X)
        0xB9 ->lda(AddressingMode.Absolute_Y)
        0xA1 ->lda(AddressingMode.Absolute_Y)
        0xB1 ->lda(AddressingMode.Absolute_Y)

        tax -> tax()
        inx -> inx()
        brk -> return
        else -> {
          TODO("Default: %04X".format(opcode.toInt()))
        }
      }
    }
  }

  private fun updateZN (value: UByte) {
    status['Z'] = value.toInt() == 0
    status['N'] = value.bitIsSetAt(7)
  }
  private fun tax() {
    regX = regA
    updateZN(regX)
  }
  fun lda(mode: AddressingMode) {
    val addr = getOpAddressAndUpdatePc(mode)
    val op  = memRead(addr)
    regA = op
    updateZN(regA)
  }
  private fun inx() {
    regX = regX.inc()
    updateZN(regX)
  }
}

fun main() {

}