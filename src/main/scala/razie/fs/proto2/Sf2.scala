package razie.fs.proto2

import java.io._
import com.razie.pub.util.Files
import java.net.MalformedURLException

object O { // TODO 2.8.1
  def apply[A](x: A): Option[A] = if (x == null) None else Some(x)
}

// screw println () 

class Indented(val indent: String) {
  def <<(s: Any) = { print (s.toString()); this }
  def <<<(s: Any) = { println (indent + s.toString()); this }
}
object out extends Indented("") {
  def indented(indent: String) = new Indented(indent)
  def + (indent: String) = new Indented(indent)
}

// abstract actions on a path/file
trait FPBase { self : FP =>
  def mkdir: CF[Boolean]
  def mkdirs: CF[Boolean]
  def rmdir: CF[Boolean]

  def rm: CF[Boolean] = rm()
  def rm(f: RmFlag*): CF[Boolean]

  def renameTo(o: FP): CF[Boolean]
  def copyTo(o: FP): CF[Boolean]
  def >> (other: FP) = { println ("------------>"); copyTo(other)}
  def << (other: FP) = other >> this

  def ls: CF[List[FP]] = ls(".*")
  def ls(pat: String, flags: LsFlag*): CF[List[FP]]

  def exists: Boolean
  def isDirectory: Boolean
  def parentFile: Option[FP]
  //def find (pat:String = "") : CF[List[FP]]

  def is: InputStream
  def os: OutputStream
}

// abstract path/file
trait FP extends FPBase {
  def name: String
  def path: String
  def absolute: FP
  def sub(subDir: String): FP

  def /(s: String) = sub(s)
}

// basic abstract File. java.File + conversion from String
object FP {
  def apply(s: String): FP = new JF(s) // TODO fs specific
  def apply(f: File): FP = new JF(f) // TODO fs specific
}

// identifies one or more files or paths
trait FQ {
  def list: List[FP]

  def ++(other: FQ) = new LSFQ(this :: other :: Nil)
  //TODO def --(other: FQ) = new LSFQ(this :: other :: Nil)
  def ::(other: FQ) = other ++ this

  def exists: Boolean = list.foldLeft(true)(_ && _.exists)
  def copyTo(other: FP) = new CmdCp(this, other)
  def rm: CF[Boolean] = rm()
  def rm(f: RmFlag*): CF[Boolean] = new CFlatten[Boolean](list.map(_.rm))(true)(_ && _)
  def mkdir: CF[Boolean] = new CFlatten[Boolean](list.map(_.mkdir))(true)(_ && _)

  def ->(other: FP) = copyTo(other)
}

object FQ {
  def apply(s: String) = new SFQ(s) // TODO fs specific
  def apply(s: Seq[String]): LSFQ = new LSFQ(s.map(apply(_)).toList) // TODO fs specific
  def apply(s: FP) = new FPFQ(s) // TODO fs specific
  def apply(s: Seq[FP]) = new LFQ(s.toList) // TODO fs specific
}

/** nothing */
object NOFQ extends FQ {
  def list: List[FP] = Nil
}

/** simple id of a single file */
class SFQ(val name: String) extends FQ {
  def list: List[FP] = List(FP(name))
}

/** most complicated - a list of queries */
case class LSFQ(val l: List[FQ]) extends FQ {
  def list: List[FP] = l.flatMap(_.list)
}

/** list already known */
case class LFQ(ll: List[FP]) extends LFQBase(ll)
class LFQBase(val l: List[FP]) extends FQ {
  def list: List[FP] = l
}

/** simple single file */
case class FPFQ(val p: FP) extends FQ {
  def list: List[FP] = List(p)
}

/** string contains a list of names separated by comma */
case class MFQ(ll: String) extends LFQBase(ll.split(",").map(FP(_)).toList)

/** pattern */
case class PatFQ(val pattern: String) extends FQ {
  def list: List[FP] = List() // TODO
}

class Cmd

/** default wrapper over a java.io.File */
class JF(val f: File) extends FP {
  //  val f = if (_f != null) _f else new File("") // TODO what the heck if f is null?
  def name = f.getName
  def path = f.getAbsolutePath

  def this(s: String) = this(new File(s))

  override def mkdir: CF[Boolean] = new CDO(new CmdMkdir(FQ(this)))
  override def mkdirs: CF[Boolean] = new CDO(new CmdMkdirs(FQ(this)))
  //  override def mkdirs : CF[Boolean] = { log ("mkdirs", this); new CTODO (f.mkdirs()) }
  override def rmdir: CF[Boolean] = {
    log("rmdir", this)
    assert(f.isDirectory)
    new CTODO(f.delete())
  }

