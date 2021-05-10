package main

fun CPU.takeTrace() : String {
  var trace : String = ""
  val opcode = this.memRead(this.pc)

  trace += this.pc.toHex()
  trace += " "

  trace += "P:" + this.statusAsUByte().toHex()
  return trace
}