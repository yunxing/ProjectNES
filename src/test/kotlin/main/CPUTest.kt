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
    Assert.assertEquals(cpu.status['N'], false)
    Assert.assertEquals(cpu.status['Z'], false)
  }

  @Test
  fun testLDAZero() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0x00, 0x00))
    Assert.assertEquals(cpu.regA, 0x00.toUByte())
    Assert.assertEquals(cpu.status['N'], false)
    Assert.assertEquals(cpu.status['Z'], true)
  }

  @Test
  fun testLDANeg() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xa9, 0b1000_0001, 0x00))
    Assert.assertEquals(cpu.regA, 0b1000_0001.toUByte())
    Assert.assertEquals(cpu.status['N'], true)
    Assert.assertEquals(cpu.status['Z'], false)
  }

  @Test
  fun testTAX() {
    val cpu = CPU()
    cpu.execute(uByteListOf(0xA9, 0x5, 0xAA, 0x00))
    Assert.assertEquals(cpu.regA, 0x05.toUByte())
    Assert.assertEquals(cpu.regX, 0x05.toUByte())
    Assert.assertEquals(cpu.status['N'], false)
    Assert.assertEquals(cpu.status['Z'], false)
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
    cpu.execute(uByteListOf(0xa9, 0xff, 0xAA, 0xE8, 0xE8, 0x00))
    Assert.assertEquals(1.toUByte(), cpu.regX)
  }
}
