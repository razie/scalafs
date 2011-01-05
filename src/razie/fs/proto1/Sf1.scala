package razie.fs

// basic abstract File. java.File + conversion from String
trait F 

// basic command handle
trait CF[A] {
  def | [B] (right:CF[B]) : CF[B]
}

trait Env {
  def apply (name : String) : String
  def update (name:String, value:String) : String
}

trait fs {
  def $0
  def `$?`

  def env : Env
  
  def pwd : CF[F]
  def cd (to:F) : CF[F]
  def ls (pat:String = "") : CF[List[F]]
  def rm (to:F) : CF[F]
}

trait fs {
  def $0
  def `$?`

  def env : Env
  
  def pwd : CF[F]
  def cd (to:F) : CF[F]
  def ls (pat:String = "") : CF[List[F]]
  def rm (to:F) : CF[F]
}

class Sf1 extends fs {

}


object UseCases {
  import Sf1._
  
  def u1_pwd = {
    pwd
  }
}