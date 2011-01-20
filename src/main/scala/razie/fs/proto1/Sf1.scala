package razie.fs.proto1

// basic abstract File. java.File + conversion from String
object P {
  def apply (s:String) : P = new JF(s) // TODO fs specific
}

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

// abstract actions on a path/file
trait FP {
  def mkdir  : CF[Boolean]
  def mkdirs : CF[Boolean]
  def rmdir  : CF[Boolean]
  
  def rm : CF[Boolean] = rm ()
  def rm (f:Flags.Flag*) : CF[Boolean]
  
  def ls : CF[List[P]] = ls (".*")
  def ls (pat:String, flags:Flags.Flag*) : CF[List[P]]

  def exists : Boolean
  //def find (pat:String = "") : CF[List[P]]
}

// abstract path/file
trait P extends FP {
  def name:String
  
  def toAbsolute : P
}

class JF (val name:String) extends P {
  import scala.collection.JavaConversions._
  
  val f = new java.io.File (name)
  
  override def mkdir  : CF[Boolean] = { log ("mkdir", this); new CTODO (f.mkdir()) }
  override def mkdirs : CF[Boolean] = { log ("mkdirs", this); new CTODO (f.mkdirs()) }
  override def rmdir  : CF[Boolean] = {
    log ("rmdir", this)
    assert (f.isDirectory)
    new CTODO(f.delete())
  }
  
  override def ls (pat:String, flags:Flags.Flag*) : CF[List[P]] = 
    new CTODO (f.list().filter (_.matches (pat)).map (P(_)).toList)
  
  override def rm (flags:Flags.Flag*) : CF[Boolean] = { 
    log ("rm", flags, this); 
    if (flags contains Flags.r)
      new CTODO (f.delete())
    else
      new CTODO (f.delete())
    }
  
  override def exists : Boolean = f.exists
  
  override def toAbsolute : P = P(f.getAbsolutePath)
  
  override def toString : String = f.toString
}

trait Content {
  def textContent : String
  def binContent : Boolean
}

case class SContent (val textContent : String) extends Content {
  def binContent : Boolean = false
  override def toString = textContent
}

// basic command handle
trait CF[A] {
  def | [B] (right:CF[B]) : CF[B] = new Pipe[A,B] (this, right)
//  def > (dest:P) : CF[Unit] = new PipeToFile[A,Unit] (this, dest)
  def get : A
}

class Pipe[A,B] (val left:CF[A], val right:CF[B]) extends CF[B] {
  override def get : B = throw new UnsupportedOperationException
}

class PipeToFile[A] (val left:CF[A], val right:P) extends CF[Unit] {
  def get : Unit = throw new UnsupportedOperationException
}

class CDone[A] (val result : A) extends CF[A] {
  def get = result
  override def toString = result.toString
}

case class CTODO[A] (_result : A) extends CDone[A] (_result) 

trait Env {
  def apply (name : String) : String
  def update (name:String, value:String) : String
}

class DefaultEnv extends Env {
  val m = new scala.collection.mutable.HashMap[String,String]()
  override def apply (name : String) : String = m(name)
  override def update (name:String, value:String) : String = m.put(name, value).get
}

trait Shell {
  implicit def toP (s:String) : P = P(s)
  implicit def toE (s:String) : E = new E(s)
  
  class E (s:String) {
    def echo : CF[Content] = iecho (s)
  }
  
  def env : Env 
  def $0 : String 
  def `$?` : String 
  def $ (name:String) : String 

  def pwd : P
  def cd (to:P) : P 
  
  def ls : CF[List[P]] = ls (".*")
  def ls (pat:String, flags:Flags.Flag*) : CF[List[P]] = pwd.ls(pat, flags:_*)
//  def ls (pat:String) : CF[List[P]] = pwd.ls(pat)()
  
  def iecho (expr:String) : CF[Content] = echo (expr)
  def echo (expr:String) : CF[Content] // TODO expr 
}

object DefaultShell extends Shell {
  val env : Env = new DefaultEnv() // TODO populate with curent env
  var $0 : String = ""
  var `$?` : String = ""
  def $ (name:String) : String = env(name)

  var cwd : P = P(".")
  def jwd : JF = cwd.asInstanceOf[JF] // TODO remove - for quick debug only
  def pwd : P = cwd.toAbsolute
  
  override def cd (to:P) : P = {
    cwd = to
    pwd
  }
  
  override def echo (expr:String) : CF[Content] = { // TODO expr 
    new CTODO (SContent(expr))
  }
}

object UseCases {
  import DefaultShell._
  
  def u1_pwd = {
    pwd
  }
  def u2_mkd = {
    "dir1".mkdir
  }
  def u3_mkd = {
    "dir2/dir21/dir211".mkdirs
    assert (P("dir2/dir21/dir211").exists)
  }
  def u3_echo = {
    "gigi".echo
  }
}
