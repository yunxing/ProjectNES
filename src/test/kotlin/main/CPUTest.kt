package main

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
public class CPUTest {
  fun uByteListOf(vararg elements: Int): List<UByte> {
    return elements.map(Int::toUByte)
  }

  @Test
  fun testLDA() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0x05, 0x00))
    assertEquals(cpu.regA, 0x05.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, false)
  }

  @Test
  fun testLDAZero() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0x00, 0x00))
    assertEquals(cpu.regA, 0x00.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, true)
  }

  @Test
  fun testLDANeg() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0b1000_0001, 0x00))
    assertEquals(cpu.regA, 0b1000_0001.toUByte())
    assertEquals(cpu.status_n, true)
    assertEquals(cpu.status_z, false)
  }

  @Test
  fun testTAX() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0x5, 0xAA, 0x00))
    assertEquals(cpu.regA, 0x05.toUByte())
    assertEquals(cpu.regX, 0x05.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, false)
  }

  @Test
  fun testIntegration() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0xC0, 0xAA, 0xE8, 0x00))
    assertEquals(cpu.regX, 0xC1.toUByte())
  }

  @Test
  fun testINXOverflow() {
    val cpu = CPU()
    cpu.regX = 0xff.toUByte()
    cpu.execute(uByteListOf(0xa9, 0xff, 0xAA, 0xE8, 0xE8, 0x00), false)
    assertEquals(1.toUByte(), cpu.regX)
  }

  @Test
  fun testStoreLoadZeroPage() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x04, 0xB5, 0x03, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadZeroPageWrapped() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x00, 0xB5, 0xFF, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadAbsolute() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x01, 0xAD, 0x01, 0x00, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadAbsoluteY() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regY = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x01, 0xBD, 0x01, 0x00, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadIndirectX() {
    val cpu = CPU()
    cpu.regA = 0x01.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.mem[0x01] = 0xDB.toUByte()
    cpu.execute(uByteListOf(0x85, 0xFF, 0xA1, 0xFE, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadIndirectY() {
    val cpu = CPU()
    cpu.regA = 0x01.toUByte()
    cpu.regY = 0x01.toUByte()
    cpu.mem[0x02] = 0xDB.toUByte()
    cpu.execute(uByteListOf(0x85, 0xFF, 0xB1, 0xFF, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testADCOverflow() {
    val cpu = CPU()
    // Negative overflow: http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html.
    cpu.regA = 0xD0.toUByte()
    cpu.execute(uByteListOf(0x69, 0x90, 0x00), false)
    assertEquals(0x60.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_v)
    assertEquals(true, cpu.status_c)

    cpu.reset()
    // Positive overflow.
    cpu.regA = 0x50.toUByte()
    cpu.execute(uByteListOf(0x69, 0x50, 0x00), false)
    assertEquals(0xa0.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_v)
    assertEquals(false, cpu.status_c)

    cpu.reset()
    // Not overflow but carry bit set.
    cpu.regA = 0x50.toUByte()
    cpu.execute(uByteListOf(0x69, 0xd0, 0x00), false)
    assertEquals(0x20.toUByte(), cpu.regA)
    assertEquals(false, cpu.status_v)
    assertEquals(true, cpu.status_c)
  }

  @Test
  fun testAND() {
    val cpu = CPU()
    cpu.regA = 0x11.toUByte()
    cpu.execute(uByteListOf(0x29, 0x10, 0x00), false)
    assertEquals(0x10.toUByte(), cpu.regA)
  }

  @Test
  fun testASL() {
    val cpu = CPU()
    cpu.regA = 0b0100_0000.toUByte()
    cpu.execute(uByteListOf(0x0A, 0x00), false)
    assertEquals(0b1000_0000.toUByte(), cpu.regA)
    assertEquals(false, cpu.status_c)
  }

  @Test
  fun testASLOverflow() {
    val cpu = CPU()
    cpu.regA = 0b1000_0000.toUByte()
    cpu.execute(uByteListOf(0x0A, 0x00), false)
    assertEquals(0x00.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_c)
  }

  @Test
  fun testBCC() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_c = true
    cpu.execute(uByteListOf(0x90, 0x03, 0x0A, 0x0A, 0x00), false)
    // Skipped one shift, but should have done another shift.
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBCCNot() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_c = false
    cpu.execute(uByteListOf(0x90, 0x03, 0x0A, 0x0A, 0x00), false)
    // Not doing branch.
    assertEquals(0b0000_0100.toUByte(), cpu.regA)
  }

  @Test
  fun testBCS() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_c = false
    cpu.execute(uByteListOf(0xB0, 0x03, 0x0A, 0x0A, 0x00), false)
    // Skipped one shift, but should have done another shift.
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBCSNot() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_c = true
    cpu.execute(uByteListOf(0xB0, 0x03, 0x0A, 0x0A, 0x00), false)
    // Not doing branch.
    assertEquals(0b0000_0100.toUByte(), cpu.regA)
  }

  @Test
  fun testBEQ() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_z = true
    cpu.execute(uByteListOf(0xF0, 0x03, 0x0A, 0x0A, 0x00), false)
    // Skipped one shift, but should have done another shift.
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBIT() {
    val cpu = CPU()
    cpu.regA = 0b0100_0000.toUByte()
    cpu.mem[0x01] = 0b1111_0000.toUByte()
    cpu.execute(uByteListOf(0x24, 0x01, 0x00), false)
    assertEquals(cpu.status_v, true)
  }

  @Test
  fun testBMI() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_n = true
    cpu.execute(uByteListOf(0x30, 0x03, 0x0A, 0x0A, 0x00), false)
    // Skipped one shift, but should have done another shift.
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBNE() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_z = false
    cpu.execute(uByteListOf(0xD0, 0x03, 0x0A, 0x0A, 0x00), false)
    // Skipped one shift, but should have done another shift.
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBPL() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_n = false
    cpu.execute(uByteListOf(0x10, 0x03, 0x0A, 0x0A, 0x00), false)
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBVC() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_v = false
    cpu.execute(uByteListOf(0x50, 0x03, 0x0A, 0x0A, 0x00), false)
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testBVS() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.status_v = true
    cpu.execute(uByteListOf(0x70, 0x03, 0x0A, 0x0A, 0x00), false)
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testClears() {
    val cpu = CPU()
    cpu.status_c = true
    cpu.execute(uByteListOf(0x18, 0x00), false)
    assertEquals(false, cpu.status_c)

    cpu.status_d = true
    cpu.execute(uByteListOf(0xD8, 0x00), false)
    assertEquals(false, cpu.status_d)

    cpu.status_i = true
    cpu.execute(uByteListOf(0x58, 0x00), false)
    assertEquals(false, cpu.status_i)

    cpu.status_v = true
    cpu.execute(uByteListOf(0xB8, 0x00), false)
    assertEquals(false, cpu.status_v)
  }

  fun testCompareInternal(cpu: CPU, op: Int) {
    cpu.mem[0x0] = 0b0100_0000.toUByte()
    cpu.execute(uByteListOf(op, 0x00, 0x00), false)
    assertEquals(true, cpu.status_c)
    assertEquals(true, cpu.status_n)
    assertEquals(false, cpu.status_z)
    cpu.mem[0x0] = 0b1100_0000.toUByte()
    cpu.execute(uByteListOf(op, 0x00, 0x00), false)
    assertEquals(true, cpu.status_z)
  }

  @Test
  fun testCompare() {
    val cpu = CPU()
    cpu.regA = 0b1100_0000.toUByte()
    testCompareInternal(cpu, 0xC5)
  }

  @Test
  fun testCompareX() {
    val cpu = CPU()
    cpu.regX = 0b1100_0000.toUByte()
    testCompareInternal(cpu, 0xE4)
  }

  @Test
  fun testCompareY() {
    val cpu = CPU()
    cpu.regY = 0b1100_0000.toUByte()
    testCompareInternal(cpu, 0xC4)
  }

  @Test
  fun testDECOverflow() {
    val cpu = CPU()
    cpu.mem[0x00] = 0x00.toUByte()
    cpu.execute(uByteListOf(0xC6, 0x00, 0x00), false)
    assertEquals(0xFF.toUByte(), cpu.mem[0x00])
    assertEquals(true, cpu.status_n)
  }

  @Test
  fun testDECZero() {
    val cpu = CPU()
    cpu.mem[0x00] = 0x01.toUByte()
    cpu.execute(uByteListOf(0xC6, 0x00, 0x00), false)
    assertEquals(0x00.toUByte(), cpu.mem[0x00])
    assertEquals(true, cpu.status_z)
  }

  @Test
  fun testDEX() {
    val cpu = CPU()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0xCA, 0x00, 0x00), false)
    assertEquals(0x00.toUByte(), cpu.regX)
    assertEquals(true, cpu.status_z)
  }

  @Test
  fun testDEY() {
    val cpu = CPU()
    cpu.regY = 0x01.toUByte()
    cpu.execute(uByteListOf(0x88, 0x00, 0x00), false)
    assertEquals(0x00.toUByte(), cpu.regY)
    assertEquals(true, cpu.status_z)
  }

  @Test
  fun testEOR() {
    val cpu = CPU()
    cpu.regA = 0b1000_0000.toUByte()
    cpu.execute(uByteListOf(0x49, 0b0100_0000, 0x00), false)
    assertEquals(0b1100_0000.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_n)
  }

  @Test
  fun testINC() {
    val cpu = CPU()
    cpu.mem[0x00] = 0xFF.toUByte()
    cpu.execute(uByteListOf(0xE6, 0x00, 0x00), false)
    assertEquals(0x00.toUByte(), cpu.mem[0x00])
    assertEquals(true, cpu.status_z)
  }

  @Test
  fun testINY() {
    val cpu = CPU()
    cpu.regY = 0x00.toUByte()
    cpu.execute(uByteListOf(0xC8, 0x00, 0x00), false)
    assertEquals(0x01.toUByte(), cpu.regY)
  }

  @Test
  fun testJMPIndirect() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.mem[0x02] = 0x0A.toUByte()
    // BRK after 0x02.
    cpu.mem[0x03] = 0x00.toUByte()
    cpu.mem[0x00] = 0x02.toUByte()
    cpu.mem[0x01] = 0x00.toUByte()
    cpu.execute(uByteListOf(0x6C, 0x00, 0x00), false)
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testJMPPageBoundary() {
    val cpu = CPU()
    cpu.regA = 0b0000_0001.toUByte()
    cpu.mem[0x3000] = 0x40.toUByte()
    // BRK after 0x02.
    cpu.mem[0x30FF] = 0x80.toUByte()
    cpu.mem[0x3100] = 0x50.toUByte()
    cpu.mem[0x4080] = 0x0A.toUByte()
    cpu.mem[0x4081] = 0x00.toUByte()
    cpu.execute(uByteListOf(0x6C, 0xFF, 0x30), false)
    assertEquals(0b0000_0010.toUByte(), cpu.regA)
  }

  @Test
  fun testJSR() {
    val cpu = CPU()
    cpu.mem[0x0001] = 0x00.toUByte()
    cpu.execute(uByteListOf(0x20, 0x01, 0x00), false)
    assertEquals(0x80.toUByte(), cpu.mem[0x10FF])
    assertEquals(0x02.toUByte(), cpu.mem[0x10FE])
    assertEquals(0x10FD.toUShort(), cpu.sp)
  }

  @Test
  fun testLDXZero() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA2, 0x00, 0x00))
    assertEquals(cpu.regX, 0x00.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, true)
  }

  @Test
  fun testLDYZero() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA0, 0x00, 0x00))
    assertEquals(cpu.regY, 0x00.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, true)
  }

  @Test
  fun testLSR() {
    val cpu = CPU()
    cpu.regA = 0b0100_0000.toUByte()
    cpu.execute(uByteListOf(0x4A, 0x00), false)
    assertEquals(0b0010_0000.toUByte(), cpu.regA)
    assertEquals(false, cpu.status_c)
  }

  @Test
  fun testORA() {
    val cpu = CPU()
    cpu.regA = 0x01.toUByte()
    cpu.execute(uByteListOf(0x09, 0x10, 0x00), false)
    assertEquals(0x11.toUByte(), cpu.regA)
  }


  @Test
  fun testPHPPLA() {
    // Push status into stack and pull it into A.
    // See http://wiki.nesdev.com/w/index.php/Status_flags#The_B_flag
    val cpu = CPU()
    cpu.status_n = true
    cpu.execute(uByteListOf(0x08, 0x68), false)
    assertEquals(0xB0.toUByte(), cpu.regA)
  }

  @Test
  fun testPHAPLP() {
    // Reverse of testPHPPLA
    val cpu = CPU()
    cpu.regA = 0xB0u
    cpu.execute(uByteListOf(0x48, 0x28), false)
    assertEquals(true, cpu.status_n)
  }

  @Test
  fun testROL() {
    val cpu = CPU()
    cpu.regA = 0b1100_0001.toUByte()
    cpu.execute(uByteListOf(0x2A, 0x00), false)
    assertEquals(0b1000_0011.toUByte(), cpu.regA)
  }

  @Test
  fun testROR() {
    val cpu = CPU()
    cpu.regA = 0b1100_0001.toUByte()
    cpu.execute(uByteListOf(0x6A, 0x00), false)
    assertEquals(0b1110_0000.toUByte(), cpu.regA)
  }

  @Test
  fun testSets() {
    val cpu = CPU()
    cpu.status_c = false
    cpu.execute(uByteListOf(0x38, 0x00), false)
    assertEquals(true, cpu.status_c)

    cpu.status_d = false
    cpu.execute(uByteListOf(0xF8, 0x00), false)
    assertEquals(true, cpu.status_d)

    cpu.status_i = false
    cpu.execute(uByteListOf(0x78, 0x00), false)
    assertEquals(true, cpu.status_i)
  }

  @Test
  fun testSTXZeroPageY() {
    val cpu = CPU()
    cpu.regX = 0xDBu
    cpu.regY = 0x01u
    cpu.execute(uByteListOf(0x96, 0x01, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.mem[0x02])
  }

  @Test
  fun testSTYZeroPageX() {
    // reverse of previous test
    val cpu = CPU()
    cpu.regY = 0xDBu
    cpu.regX = 0x01u
    cpu.execute(uByteListOf(0x94, 0x01, 0x00), false)
    assertEquals(0xDB.toUByte(), cpu.mem[0x02])
  }

  @Test
  fun testTAY() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0x5, 0xA8, 0x00))
    assertEquals(cpu.regA, 0x05.toUByte())
    assertEquals(cpu.regY, 0x05.toUByte())
    assertEquals(cpu.status_n, false)
    assertEquals(cpu.status_z, false)
  }

  @Test
  fun testSPTransfers() {
    // Transfers x to sp.
    val cpu = CPU()
    cpu.regX = 0x80u
    cpu.execute(uByteListOf(0x9A, 0xBA, 0x00), false)
    assertEquals(0x80u.toUByte(), cpu.sp)
    assertEquals(0x80u.toUByte(), cpu.regX)
    assertEquals(true, cpu.status_n)
  }

  @Test
  fun testTXA() {
    // Transfers x to sp.
    val cpu = CPU()
    cpu.regX = 0x80u
    cpu.execute(uByteListOf(0x8A, 0x00), false)
    assertEquals(0x80u.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_n)
  }

  @Test
  fun testTYA() {
    // Transfers x to sp.
    val cpu = CPU()
    cpu.regY = 0x80u
    cpu.execute(uByteListOf(0x98, 0x00), false)
    assertEquals(0x80u.toUByte(), cpu.regA)
    assertEquals(true, cpu.status_n)
  }
}
