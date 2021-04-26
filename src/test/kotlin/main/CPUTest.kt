package main

import org.junit.Assert;
import org.junit.Test;

@ExperimentalUnsignedTypes
public class CPUTest {
  fun uByteListOf(vararg elements: Int): List<UByte> {
    return elements.map(Int::toUByte)
  }
  @Test
  fun testLDA() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0x05, 0x00))
    Assert.assertEquals(cpu.regA, 0x05.toUByte())
    Assert.assertEquals(cpu.status_n, false)
    Assert.assertEquals(cpu.status_z, false)
  }

  @Test
  fun testLDAZero() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0x00, 0x00))
    Assert.assertEquals(cpu.regA, 0x00.toUByte())
    Assert.assertEquals(cpu.status_n, false)
    Assert.assertEquals(cpu.status_z, true)
  }

  @Test
  fun testLDANeg() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0b1000_0001, 0x00))
    Assert.assertEquals(cpu.regA, 0b1000_0001.toUByte())
    Assert.assertEquals(cpu.status_n, true)
    Assert.assertEquals(cpu.status_z, false)
  }

  @Test
  fun testTAX() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0x5, 0xAA, 0x00))
    Assert.assertEquals(cpu.regA, 0x05.toUByte())
    Assert.assertEquals(cpu.regX, 0x05.toUByte())
    Assert.assertEquals(cpu.status_n, false)
    Assert.assertEquals(cpu.status_z, false)
  }

  @Test
  fun testIntegration() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0xC0, 0xAA, 0xE8, 0x00))
    Assert.assertEquals(cpu.regX, 0xC1.toUByte())
  }

  @Test
  fun testINXOverflow() {
    val cpu = CPU()
    cpu.regX = 0xff.toUByte()
    cpu.execute(uByteListOf(0xa9, 0xff, 0xAA, 0xE8, 0xE8, 0x00), false)
    Assert.assertEquals(1.toUByte(), cpu.regX)
  }

  @Test
  fun testStoreLoadZeroPage() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x04, 0xB5, 0x03, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadZeroPageWrapped() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x00, 0xB5, 0xFF, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadAbsolute() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x01, 0xAD, 0x01, 0x00, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadAbsoluteY() {
    val cpu = CPU()
    cpu.regA = 0xDB.toUByte()
    cpu.regY = 0x01.toUByte()
    cpu.execute(uByteListOf(0x85, 0x01, 0xBD, 0x01, 0x00, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadIndirectX() {
    val cpu = CPU()
    cpu.regA = 0x01.toUByte()
    cpu.regX = 0x01.toUByte()
    cpu.mem[0x01] = 0xDB.toUByte()
    cpu.execute(uByteListOf(0x85, 0xFF, 0xA1, 0xFE, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testStoreLoadIndirectY() {
    val cpu = CPU()
    cpu.regA = 0x01.toUByte()
    cpu.regY = 0x01.toUByte()
    cpu.mem[0x02] = 0xDB.toUByte()
    cpu.execute(uByteListOf(0x85, 0xFF, 0xB1, 0xFF, 0x00), false)
    Assert.assertEquals(0xDB.toUByte(), cpu.regA)
  }

  @Test
  fun testADCOverflow() {
    val cpu = CPU()
    // Negative overflow: http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html.
    cpu.regA = 0xD0.toUByte()
    cpu.execute(uByteListOf(0x69, 0x90, 0x00), false)
    Assert.assertEquals(0x60.toUByte(), cpu.regA)
    Assert.assertEquals(true, cpu.status_v)
    Assert.assertEquals(true, cpu.status_c)

    cpu.reset()
    // Positive overflow.
    cpu.regA = 0x50.toUByte()
    cpu.execute(uByteListOf(0x69, 0x50, 0x00), false)
    Assert.assertEquals(0xa0.toUByte(), cpu.regA)
    Assert.assertEquals(true, cpu.status_v)
    Assert.assertEquals(false, cpu.status_c)

    cpu.reset()
    // Not overflow but carry bit set.
    cpu.regA = 0x50.toUByte()
    cpu.execute(uByteListOf(0x69, 0xd0, 0x00), false)
    Assert.assertEquals(0x20.toUByte(), cpu.regA)
    Assert.assertEquals(false, cpu.status_v)
    Assert.assertEquals(true, cpu.status_c)
  }
}
