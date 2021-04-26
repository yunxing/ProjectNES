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

val cpuOpcodes: List<Opcode> = mutableListOf<Opcode>().apply {
  add(Opcode(0x69, CPU::adc, 2, AddressingMode.Immediate))
  add(Opcode(0x65, CPU::adc, 2, AddressingMode.ZeroPage))
  add(Opcode(0x75, CPU::adc, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x6D, CPU::adc, 3, AddressingMode.Absolute))
  add(Opcode(0x7D, CPU::adc, 3, AddressingMode.Absolute_X))
  add(Opcode(0x79, CPU::adc, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x61, CPU::adc, 2, AddressingMode.Indirect_X))
  add(Opcode(0x71, CPU::adc, 2, AddressingMode.Indirect_Y))


  add(Opcode(0xA9, CPU::lda, 2, AddressingMode.Immediate))
  add(Opcode(0xA5, CPU::lda, 2, AddressingMode.ZeroPage))
  add(Opcode(0xB5, CPU::lda, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0xAD, CPU::lda, 3, AddressingMode.Absolute))
  add(Opcode(0xBD, CPU::lda, 3, AddressingMode.Absolute_X))
  add(Opcode(0xB9, CPU::lda, 3, AddressingMode.Absolute_Y))
  add(Opcode(0xA1, CPU::lda, 2, AddressingMode.Indirect_X))
  add(Opcode(0xB1, CPU::lda, 2, AddressingMode.Indirect_Y))

  add(Opcode(0x85, CPU::sta, 2, AddressingMode.ZeroPage))
  add(Opcode(0x95, CPU::sta, 2, AddressingMode.ZeroPage_X))
  add(Opcode(0x8D, CPU::sta, 3, AddressingMode.Absolute))
  add(Opcode(0x9D, CPU::sta, 3, AddressingMode.Absolute_X))
  add(Opcode(0x99, CPU::sta, 3, AddressingMode.Absolute_Y))
  add(Opcode(0x81, CPU::sta, 2, AddressingMode.Indirect_X))
  add(Opcode(0x91, CPU::sta, 2, AddressingMode.Indirect_Y))

  add(Opcode(0xE8, CPU::inx, 1, AddressingMode.NoneAddressing))
//  add(Opcode(0xA5, CPU::lda, 2, 3, AddressingMode.NoneAddressing))
//  add(Opcode(0xB5, CPU::lda, 2, 4, AddressingMode.NoneAddressing))
//  add(Opcode(0xAD, CPU::lda, 3, 4, AddressingMode.NoneAddressing))
//  add(Opcode(0xBD, CPU::lda, 3, 4, AddressingMode.NoneAddressing))
//  add(Opcode(0xB9, CPU::lda, 3, 4, AddressingMode.NoneAddressing))
//  add(Opcode(0xA1, CPU::lda, 2, 6, AddressingMode.NoneAddressing))
//  add(Opcode(0xB1, CPU::lda, 2, 5, AddressingMode.NoneAddressing))

}