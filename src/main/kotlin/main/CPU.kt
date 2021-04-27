package main

import kotlin.reflect.KMutableProperty0

@ExperimentalUnsignedTypes
fun UByte.bitIsSetAt(bit: Int): Boolean {
  val mask = (0b1 shl bit)
  return this.toInt() and mask != 0
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

@ExperimentalUnsignedTypes
class CPU {
  var regA: UByte = 0.toUByte()
  var regX: UByte = 0.toUByte()
  var regY: UByte = 0.toUByte()
  var status_c: Boolean = false
  var status_z: Boolean = false
  var status_i: Boolean = false
  var status_d: Boolean = false
  var status_b: Boolean = false
  var status_v: Boolean = false
  var status_n: Boolean = false
  var mem = UByteArray(0xFFFF)
  var pc: UShort = 0u
  var sp: UShort = 0x10FFu

  val opTable: HashMap<UByte, Opcode> = hashMapOf<UByte, Opcode>().apply {
    for (opcode in cpuOpcodes) {
      assert(
        put(opcode.opcode, opcode) == null
      ) {
        "Opcode %04X already exists".format(opcode.opcode.toInt())
      }
    }
  }

  private fun memRead(addr: UShort): UByte {
    return mem[addr.toInt()]
  }

  fun memRead16(addr: UShort): UShort {
    val lo = memRead(addr).toUShort()
    val hi = memRead(addr.inc()).toUShort()
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  // Used for page boundary bug mode for 6502.
  fun memRead16Wrapped(addr: UShort): UShort {
    val lo = memRead(addr).toUShort()
    val wrapped = (addr.inc() and 0x00FFu) == 0.toUShort()
    val hi = memRead(addr and 0xFF00u).toUShort()
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  fun memRead16ZeroPage(addr: UByte): UShort {
    val lo = memRead(addr.toUShort()).toUShort()
    val hi = memRead(addr.inc().toUShort()).toUShort()
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  fun memWrite(addr: UShort, data: UByte) {
    mem[addr.toInt()] = data
  }

  fun memWrite16(addr: UShort, data: UShort) {
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
    status_c = false
    status_z = false
    status_i = false
    status_d = false
    status_b = false
    status_v = false
    status_n = false

  }

  fun execute(program: List<UByte>, resetState: Boolean = true) {
    load(program)
    if (resetState) {
      reset()
    }
    pc = memRead16(0xFFFC.toUShort())
    run()
  }

  fun getOpAddress(mode: AddressingMode): UShort {
    return when (mode) {
      AddressingMode.Immediate -> pc
      AddressingMode.Relative -> (pc - 1U + memRead(pc)).toUShort()
      AddressingMode.ZeroPage -> memRead(pc).toUShort()
      AddressingMode.Absolute -> memRead16(pc)
      // Additional toUByte conversions are needed to wrap in zero page space.
      AddressingMode.ZeroPage_X -> (memRead(pc) + regX).toUByte().toUShort()
      AddressingMode.ZeroPage_Y -> (memRead(pc) + regY).toUByte().toUShort()
      AddressingMode.Absolute_X -> (memRead16(pc) + regX).toUShort()
      AddressingMode.Absolute_Y -> (memRead16(pc) + regY).toUShort()
      AddressingMode.Indirect -> {
        val base = memRead16(pc)
        memRead16Wrapped(base)
      }
      AddressingMode.Indirect_X -> {
        val base = memRead(pc)
        val addr = (base + regX).toUByte().toUShort()
        memRead16(addr)
      }
      AddressingMode.Indirect_Y -> {
        val base = memRead(pc)
        val derefBase = memRead16ZeroPage(base)
        (regY + derefBase).toUShort()
      }
      AddressingMode.NoneAddressing,
      AddressingMode.Accumulator -> {
        TODO("Mode not supported")
      }
    }
  }

  companion object Static {
    // Companion objects can also have names.

  }

  private fun run() {
    println("run")
    while (true) {
      val opcode = memRead(pc)
      pc = pc.inc()
      // Used to check if branch instruction is called.
      val pcBefore = pc
      if (opcode == 0.toUByte()) {
        status_b = true
        return
      }
      val op = opTable[opcode]
      if (op != null) {
        op.handler(this, op.mode)
        // Not a branch instruction, update pc.
        if (pcBefore == pc) {
          // The pc has already been incremented before execution, account for that and update by
          // op.bytes - 1.
          ::pc.incBy(op.bytes.toInt() - 1)
        }
      } else {
        TODO("Unknown op code: %04X".format(opcode.toInt()))
      }
    }
  }

  private fun updateZN(value: UByte) {
    status_z = value.toInt() == 0
    status_n = value.bitIsSetAt(7)
  }

  private fun updateZNAndRegA(value: UByte) {
    regA = value
    updateZN(regA)
  }

  private fun updateZNAndRegX(value: UByte) {
    regX = value
    updateZN(regX)
  }

  private fun updateZNAndRegY(value: UByte) {
    regY = value
    updateZN(regY)
  }

  fun adc(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    val sum = regA.toUShort() + status_c.toUShort() + op.toUShort()
    val result = sum.toUByte()
    status_c = sum > 0xFF.toUShort()
    status_v = (regA xor result) and
      (op xor result) and
      0x80.toUByte() != 0.toUByte()
    updateZNAndRegA(result)
  }

  fun and(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    updateZNAndRegA(regA and op)
  }

  fun asl(mode: AddressingMode) {
    var op : UByte = 0.toUByte()
    if (mode == AddressingMode.Accumulator) {
      op = regA
    } else {
      val addr = getOpAddress(mode)
      op = memRead(addr)
    }
    this.status_c = op.bitIsSetAt(7)
    updateZNAndRegA((op.toInt() shl 1).toUByte())
  }

  fun bcc(mode: AddressingMode) {
    if (status_c) {
      pc = getOpAddress(mode)
    }
  }

  fun bcs(mode: AddressingMode) {
    if (!status_c) {
      pc = getOpAddress(mode)
    }
  }

  fun beq(mode: AddressingMode) {
    if (status_z) {
      pc = getOpAddress(mode)
    }
  }

  fun bit(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    val result = regA and op
    updateZN(result)
    status_v = result.bitIsSetAt(6)
  }

  fun bmi(mode: AddressingMode) {
    if (status_n) {
      pc = getOpAddress(mode)
    }
  }

  fun bne(mode: AddressingMode) {
    if (!status_z) {
      pc = getOpAddress(mode)
    }
  }

  fun bpl(mode: AddressingMode) {
    if (!status_n) {
      pc = getOpAddress(mode)
    }
  }

  fun bvc(mode: AddressingMode) {
    if (!status_v) {
      pc = getOpAddress(mode)
    }
  }

  fun bvs(mode: AddressingMode) {
    if (status_v) {
      pc = getOpAddress(mode)
    }
  }

  fun clc(mode: AddressingMode) {
    status_c = false
  }


  fun cld(mode: AddressingMode) {
    status_d = false
  }

  fun cli(mode: AddressingMode) {
    status_i = false
  }

  fun clv(mode: AddressingMode) {
    status_v = false
  }

  fun compare(data: UByte, compareWith: UByte) {
    status_c = data <= compareWith
    updateZN((compareWith - data).toUByte())
  }
  fun cmp(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    compare(op, regA)
  }

  fun cpx(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    compare(op, regX)
  }

  fun cpy(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    compare(op, regY)
  }

  fun dec(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    memWrite(addr, op.dec())
    updateZN(op.dec())
  }
  fun dex(mode: AddressingMode) {
    updateZNAndRegX(regX.dec())
  }
  fun dey(mode: AddressingMode) {
    updateZNAndRegY(regY.dec())
  }

  fun eor(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    updateZNAndRegA(regA xor op)
  }

  fun inc(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    memWrite(addr, op.inc())
    updateZN(op.inc())
  }

  // avoiding jvm name collision
  fun inxFun(mode: AddressingMode) {
    updateZNAndRegX(regX.inc())
  }
  fun iny(mode: AddressingMode) {
    updateZNAndRegY(regY.inc())
  }

  fun jmp(mode: AddressingMode) {
    pc = getOpAddress(mode)
  }

  private fun stackPush(data: UByte) {
    memWrite(sp, data)
    sp = sp.dec()
  }

  private fun stackPush16(data: UShort) {
    val hi = (data.toInt() shr 8).toUByte()
    val lo = data.toUByte()
    stackPush(hi)
    stackPush(lo)
  }

  fun jsr(mode: AddressingMode) {
    stackPush16((pc + 2U - 1U).toUShort());
    pc = getOpAddress(mode)
  }

  fun lda(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    updateZNAndRegA(memRead(addr))
  }

  fun ldx(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    updateZNAndRegX(memRead(addr))
  }

  fun ldy(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    updateZNAndRegY(memRead(addr))
  }

  fun nop(mode: AddressingMode) = Unit

  fun lsr(mode: AddressingMode) {
    var op : UByte = 0.toUByte()
    if (mode == AddressingMode.Accumulator) {
      op = regA
    } else {
      val addr = getOpAddress(mode)
      op = memRead(addr)
    }
    this.status_c = op.bitIsSetAt(0)
    updateZNAndRegA((op.toInt() shr 1).toUByte())
  }

  fun ora(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    updateZNAndRegA(regA or op)
  }

  fun pha(mode: AddressingMode) {
    stackPush(regA);
  }

  fun tax(mode: AddressingMode) {
    regX = regA
    updateZNAndRegX(regX)
  }

  fun sta(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    memWrite(addr, regA)
  }

  fun inx(mode: AddressingMode) {
    regX = regX.inc()
    updateZNAndRegX(regX)
  }
}

fun main() {

}