  override def renameTo(other: FP): CF[Boolean] = {
    log("renameTo", this, "->", other)
    new CTODO(f.renameTo(other.asInstanceOf[JF].f)) // TODO fix
  }

  override def copyTo(other: FP): CF[Boolean] = new CmdCp(FQ(this), other)

  override def sub(subDir: String): FP = {
    // CANNOT assert since paths can be prepared before dirs created... assert(f.isDirectory, f.getAbsolutePath())
    FP(this.path + (if (subDir.startsWith("/") || subDir.startsWith("\\")) "" else "/") + subDir)
  }

  override def ls(pat: String, flags: LsFlag*): CF[List[FP]] =
    new CTODO(f.list().filter(_.matches(pat)).map(FP(_)).toList)

  override def rm(flags: RmFlag*): CF[Boolean] = {
    log("rm", flags, this);
    if (flags contains Flags.r)
      new CTODO(f.delete())
    else
      new CTODO(f.delete())
  }

  override def exists: Boolean = f.exists()
  override def isDirectory: Boolean = f.isDirectory()
  override def parentFile: Option[FP] = O(f.getParentFile()).map(FP(_))

  override def absolute: FP = O(f.getAbsolutePath()).map(FP(_)).getOrElse(null)

  override def is: InputStream = f.toURI().toURL().openStream()
  override def os: OutputStream = new FileOutputStream(f);

  override def toString: String = f.toString
}

trait Content {
  def textContent: String
  def binContent: Boolean
}

case class SContent(val textContent: String) extends Content {
  def binContent: Boolean = false
  override def toString = textContent
}

// basic command handle
trait CF[A] {

  // evaluate asap if not collecting
  def donow = if (!CF.collecting) result

  var flags: List[Flag] = Nil

  def -(f: Flag) = { flags = f :: flags; this }

  def |[B](right: CF[B]): CF[B] = new FPipe(this, right)

  def >(dest: FP): CF[Unit] = new FPipeToFile(this, dest)

  def result: A
  def done: Boolean = !CF.collecting
}

object CF {
  def collecting: Boolean = false // Future collecting commands rather than running on the spot
}

/* command with one parameter. 
 * 
 * TODO To apply to multiple parameters, just use map or flatMap */
trait CF1[A] extends CF[A] {
  var fq: FQ = NOFQ

  def apply(s: String): CF[A] = { fq = FQ(s); new CDO(this) }
  def on(s: String) = apply(s)

}

/* command with two parameters like cp */
trait CF2[A] extends CF[A] {
  var psrc: FQ = NOFQ
  var pdest = ""
  def apply(src: FQ, dest: String): CF[A] = { psrc = src; pdest = dest; new CDO(this) }
  def on(src: FQ, dest: String) = apply(src, dest)
}

class CDone[A](val _result: A) extends CF[A] {
  override def done = true
  override def result = _result
  override def toString = _result.toString
}

case class CTODO[A](__result: A) extends CDone[A](__result)
case class CDO[A](_cmd: CF[A]) extends CDone[A](_cmd.result)
case class CTODO1[A](__result: A) extends CDone[A](__result) with CF1[A]

class CList[A](val cmds: List[CF[A]])() extends CDone[List[A]](cmds.map(_.result).toList)
class CFlatten[A](val cmds: List[CF[A]])(start: A)(f: (A, A) => A) extends CDone[A](cmds.map(_.result).toList.foldLeft(start)(f))

class CmdMkdir(q: FQ /* TODO 2.8.1 = NOFQ*/ ) extends CF1[Boolean] {
  fq = q
  donow

  def result: Boolean = {
    //    fq.list.map(doit(_)).foldLeft(true)(_ && _) getOrElse false
    fq.list.map(doit(_)).foldLeft(true)(_ && _)
  }

  def doit(p: FP): Boolean = mk(p)

  def mk(p: FP): Boolean = {
    log("mkdir", p);
    try {
      p.asInstanceOf[JF].f.mkdir
    } catch {
      case e @ _ => false
    }
  }
}

class CmdMkdirs(q: FQ /* TODO 2.8.1 = NOFQ*/ ) extends CmdMkdir(q) {
  donow

  override def mk(p: FP): Boolean = {
    log("mkdirs", p);
    try {
      p.asInstanceOf[JF].f.mkdirs
    } catch {
      case e @ _ => false
    }
  }
}

class CmdRm extends CF1[Boolean] {
  donow

  def result: Boolean = false
}

class CmdCp(q: FQ, to: FP) extends CF2[Boolean] {
  donow

