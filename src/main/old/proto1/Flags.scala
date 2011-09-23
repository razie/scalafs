package razie.fs.proto1

trait BaseFlag {
  val f:String
  override def toString = f
}

case class Flag (f:String) extends BaseFlag

trait RmFlag extends Flag
trait CpFlag extends Flag
trait LsFlag extends Flag

object Flags {
  object a extends Flag ("a") with LsFlag
  object l extends Flag ("l")
  object r extends Flag ("r") with RmFlag with CpFlag
  object f extends Flag ("f") with RmFlag 
  object p extends Flag ("p")
  object t extends Flag ("t")
}

  object a extends Flag ("a") with LsFlag
  object l extends Flag ("l")
  object r extends Flag ("r") with RmFlag with CpFlag
  object f extends Flag ("f") with RmFlag
  object p extends Flag ("p")
  object t extends Flag ("t")