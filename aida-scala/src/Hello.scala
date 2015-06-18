

/**
 * @author inheaven on 10.04.2015 23:25.
 */

object Hello extends App{
  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString

  val s = "\n                    "
  println(s.map(c => c.toInt))

}