  def copyLocal(fileSrc: FP, fileDest: FP) = {
    log("CopyLocal", fileSrc, " -> ", fileDest)
    try {
      val is = fileSrc.is
      // do the tragic ...
      val (parent, dest) =
        if (fileDest.exists && fileDest.isDirectory)
          (O(fileDest), fileDest / fileSrc.asInstanceOf[JF].f.getName)
        else
          (fileDest.parentFile, fileDest)

      val destTmpFile = FP(dest.path + ".TMFP")
      parent.filter(!_.exists).foreach(_.mkdirs)

      destTmpFile.asInstanceOf[JF].f.createNewFile()

      val os = destTmpFile.os

      Files.copyStream(is, os)

      if (!destTmpFile.renameTo(dest).result) {
        dest.rm
        if (!destTmpFile.renameTo(dest).result) {
          throw new RuntimeException("Cannot rename...");
        }
      }
    } catch {
      case e: MalformedURLException => {
        val iex = new IllegalArgumentException();
        iex.initCause(e);
        throw iex;
      }
      case e1: IOException =>
        throw new RuntimeException("Copy from: " + fileSrc + " to: " + fileDest, e1);
    }
  }

  /** copy a directory, recursively */
  def copyDir(srcDir: FP, targetDir: FP, recurse: Boolean, exclusions: Array[String] /* TODO 2.8.1 = Array() */ ) {
    log("CopyDir", srcDir, " -> ", targetDir)
    if (!targetDir.exists)
      targetDir.mkdirs

    // TODO counht files and size and mark % complete while copying
    // TODO also copy only files that actually changed - check date/time?
    val files = srcDir.ls.result
    for (file <- files) {
      if (!exclusions.foldLeft(false) { (b: Boolean, s: String) => b || file.name.matches(s) }) {
        if (file.isDirectory && recurse)
          copyDir(srcDir / file.name, targetDir / file.name, true, Array());
        else
          copyLocal(srcDir / file.name, targetDir / file.name);
      }
    }
  }

  def result: Boolean = {
    q.list.foreach { x =>
      if (x.isDirectory)
        copyDir(x, to, true, Array())
      else copyLocal(x, to)
    }
    true
  }
}

class FPipe[A, B](val left: CF[A], val right: CF[B]) extends CF[B] {
  override def result: B = throw new UnsupportedOperationException
}

class FPipeToFile[A](val left: CF[A], val file: FP) extends CF[Unit] {
  if (left.done)
    result

  def result: Unit = { //TODO 1-1 optimize
    val x = left.result
    if (!file.exists)
      file.asInstanceOf[JF].f.createNewFile
    Files.copyStream(new StringBufferInputStream(x.toString), file.os)
  }
}

trait Env {
  def apply(name: String): String
  def update(name: String, value: String): String
}

class DefaultEnv extends Env {
  val m = new scala.collection.mutable.HashMap[String, String]()
  override def apply(name: String): String = m(name)
  override def update(name: String, value: String): String = m.put(name, value).get
}

trait Shell {
  implicit def toFP(s: String): FP = FP(s)
  implicit def toE(s: String): E = new E(s) // convert any simple string into an echo
  implicit def toA[A](s: CF[A]): A = s.result

  class E(s: String) {
    def echo: CF[Content] = iecho(s)
  }

  def env: Env
  def $0: String
  def `$?`: String
  def $(name: String): String

  def pwd: FP
  def cd(to: FP): FP

  def ls: CF[List[FP]] = ls(".*")
  def ls(pat: String, flags: LsFlag*): CF[List[FP]] = pwd.ls(pat, flags: _*)
  //  def ls (pat:String) : CF[List[FP]] = pwd.ls(pat)()

  def iecho(expr: => String): CF[Content] = echo(expr)
  def echo(expr: => String): CF[Content] // TODO expr 

  def mkdir = new CmdMkdir(NOFQ)
  def rmdir(s: String) = s.rmdir
  def rm(p: String) = p.rm
  def exists(s: String) = toFP(s).exists

  def cp(src: FP, dest: FP) = new CmdCp(FQ(src), dest)
  def cp(src: FQ, dest: FP) = new CmdCp(src, dest)
}

object DefaultShell extends Shell {
  val env: Env = new DefaultEnv() // TODO populate with curent env
  var $0: String = ""
  var `$?`: String = ""
  def $(name: String): String = env(name)

  var cwd: FP = FP(".")
  def jwd: JF = cwd.asInstanceOf[JF] // TODO remove - for quick debug only
  def pwd: FP = cwd.absolute

  override def cd(to: FP): FP = {
    cwd = to
    pwd
  }

  override def echo(expr: => String): CF[Content] = { // TODO expr 
    new CTODO(SContent(expr))
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
    assert(FP("dir2/dir21/dir211").exists)
  }
  def u3_echo = {
    "gigi".echo
  }
}
