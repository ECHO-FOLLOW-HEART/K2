package controllers.bache

import java.io.{ File, PrintWriter }

/**
 * Created by topy on 2015/7/15.
 */
object BatchUtils extends App {
  main(args)

  override def main(args: Array[String]): Unit = {
    val content = Seq("ff", "df")
    makeConfFile(content)
  }

  def makeConfFile(contents: Seq[String]): Unit = {
    val root = System.getProperty("user.dir")
    val path = "\\conf\\"
    val fileName = "expertTrack.conf"
    val file = root + path + fileName
    val writer = new PrintWriter(new File(file))
    contents.map(writer.println(_))
    writer.close()
  }

  def writeLine(key: String, value: String): String = {
    key + " " + "=" + " " + value
  }
}
