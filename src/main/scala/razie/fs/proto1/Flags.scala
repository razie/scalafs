package razie.fs.proto1

object Flags {
  case class Flag (f:String) {
    override def toString = f
  }
  object a extends Flag ("a")
  object l extends Flag ("l")
  object r extends Flag ("r")
  object p extends Flag ("p")
  object t extends Flag ("t")
}

