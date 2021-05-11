package main

data class Opcode(
  val opcode: UByte,
  val handler: (CPU, AddressingMode) -> Unit,
  val mnemonic: String,
  val bytes: UShort,
  val mode: AddressingMode
) {
  constructor(
    opcode: Int,
    handler: (CPU, AddressingMode) -> Unit,
    mnemonic: String,
    bytes: Int,
    mode: AddressingMode
  ) :
    this(opcode.toUByte(), handler, mnemonic, bytes.toUShort(), mode)
}

@kotlin.ExperimentalUnsignedTypes
val cpuOpcodes: List<Opcode> = mutableListOf<Opcode>().apply {
  add(Opcode(0x69, CPU::adc, "ADC", 2, AddressingMode.Immediate))
  add(Opcode(0x65, CPU::adc, "ADC", 2, AddressingMode.ZeroPage))
  add(Opcode(0x75, CPU::adc, "ADC", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6D, CPU::adc, "ADC", 3, AddressingMode.Absolute))
  add(Opcode(0x7D, CPU::adc, "ADC", 3, AddressingMode.Absolute_X))
  add(Opcode(0x79, CPU::adc, "ADC", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x61, CPU::adc, "ADC", 2, AddressingMode.Indirect_X))
  add(Opcode(0x71, CPU::adc, "ADC", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x29, CPU::and, "AND", 2, AddressingMode.Immediate))
  add(Opcode(0x25, CPU::and, "AND", 2, AddressingMode.ZeroPage))
  add(Opcode(0x35, CPU::and, "AND", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x2D, CPU::and, "AND", 3, AddressingMode.Absolute))
  add(Opcode(0x3D, CPU::and, "AND", 3, AddressingMode.Absolute_X))
  add(Opcode(0x39, CPU::and, "AND", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x21, CPU::and, "AND", 2, AddressingMode.Indirect_X))
  add(Opcode(0x31, CPU::and, "AND", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x0A, CPU::asl, "ASL", 1, AddressingMode.Accumulator))
  add(Opcode(0x06, CPU::asl, "ASL", 2, AddressingMode.ZeroPage))
  add(Opcode(0x16, CPU::asl, "ASL", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x0E, CPU::asl, "ASL", 3, AddressingMode.Absolute))
  add(Opcode(0x1E, CPU::asl, "ASL", 3, AddressingMode.Absolute_X))

  add(Opcode(0x90, CPU::bcc, "BCC", 2, AddressingMode.Relative))

  add(Opcode(0xB0, CPU::bcs, "BCS", 2, AddressingMode.Relative))
  add(Opcode(0xF0, CPU::beq, "BEQ", 2, AddressingMode.Relative))

  add(Opcode(0x24, CPU::bit, "BIT", 2, AddressingMode.ZeroPage))
  add(Opcode(0x2C, CPU::bit, "BIT", 3, AddressingMode.Absolute))

  add(Opcode(0x30, CPU::bmi, "BMI", 2, AddressingMode.Relative))
  add(Opcode(0xD0, CPU::bne, "BNE", 2, AddressingMode.Relative))
  add(Opcode(0x10, CPU::bpl, "BPL", 2, AddressingMode.Relative))
  add(Opcode(0x50, CPU::bvc, "BVC", 2, AddressingMode.Relative))
  add(Opcode(0x70, CPU::bvs, "BVS", 2, AddressingMode.Relative))

  add(Opcode(0x18, CPU::clc, "CLC", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xD8, CPU::cld, "CLD", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x58, CPU::cli, "CLI", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xB8, CPU::clv, "CLV", 1, AddressingMode.NoneAddressing))

  add(Opcode(0xC9, CPU::cmp, "CMP", 2, AddressingMode.Immediate))
  add(Opcode(0xC5, CPU::cmp, "CMP", 2, AddressingMode.ZeroPage))
  add(Opcode(0xD5, CPU::cmp, "CMP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xCD, CPU::cmp, "CMP", 3, AddressingMode.Absolute))
  add(Opcode(0xDD, CPU::cmp, "CMP", 3, AddressingMode.Absolute_X))
  add(Opcode(0xD9, CPU::cmp, "CMP", 3, AddressingMode.Absolute_Y))
  add(Opcode(0xC1, CPU::cmp, "CMP", 2, AddressingMode.Indirect_X))
  add(Opcode(0xD1, CPU::cmp, "CMP", 2, AddressingMode.Indirect_Y))

  add(Opcode(0xE0, CPU::cpx, "CPX", 2, AddressingMode.Immediate))
  add(Opcode(0xE4, CPU::cpx, "CPX", 2, AddressingMode.ZeroPage))
  add(Opcode(0xEC, CPU::cpx, "CPX", 3, AddressingMode.Absolute))

  add(Opcode(0xC0, CPU::cpy, "CPY", 2, AddressingMode.Immediate))
  add(Opcode(0xC4, CPU::cpy, "CPY", 2, AddressingMode.ZeroPage))
  add(Opcode(0xCC, CPU::cpy, "CPY", 3, AddressingMode.Absolute))

  add(Opcode(0xC6, CPU::dec, "DEC", 2, AddressingMode.ZeroPage))
  add(Opcode(0xD6, CPU::dec, "DEC", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xCE, CPU::dec, "DEC", 3, AddressingMode.Absolute))
  add(Opcode(0xDE, CPU::dec, "DEC", 3, AddressingMode.Absolute_X))

  add(Opcode(0xCA, CPU::dex, "DEX", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x88, CPU::dey, "DEY", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x49, CPU::eor, "EOR", 2, AddressingMode.Immediate))
  add(Opcode(0x45, CPU::eor, "EOR", 2, AddressingMode.ZeroPage))
  add(Opcode(0x55, CPU::eor, "EOR", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x4D, CPU::eor, "EOR", 3, AddressingMode.Absolute))
  add(Opcode(0x5D, CPU::eor, "EOR", 3, AddressingMode.Absolute_X))
  add(Opcode(0x59, CPU::eor, "EOR", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x41, CPU::eor, "EOR", 2, AddressingMode.Indirect_X))
  add(Opcode(0x51, CPU::eor, "EOR", 2, AddressingMode.Indirect_Y))

  add(Opcode(0xE6, CPU::inc, "INC", 2, AddressingMode.ZeroPage))
  add(Opcode(0xF6, CPU::inc, "INC", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xEE, CPU::inc, "INC", 3, AddressingMode.Absolute))
  add(Opcode(0xFE, CPU::inc, "INC", 3, AddressingMode.Absolute_X))

  add(Opcode(0xE8, CPU::inxFun, "INX",1, AddressingMode.NoneAddressing))
  add(Opcode(0xC8, CPU::iny, "INY", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x4C, CPU::jmp, "JMP", 3, AddressingMode.Absolute))
  add(Opcode(0x6C, CPU::jmp, "JMP", 3, AddressingMode.Indirect))

  add(Opcode(0x20, CPU::jsr, "JSR", 3, AddressingMode.Absolute))

  add(Opcode(0xA9, CPU::lda, "LDA", 2, AddressingMode.Immediate))
  add(Opcode(0xA5, CPU::lda, "LDA", 2, AddressingMode.ZeroPage))
  add(Opcode(0xB5, CPU::lda, "LDA", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xAD, CPU::lda, "LDA", 3, AddressingMode.Absolute))
  add(Opcode(0xBD, CPU::lda, "LDA", 3, AddressingMode.Absolute_X))
  add(Opcode(0xB9, CPU::lda, "LDA", 3, AddressingMode.Absolute_Y))
  add(Opcode(0xA1, CPU::lda, "LDA", 2, AddressingMode.Indirect_X))
  add(Opcode(0xB1, CPU::lda, "LDA", 2, AddressingMode.Indirect_Y))

  add(Opcode(0xA2, CPU::ldx, "LDX", 2, AddressingMode.Immediate))
  add(Opcode(0xA6, CPU::ldx, "LDX", 2, AddressingMode.ZeroPage))
  add(Opcode(0xB6, CPU::ldx, "LDX", 2, AddressingMode.ZeroPage_Y))
  add(Opcode(0xAE, CPU::ldx, "LDX", 3, AddressingMode.Absolute))
  add(Opcode(0xBE, CPU::ldx, "LDX", 3, AddressingMode.Absolute_Y))

  add(Opcode(0xA0, CPU::ldy, "LDY", 2, AddressingMode.Immediate))
  add(Opcode(0xA4, CPU::ldy, "LDY", 2, AddressingMode.ZeroPage))
  add(Opcode(0xB4, CPU::ldy, "LDY", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xAC, CPU::ldy, "LDY", 3, AddressingMode.Absolute))
  add(Opcode(0xBC, CPU::ldy, "LDY", 3, AddressingMode.Absolute_X))

  add(Opcode(0x4A, CPU::lsr, "LSR", 1, AddressingMode.Accumulator))
  add(Opcode(0x46, CPU::lsr, "LSR", 2, AddressingMode.ZeroPage))
  add(Opcode(0x56, CPU::lsr, "LSR", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x4E, CPU::lsr, "LSR", 3, AddressingMode.Absolute))
  add(Opcode(0x5E, CPU::lsr, "LSR", 3, AddressingMode.Absolute_X))

  add(Opcode(0xEA, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x1A, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x3A, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x5A, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x7A, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xDA, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xFA, CPU::nop, "NOP", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x09, CPU::ora, "ORA", 2, AddressingMode.Immediate))
  add(Opcode(0x05, CPU::ora, "ORA", 2, AddressingMode.ZeroPage))
  add(Opcode(0x15, CPU::ora, "ORA", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x0D, CPU::ora, "ORA", 3, AddressingMode.Absolute))
  add(Opcode(0x1D, CPU::ora, "ORA", 3, AddressingMode.Absolute_X))
  add(Opcode(0x19, CPU::ora, "ORA", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x01, CPU::ora, "ORA", 2, AddressingMode.Indirect_X))
  add(Opcode(0x11, CPU::ora, "ORA", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x48, CPU::pha, "PHA", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x08, CPU::php, "PHP", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x68, CPU::pla, "PLA", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x28, CPU::plp, "PLP", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x2A, CPU::rol, "ROL", 1, AddressingMode.Accumulator))
  add(Opcode(0x26, CPU::rol, "ROL", 2, AddressingMode.ZeroPage))
  add(Opcode(0x36, CPU::rol, "ROL", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x2E, CPU::rol, "ROL", 3, AddressingMode.Absolute))
  add(Opcode(0x3E, CPU::rol, "ROL", 3, AddressingMode.Absolute_X))

  add(Opcode(0x6A, CPU::ror, "ROR", 1, AddressingMode.Accumulator))
  add(Opcode(0x66, CPU::ror, "ROR", 2, AddressingMode.ZeroPage))
  add(Opcode(0x76, CPU::ror, "ROR", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6E, CPU::ror, "ROR", 3, AddressingMode.Absolute))
  add(Opcode(0x7E, CPU::ror, "ROR", 3, AddressingMode.Absolute_X))

  add(Opcode(0x40, CPU::rti, "RTI", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x60, CPU::rts, "RTS", 1, AddressingMode.NoneAddressing))

  add(Opcode(0xE9, CPU::sbc, "SBC", 2, AddressingMode.Immediate))
  add(Opcode(0xEB, CPU::sbc, "SBC", 2, AddressingMode.Immediate))
  add(Opcode(0xE5, CPU::sbc, "SBC", 2, AddressingMode.ZeroPage))
  add(Opcode(0xF5, CPU::sbc, "SBC", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xED, CPU::sbc, "SBC", 3, AddressingMode.Absolute))
  add(Opcode(0xFD, CPU::sbc, "SBC", 3, AddressingMode.Absolute_X))
  add(Opcode(0xF9, CPU::sbc, "SBC", 3, AddressingMode.Absolute_Y))
  add(Opcode(0xE1, CPU::sbc, "SBC", 2, AddressingMode.Indirect_X))
  add(Opcode(0xF1, CPU::sbc, "SBC", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x38, CPU::sec, "SEC", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xF8, CPU::sed, "SED", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x78, CPU::sei, "SEI", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x85, CPU::sta, "STA", 2, AddressingMode.ZeroPage))
  add(Opcode(0x95, CPU::sta, "STA", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x8D, CPU::sta, "STA", 3, AddressingMode.Absolute))
  add(Opcode(0x9D, CPU::sta, "STA", 3, AddressingMode.Absolute_X))
  add(Opcode(0x99, CPU::sta, "STA", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x81, CPU::sta, "STA", 2, AddressingMode.Indirect_X))
  add(Opcode(0x91, CPU::sta, "STA", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x86, CPU::stx, "STX", 2, AddressingMode.ZeroPage))
  add(Opcode(0x96, CPU::stx, "STX", 2, AddressingMode.ZeroPage_Y))
  add(Opcode(0x8E, CPU::stx, "STX", 3, AddressingMode.Absolute))

  add(Opcode(0x84, CPU::sty, "STY", 2, AddressingMode.ZeroPage))
  add(Opcode(0x94, CPU::sty, "STY", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x8C, CPU::sty, "STY", 3, AddressingMode.Absolute))

  add(Opcode(0xAA, CPU::tax, "TAX", 1, AddressingMode.NoneAddressing))
  add(Opcode(0xA8, CPU::tay, "TAY", 1, AddressingMode.NoneAddressing))

  add(Opcode(0xBA, CPU::tsx, "TSX", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x9A, CPU::txs, "TXS", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x8A, CPU::txa, "TXA", 1, AddressingMode.NoneAddressing))
  add(Opcode(0x98, CPU::tya, "TYA", 1, AddressingMode.NoneAddressing))

  add(Opcode(0x0B, CPU::aac, "AAC", 2, AddressingMode.Immediate))
  add(Opcode(0x2B, CPU::aac, "AAC", 2, AddressingMode.Immediate))

  add(Opcode(0x87, CPU::aax, "AAX", 2, AddressingMode.ZeroPage))
  add(Opcode(0x97, CPU::aax, "AAX", 2, AddressingMode.ZeroPage_Y))
  add(Opcode(0x83, CPU::aax, "AAX", 2, AddressingMode.Indirect_X))
  add(Opcode(0x8F, CPU::aax, "AAX", 3, AddressingMode.Absolute))

  add(Opcode(0x6B, CPU::arr, "ARR", 2, AddressingMode.Immediate))

  add(Opcode(0x4B, CPU::asr, "ASR", 2, AddressingMode.Immediate))
  add(Opcode(0xCB, CPU::axs, "AXS", 2, AddressingMode.Immediate))

  add(Opcode(0x04, CPU::dop, "DOP", 2, AddressingMode.ZeroPage))
  add(Opcode(0x14, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x34, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x44, CPU::dop, "DOP", 2, AddressingMode.ZeroPage))
  add(Opcode(0x54, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x64, CPU::dop, "DOP", 2, AddressingMode.ZeroPage))
  add(Opcode(0x74, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x80, CPU::dop, "DOP", 2, AddressingMode.Immediate))
  add(Opcode(0x82, CPU::dop, "DOP", 2, AddressingMode.Immediate))
  add(Opcode(0x89, CPU::dop, "DOP", 2, AddressingMode.Immediate))
  add(Opcode(0xC2, CPU::dop, "DOP", 2, AddressingMode.Immediate))
  add(Opcode(0xD4, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xE2, CPU::dop, "DOP", 2, AddressingMode.Immediate))
  add(Opcode(0xF4, CPU::dop, "DOP", 2, AddressingMode.ZeroPage_X))

  add(Opcode(0xE7, CPU::isc, "ISC", 2, AddressingMode.ZeroPage))
  add(Opcode(0xF7, CPU::isc, "ISC", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xEF, CPU::isc, "ISC", 3, AddressingMode.Absolute))
  add(Opcode(0xFF, CPU::isc, "ISC", 3, AddressingMode.Absolute_X))
  add(Opcode(0xFB, CPU::isc, "ISC", 3, AddressingMode.Absolute_Y))
  add(Opcode(0xE3, CPU::isc, "ISC", 2, AddressingMode.Indirect_X))
  add(Opcode(0xF3, CPU::isc, "ISC", 2, AddressingMode.Indirect_Y))

  add(Opcode(0xBB, CPU::lar, "LAR", 3, AddressingMode.Absolute_Y))

  add(Opcode(0x27, CPU::rla, "RLA", 2, AddressingMode.ZeroPage))
  add(Opcode(0x37, CPU::rla, "RLA", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x2F, CPU::rla, "RLA", 3, AddressingMode.Absolute))
  add(Opcode(0x3F, CPU::rla, "RLA", 3, AddressingMode.Absolute_X))
  add(Opcode(0x3B, CPU::rla, "RLA", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x23, CPU::rla, "RLA", 2, AddressingMode.Indirect_X))
  add(Opcode(0x33, CPU::rla, "RLA", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x67, CPU::rla, "RRA", 2, AddressingMode.ZeroPage))
  add(Opcode(0x77, CPU::rla, "RRA", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6F, CPU::rla, "RRA", 3, AddressingMode.Absolute))
  add(Opcode(0x7F, CPU::rla, "RRA", 3, AddressingMode.Absolute_X))
  add(Opcode(0x7B, CPU::rla, "RRA", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x63, CPU::rla, "RRA", 2, AddressingMode.Indirect_X))
  add(Opcode(0x73, CPU::rla, "RRA", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x07, CPU::slo, "SLO", 2, AddressingMode.ZeroPage))
  add(Opcode(0x17, CPU::slo, "SLO", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x0F, CPU::slo, "SLO", 3, AddressingMode.Absolute))
  add(Opcode(0x1F, CPU::slo, "SLO", 3, AddressingMode.Absolute_X))
  add(Opcode(0x1B, CPU::slo, "SLO", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x03, CPU::slo, "SLO", 2, AddressingMode.Indirect_X))
  add(Opcode(0x13, CPU::slo, "SLO", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x47, CPU::sre, "SRE", 2, AddressingMode.ZeroPage))
  add(Opcode(0x57, CPU::sre, "SRE", 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x4F, CPU::sre, "SRE", 3, AddressingMode.Absolute))
  add(Opcode(0x5F, CPU::sre, "SRE", 3, AddressingMode.Absolute_X))
  add(Opcode(0x5B, CPU::sre, "SRE", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x43, CPU::sre, "SRE", 2, AddressingMode.Indirect_X))
  add(Opcode(0x53, CPU::sre, "SRE", 2, AddressingMode.Indirect_Y))

  add(Opcode(0x9E, CPU::shx, "SHX", 3, AddressingMode.Absolute_Y))
  add(Opcode(0x9C, CPU::shy, "SHY", 3, AddressingMode.Absolute_X))

  add(Opcode(0x0C, CPU::top, "TOP", 3, AddressingMode.Absolute))
  add(Opcode(0x1C, CPU::top, "TOP", 3, AddressingMode.Absolute_X))
  add(Opcode(0x3C, CPU::top, "TOP", 3, AddressingMode.Absolute_X))
  add(Opcode(0x5C, CPU::top, "TOP", 3, AddressingMode.Absolute_X))
  add(Opcode(0x7C, CPU::top, "TOP", 3, AddressingMode.Absolute_X))
  add(Opcode(0xDC, CPU::top, "TOP", 3, AddressingMode.Absolute_X))
  add(Opcode(0xF4, CPU::top, "TOP", 3, AddressingMode.Absolute_X))

  add(Opcode(0x9B, CPU::xas, "XAS", 3, AddressingMode.Absolute_Y))
}