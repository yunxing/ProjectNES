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

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
class CPU {
  var regA: UByte = 0.toUByte()
  var regX: UByte = 0.toUByte()
  var regY: UByte = 0.toUByte()
  var status_c: Boolean = false
  var status_z: Boolean = false
  var status_i: Boolean = false
  var status_d: Boolean = false
  var status_b_hi: Boolean = false
  var status_b_lo: Boolean = false
  var status_v: Boolean = false
  var status_n: Boolean = false
  var mem = UByteArray(0xFFFF)

  // Program counter.
  var pc: UShort = 0u

  // Stack pointer.
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
  private fun stackPush(data: UByte) {
    println("push stack %8s".format(Integer.toBinaryString(data.toInt())))
    memWrite(sp, data)
    sp = sp.dec()

  }

  private fun stackPush16(data: UShort) {
    val hi = (data.toInt() shr 8).toUByte()
    val lo = data.toUByte()
    stackPush(hi)
    stackPush(lo)
  }

  // Pop stack and update stack pointer.
  private fun stackPop(): UByte {
    sp = sp.inc()
    return memRead(sp)
  }

  private fun stackPop16(): UShort {
    // Order matters here as stackPop updates stack pointer.
    val lo = stackPop()
    val hi = stackPop()
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  private fun statusAsUByte(): UByte {
    var result: UByte = 0u
    if (status_n) {
      result = result or 0b1000_0000u
    }
    if (status_v) {
      result = result or 0b0100_0000u
    }
    if (status_b_hi) {
      result = result or 0b0010_0000u
    }
    if (status_b_lo) {
      result = result or 0b0001_0000u
    }
    if (status_d) {
      result = result or 0b0000_1000u
    }
    if (status_i) {
      result = result or 0b0000_0100u
    }
    if (status_z) {
      result = result or 0b0000_0010u
    }
    if (status_c) {
      result = result or 0b0000_0001u
    }
    return result
  }

  private fun statusFromUByte(data: UByte) {
    var result: UByte = 0u
    if (data and 0b1000_0000u != 0.toUByte()) {
      status_n = true
    }
    if (data and 0b0100_0000u != 0.toUByte()) {
      status_v = true
    }
    status_b_hi = false
    status_b_lo = false
    if (data and 0b0000_1000u != 0.toUByte()) {
      status_d = true
    }
    if (data and 0b0000_0100u != 0.toUByte()) {
      status_i = true
    }
    if (data and 0b0000_0010u != 0.toUByte()) {
      status_z = true
    }
    if (data and 0b0000_0001u != 0.toUByte()) {
      status_c = true
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
    val lo = memRead(addr)
    val wrapped = (addr.inc() and 0x00FFu) == 0.toUShort()
    val hi = memRead(if (wrapped) {addr and 0xFF00u} else {addr.inc()})
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  fun memRead16ZeroPage(addr: UByte): UShort {
    val lo = memRead(addr.toUShort()).toUShort()
    val hi = memRead(addr.inc().toUShort()).toUShort()
    return ((hi.toInt() shl 8) or (lo.toInt())).toUShort()
  }

  fun inplaceModifyAorMem(
    mode: AddressingMode,
    preprocess: (UByte) -> Unit,
    modifier: (UByte) -> UByte,
    postprocess: (UByte) -> Unit,
  ) {
    // Read
    var op: UByte = if (mode == AddressingMode.Accumulator) {
      regA
    } else {
      val addr = getOpAddress(mode)
      memRead(addr)
    }
    // Process
    preprocess(op)
    val result = modifier(op)
    postprocess(result)
    // Write back.
    if (mode == AddressingMode.Accumulator) {
      regA = result
    } else {
      val addr = getOpAddress(mode)
      memWrite(addr, result)
    }
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
    status_b_hi = false
    status_b_lo = false
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
        val result = memRead16Wrapped(base)
        println("Base %4X".format(result.toInt()))
        result
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
        status_b_hi = true
        status_b_lo = true
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

  // Performs A + data + carry
  private fun addToRegA(data: UByte) {
    val sum = regA.toUShort() + status_c.toUShort() + data.toUShort()
    val result = sum.toUByte()
    status_c = sum > 0xFF.toUShort()
    status_v = (regA xor result) and
      (data xor result) and
      0x80.toUByte() != 0.toUByte()
    updateZNAndRegA(result)
  }

  fun adc(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    addToRegA(op)
  }

  fun and(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    updateZNAndRegA(regA and op)
  }

  fun asl(mode: AddressingMode) {
    inplaceModifyAorMem(
      mode,
      { this.status_c = it.bitIsSetAt(7) },
      { (it.toInt() shl 1).toUByte() },
      { updateZN(it) })
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

  fun jsr(mode: AddressingMode) {
    stackPush16((pc + 2U - 1U).toUShort())
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
    inplaceModifyAorMem(
      mode,
      { this.status_c = it.bitIsSetAt(7) },
      { (it.toInt() ushr 1).toUByte() },
      { updateZN(it) })
  }

  fun ora(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val op = memRead(addr)
    updateZNAndRegA(regA or op)
  }

  fun pha(mode: AddressingMode) {
    stackPush(regA);
  }

  fun php(mode: AddressingMode) {
    // The CPU doesn't really materialize the B flag, but we still do for the ease of
    // implementation.
    // See also:
    // http://wiki.nesdev.com/w/index.php/Status_flags#The_B_flag
    status_b_hi = true
    status_b_lo = true
    stackPush(statusAsUByte())
  }

  fun pla(mode: AddressingMode) {
    updateZNAndRegA(stackPop())
  }

  fun plp(mode: AddressingMode) {
    statusFromUByte(stackPop())
  }

  fun rol(mode: AddressingMode) {
    inplaceModifyAorMem(
      mode,
      { this.status_c = it.bitIsSetAt(7) },
      { it.rotateLeft(1) },
      { updateZN(it) })
  }

  fun ror(mode: AddressingMode) {
    inplaceModifyAorMem(
      mode,
      { this.status_c = it.bitIsSetAt(7) },
      { it.rotateRight(1) },
      { updateZN(it) },
    )
  }

  fun rti(mode: AddressingMode) {
    statusFromUByte(stackPop())
    pc = stackPop16()
  }

  fun rts(mode: AddressingMode) {
    // In our implementation we increment pc before each instruction. RTS expects pc to be increment
    // after each instruction. Use an additional +1 to account for the difference here.
    pc = (stackPop16() + 1u).toUShort()
  }

  fun sbc(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    val data = memRead(addr)
    // SBC = A - data - (1 - C), which can be simplified to
    //     = A + ((-data) - 1) + C
    addToRegA((-data.toInt() - 1).toUByte())
  }

  fun sec(mode: AddressingMode) {
    status_c = true
  }

  fun sed(mode: AddressingMode) {
    status_d = true
  }

  fun sei(mode: AddressingMode) {
    status_i = true
  }

  fun sta(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    memWrite(addr, regA)
  }

  fun stx(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    memWrite(addr, regX)
  }

  fun sty(mode: AddressingMode) {
    val addr = getOpAddress(mode)
    memWrite(addr, regY)
  }

  fun tax(mode: AddressingMode) {
    updateZNAndRegX(regA)
  }

  fun tay(mode: AddressingMode) {
    updateZNAndRegY(regA)
  }
}

fun main() {

}