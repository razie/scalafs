package razie.fs.proto1

// identifies one or more files or paths
trait FQ {
  def list : List[P]
}

object FQ {
  def apply (s:String) = new SFQ(s) // TODO fs specific
  def apply (s:Seq[String]):LSFQ = new LSFQ(s.map(apply(_)).toList) // TODO fs specific
  def apply (s:P) = new PFQ(s) // TODO fs specific
  def apply (s:Seq[P]) = new LFQ(s.toList) // TODO fs specific
}

/** nothing */
object NOFQ extends FQ {
  def list : List[P] = Nil
}

/** simple id of a single file */
class SFQ (val name:String) extends FQ {
  def list : List[P] = List (P(name))
}

/** most complicated - a list of queries */
class LSFQ (val l:List[FQ]) extends FQ {
  def list : List[P] = l.flatMap(_.list)
}

/** list already known */
class LFQ (val l:List[P]) extends FQ {
  def list : List[P] = l
}

/** simple single file */
class PFQ (val p:P) extends FQ {
  def list : List[P] = List (p)
}

// basic abstract File. java.File + conversion from String
object P {
  def apply (s:String) : P = new JF(s) // TODO fs specific
}

class Cmd

// abstract actions on a path/file
trait FP {
  def mkdir  : CF[Boolean]
  def mkdirs : CF[Boolean]
  def rmdir  : CF[Boolean]
  
  def rm : CF[Boolean] = rm ()
  def rm (f:RmFlag*) : CF[Boolean]
  
  def ls : CF[List[P]] = ls (".*")
  def ls (pat:String, flags:LsFlag*) : CF[List[P]]

  def exists : CF[Boolean]
  //def find (pat:String = "") : CF[List[P]]
}

// abstract path/file
trait P extends FP {
  def name:String
  
  def toAbsolute : P
}

/** default wrapper over a java.io.File */
class JF (val name:String) extends P {
  
  val f = new java.io.File (name)
  
  override def mkdir  : CF[Boolean] = new CmdMkdir  (FQ(this))
  override def mkdirs : CF[Boolean] = new CmdMkdirs (FQ(this))
//  override def mkdirs : CF[Boolean] = { log ("mkdirs", this); new CTODO (f.mkdirs()) }
  override def rmdir  : CF[Boolean] = {
    log ("rmdir", this)
    assert (f.isDirectory)
    new CTODO(f.delete())
  }
  
  override def ls (pat:String, flags:LsFlag*) : CF[List[P]] = 
    new CTODO (f.list().filter (_.matches (pat)).map (P(_)).toList)
  
  override def rm (flags:RmFlag*) : CF[Boolean] = { 
    log ("rm", flags, this); 
    if (flags contains Flags.r)
      new CTODO (f.delete())
    else
      new CTODO (f.delete())
    }
  
  override def exists : CF[Boolean] = new CTODO (f.exists())
  
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
  var flags : List[Flag] = Nil
  
  def - (f:Flag) = { flags = f :: flags; this }
  
  def | [B] (right:CF[B]) : CF[B] = new Pipe[A,B] (this, right)

  //def > (dest:P) : CF[Unit] = new PipeToFile[A,Unit] (this, dest)
 
  def get : A
}

/* command with one parameter. 
 * 
 * TODO To apply to multiple parameters, just use map or flatMap */
trait CF1[A] extends CF[A] {
  var fq : FQ = NOFQ
  
  def apply (s:String) : CF[A] = { fq = FQ(s); this }
  def on (s:String) = apply(s)
  
}

/* command with two parameters */
trait CF2[A] extends CF[A] {
  var psrc, pdest = ""
  def apply (src:String, dest:String) : CF[A] = {psrc=src; pdest=dest; this }
  def on (src:String, dest:String) = apply (src, dest)
}

class CmdMkdir (q:FQ/* = NOFQ*/) extends CF1[Boolean] {
  fq = q
  
  def get : Boolean = { 
//    fq.list.map(doit(_)).foldLeft(true)(_ && _) getOrElse false
    fq.list.map(doit(_)).foldLeft(true)(_ && _) 
    }

  def doit (p:P): Boolean = mk(p)
  
  def mk (p:P) : Boolean = { 
    log ("mkdir", p); 
    try {
      p.asInstanceOf[JF].f.mkdir
    } catch {
      case e@_ => false
    }
  }
}

class CmdMkdirs (q:FQ/* = NOFQ*/) extends CmdMkdir (q) {
  override def mk (p:P) : Boolean = { 
    log ("mkdirs", p); 
    try {
      p.asInstanceOf[JF].f.mkdirs
    } catch {
      case e@_ => false
    }
  }
}

class CmdRm extends CF1[Boolean] {
  def get : Boolean = false
}

class CmdCp extends CF2[Boolean] {
  def get : Boolean = false
}

class Pipe[A,B] (val left:CF[A], val right:CF[B]) extends CF[B] {
  override def get : B = throw new UnsupportedOperationException
}

/** pipe the result of an operation to a file */
class PipeToFile[A] (val left:CF[A], val right:P) extends CF[Unit] {
  def get : Unit = { // TODO 1-1 this is too stupid
    val x = left.get
    
  }
}

class CDone[A] (val result : A) extends CF[A] {
  def get = result
  override def toString = result.toString
}

case class CTODO[A] (_result : A) extends CDone[A] (_result) 
case class CTODO1[A] (_result : A) extends CDone[A] (_result) with CF1[A] {
  override def get = _result
}

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
  implicit def toA [A] (s:CF[A]) : A = s.get
  
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
  def ls (pat:String, flags:LsFlag*) : CF[List[P]] = pwd.ls(pat, flags:_*)
//  def ls (pat:String) : CF[List[P]] = pwd.ls(pat)()
  
  def iecho (expr:String) : CF[Content] = echo (expr)
  def echo (expr:String) : CF[Content] // TODO expr 
  
  def mkdir  = new CmdMkdir (NOFQ)
  def rmdir  (s:String) = s.rmdir
  def rm     (p:String) = p.rm   
  def exists (s:String) = toP(s).exists
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
