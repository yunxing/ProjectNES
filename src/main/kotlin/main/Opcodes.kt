package main

data class Opcode(
  val opcode: UByte,
  val handler: (CPU, AddressingMode) -> Unit,
  val bytes: UShort,
  val mode: AddressingMode
) {
  constructor(
    opcode: Int,
    handler: (CPU, AddressingMode) -> Unit,
    bytes: Int,
    mode: AddressingMode
  ) :
    this(opcode.toUByte(), handler, bytes.toUShort(), mode)
}

@kotlin.ExperimentalUnsignedTypes
val cpuOpcodes: List<Opcode> = mutableListOf<Opcode>().apply {
  add(Opcode(0x69, CPU::adc, 2, AddressingMode.Immediate))
  add(Opcode(0x65, CPU::adc, 2, AddressingMode.ZeroPage))
  add(Opcode(0x75, CPU::adc, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6D, CPU::adc, 3, AddressingMode.Absolute))
  add(Opcode(0x7D, CPU::adc, 3, AddressingMode.Absolute_X))
  add(Opcode(0x79, CPU::adc, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x61, CPU::adc, 2, AddressingMode.Indirect_X))
  add(Opcode(0x71, CPU::adc, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x29, CPU::and, 2, AddressingMode.Immediate))
  add(Opcode(0x25, CPU::and, 2, AddressingMode.ZeroPage))
  add(Opcode(0x35, CPU::and, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x2D, CPU::and, 3, AddressingMode.Absolute))
  add(Opcode(0x3D, CPU::and, 3, AddressingMode.Absolute_X))
  add(Opcode(0x39, CPU::and, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x21, CPU::and, 2, AddressingMode.Indirect_X))
  add(Opcode(0x31, CPU::and, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x0A, CPU::asl, 1, AddressingMode.Accumulator))
  add(Opcode(0x06, CPU::asl, 2, AddressingMode.ZeroPage))
  add(Opcode(0x16, CPU::asl, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x0E, CPU::asl, 3, AddressingMode.Absolute))
  add(Opcode(0x1E, CPU::asl, 3, AddressingMode.Absolute_X))

  add(Opcode(0x90, CPU::bcc, 2, AddressingMode.Relative))

  add(Opcode(0xB0, CPU::bcs, 2, AddressingMode.Relative))
  add(Opcode(0xF0, CPU::beq, 2, AddressingMode.Relative))

  add(Opcode(0x24, CPU::bit, 2, AddressingMode.ZeroPage))
  add(Opcode(0x2C, CPU::bit, 3, AddressingMode.Absolute))

  add(Opcode(0x30, CPU::bmi, 2, AddressingMode.Relative))
  add(Opcode(0xD0, CPU::bne, 2, AddressingMode.Relative))
  add(Opcode(0x10, CPU::bpl, 2, AddressingMode.Relative))
  add(Opcode(0x50, CPU::bvc, 2, AddressingMode.Relative))
  add(Opcode(0x70, CPU::bvs, 2, AddressingMode.Relative))

  add(Opcode(0x18, CPU::clc, 1, AddressingMode.NoneAddressing))
  add(Opcode(0xD8, CPU::cld, 1, AddressingMode.NoneAddressing))
  add(Opcode(0x58, CPU::cli, 1, AddressingMode.NoneAddressing))
  add(Opcode(0xB8, CPU::clv, 1, AddressingMode.NoneAddressing))

  add(Opcode(0xC9, CPU::cmp, 2, AddressingMode.Immediate))
  add(Opcode(0xC5, CPU::cmp, 2, AddressingMode.ZeroPage))
  add(Opcode(0xD5, CPU::cmp, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xCD, CPU::cmp, 3, AddressingMode.Absolute))
  add(Opcode(0xDD, CPU::cmp, 3, AddressingMode.Absolute_X))
  add(Opcode(0xD9, CPU::cmp, 3, AddressingMode.Absolute_Y))
  add(Opcode(0xC1, CPU::cmp, 2, AddressingMode.Indirect_X))
  add(Opcode(0xD1, CPU::cmp, 2, AddressingMode.Indirect_Y))

  add(Opcode(0xE0, CPU::cpx, 2, AddressingMode.Immediate))
  add(Opcode(0xE4, CPU::cpx, 2, AddressingMode.ZeroPage))
  add(Opcode(0xEC, CPU::cpx, 3, AddressingMode.Absolute))

  add(Opcode(0xC0, CPU::cpy, 2, AddressingMode.Immediate))
  add(Opcode(0xC4, CPU::cpy, 2, AddressingMode.ZeroPage))
  add(Opcode(0xCC, CPU::cpy, 3, AddressingMode.Absolute))

  add(Opcode(0xC6, CPU::dec, 2, AddressingMode.ZeroPage))
  add(Opcode(0xD6, CPU::dec, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xCE, CPU::dec, 3, AddressingMode.Absolute))
  add(Opcode(0xDE, CPU::dec, 3, AddressingMode.Absolute_X))

  add(Opcode(0xCA, CPU::dex, 1, AddressingMode.NoneAddressing))
  add(Opcode(0x88, CPU::dey, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x49, CPU::eor, 2, AddressingMode.Immediate))
  add(Opcode(0x45, CPU::eor, 2, AddressingMode.ZeroPage))
  add(Opcode(0x55, CPU::eor, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x4D, CPU::eor, 3, AddressingMode.Absolute))
  add(Opcode(0x5D, CPU::eor, 3, AddressingMode.Absolute_X))
  add(Opcode(0x59, CPU::eor, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x41, CPU::eor, 2, AddressingMode.Indirect_X))
  add(Opcode(0x51, CPU::eor, 2, AddressingMode.Indirect_Y))

  add(Opcode(0xE6, CPU::inc, 2, AddressingMode.ZeroPage))
  add(Opcode(0xF6, CPU::inc, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xEE, CPU::inc, 3, AddressingMode.Absolute))
  add(Opcode(0xFE, CPU::inc, 3, AddressingMode.Absolute_X))

  add(Opcode(0xE8, CPU::inxFun, 1, AddressingMode.NoneAddressing))
  add(Opcode(0xC8, CPU::iny, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x4C, CPU::jmp, 3, AddressingMode.Absolute))
  add(Opcode(0x6C, CPU::jmp, 3, AddressingMode.Indirect))

  add(Opcode(0x20, CPU::jsr, 3, AddressingMode.Absolute))

  add(Opcode(0xA9, CPU::lda, 2, AddressingMode.Immediate))
  add(Opcode(0xA5, CPU::lda, 2, AddressingMode.ZeroPage))
  add(Opcode(0xB5, CPU::lda, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xAD, CPU::lda, 3, AddressingMode.Absolute))
  add(Opcode(0xBD, CPU::lda, 3, AddressingMode.Absolute_X))
  add(Opcode(0xB9, CPU::lda, 3, AddressingMode.Absolute_Y))
  add(Opcode(0xA1, CPU::lda, 2, AddressingMode.Indirect_X))
  add(Opcode(0xB1, CPU::lda, 2, AddressingMode.Indirect_Y))

  add(Opcode(0xA2, CPU::ldx, 2, AddressingMode.Immediate))
  add(Opcode(0xA6, CPU::ldx, 2, AddressingMode.ZeroPage))
  add(Opcode(0xB6, CPU::ldx, 2, AddressingMode.ZeroPage_Y))
  add(Opcode(0xAE, CPU::ldx, 3, AddressingMode.Absolute))
  add(Opcode(0xBE, CPU::ldx, 3, AddressingMode.Absolute_Y))

  add(Opcode(0xA0, CPU::ldy, 2, AddressingMode.Immediate))
  add(Opcode(0xA4, CPU::ldy, 2, AddressingMode.ZeroPage))
  add(Opcode(0xB4, CPU::ldy, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xAC, CPU::ldy, 3, AddressingMode.Absolute))
  add(Opcode(0xBC, CPU::ldy, 3, AddressingMode.Absolute_X))

  add(Opcode(0x4A, CPU::lsr, 1, AddressingMode.Accumulator))
  add(Opcode(0x46, CPU::lsr, 2, AddressingMode.ZeroPage))
  add(Opcode(0x56, CPU::lsr, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x4E, CPU::lsr, 3, AddressingMode.Absolute))
  add(Opcode(0x5E, CPU::lsr, 3, AddressingMode.Absolute_X))

  add(Opcode(0xEA, CPU::nop, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x09, CPU::ora, 2, AddressingMode.Immediate))
  add(Opcode(0x05, CPU::ora, 2, AddressingMode.ZeroPage))
  add(Opcode(0x15, CPU::ora, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x0D, CPU::ora, 3, AddressingMode.Absolute))
  add(Opcode(0x1D, CPU::ora, 3, AddressingMode.Absolute_X))
  add(Opcode(0x19, CPU::ora, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x01, CPU::ora, 2, AddressingMode.Indirect_X))
  add(Opcode(0x11, CPU::ora, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x48, CPU::pha, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x08, CPU::php, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x68, CPU::pla, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x28, CPU::plp, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x2A, CPU::rol, 1, AddressingMode.Accumulator))
  add(Opcode(0x26, CPU::rol, 2, AddressingMode.ZeroPage))
  add(Opcode(0x36, CPU::rol, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x2E, CPU::rol, 3, AddressingMode.Absolute))
  add(Opcode(0x3E, CPU::rol, 3, AddressingMode.Absolute_X))

  add(Opcode(0x6A, CPU::ror, 1, AddressingMode.Accumulator))
  add(Opcode(0x66, CPU::ror, 2, AddressingMode.ZeroPage))
  add(Opcode(0x76, CPU::ror, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6E, CPU::ror, 3, AddressingMode.Absolute))
  add(Opcode(0x7E, CPU::ror, 3, AddressingMode.Absolute_X))

  add(Opcode(0x40, CPU::rti, 1, AddressingMode.NoneAddressing))
  add(Opcode(0x60, CPU::rts, 1, AddressingMode.NoneAddressing))

  add(Opcode(0xE9, CPU::sbc, 2, AddressingMode.Immediate))
  add(Opcode(0xE5, CPU::sbc, 2, AddressingMode.ZeroPage))
  add(Opcode(0xF5, CPU::sbc, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xED, CPU::sbc, 3, AddressingMode.Absolute))
  add(Opcode(0xFD, CPU::sbc, 3, AddressingMode.Absolute_X))
  add(Opcode(0xF9, CPU::sbc, 3, AddressingMode.Absolute_Y))
  add(Opcode(0xE1, CPU::sbc, 2, AddressingMode.Indirect_X))
  add(Opcode(0xF1, CPU::sbc, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x38, CPU::sec, 1, AddressingMode.NoneAddressing))
  add(Opcode(0xF8, CPU::sed, 1, AddressingMode.NoneAddressing))
  add(Opcode(0x78, CPU::sei, 1, AddressingMode.NoneAddressing))

  add(Opcode(0x85, CPU::sta, 2, AddressingMode.ZeroPage))
  add(Opcode(0x95, CPU::sta, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x8D, CPU::sta, 3, AddressingMode.Absolute))
  add(Opcode(0x9D, CPU::sta, 3, AddressingMode.Absolute_X))
  add(Opcode(0x99, CPU::sta, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x81, CPU::sta, 2, AddressingMode.Indirect_X))
  add(Opcode(0x91, CPU::sta, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x86, CPU::stx, 2, AddressingMode.ZeroPage))
  add(Opcode(0x96, CPU::stx, 2, AddressingMode.ZeroPage_Y))
  add(Opcode(0x8E, CPU::stx, 3, AddressingMode.Absolute))

  add(Opcode(0x84, CPU::sty, 2, AddressingMode.ZeroPage))
  add(Opcode(0x94, CPU::sty, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x8C, CPU::sty, 3, AddressingMode.Absolute))

  add(Opcode(0xAA, CPU::tax, 1, AddressingMode.NoneAddressing))
  add(Opcode(0xA8, CPU::tay, 1, AddressingMode.NoneAddressing))

  add(Opcode(0xBA, CPU::tsx, 1, AddressingMode.NoneAddressing))
  add(Opcode(0x9A, CPU::txs, 1, AddressingMode.NoneAddressing))
}