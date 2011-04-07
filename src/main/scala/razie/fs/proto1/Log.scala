package razie.fs.proto1

class Logger {
  def log (msg:String) = println ("LOG " + msg)
}

object log {
  val logger = new Logger
  def apply (parms:Any*) = logger.log (parms.map (tos(_)).mkString (" ,"))
  def tos (p:Any) : String = p match {
    case null => "null"
    case a : Array[_] => "[" + a.mkString(",") + "]"
    case a : Seq[_] => "[" + a.mkString(",") + "]"
    case _ => p.toString
  }
}
