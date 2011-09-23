package razie.fs.proto2

object Sf2App extends App {
  import DefaultShell._
  import Flags._

  val > = DefaultShell
  
  log ("pwd", pwd)
  
  log ("ls", ls)
  log ("ls", ls("\\..*"))
  
  "dir1".mkdir  ;  assert (FP("dir1").exists)
  "dir1".rmdir	;  assert (! FP("dir1").exists)
  "dir2/dir21/dir211".mkdirs	;  assert (FP("dir2/dir21/dir211").exists)
  "dir2".rm	;      assert (FP("dir2").exists)
  //TODO "dir2".rm (r)	;  assert (! FP("dir2").exists)

  mkdir on "dir1"  ; assert (exists ("dir1"))
  rmdir ("dir1")  ;  assert (!exists ("dir1"))
 //TODO rmdir on "dir1"  ;  assert (!exists ("dir1"))
  
  mkdir ("dir1")  ; assert (exists ("dir1"))
  rmdir ("dir1")  ;  assert (!exists ("dir1"))
  
  "gigi".echo 
  > echo "gigi"
  "gigi".echo > "gigi.out"
  assert (FP("gigi.out").exists)
  FP("gigi.out").rm
  assert (! FP("gigi.out").exists)
  
  echo ("gigi") > "gigi.out"
  cp ("gigi.out", "gigi.out2")
  assert (MFQ("gigi.out,gigi.out2").exists)
  "gigi.out2".rm
  assert (!MFQ("gigi.out,gigi.out2").exists)
  "gigi.out" copyTo "gigi.out2"
  assert (MFQ("gigi.out,gigi.out2").exists)
  "gigi.out2".rm
  assert (!MFQ("gigi.out,gigi.out2").exists)
  "gigi.out" -> "gigi.out2"
  assert (MFQ("gigi.out,gigi.out2").exists)
 
  "dir3/gigi.out".rm -f
  "dir3".rm -f
  assert (! FP("dir3").exists)
  "dir3".mkdir
  assert (! FP("dir3/gigi.out").exists)
  "gigi.out" -> "dir3"
  assert (FP("dir3/gigi.out").exists)
  "dir3".rm (r)
  
  MFQ("gigi.out,gigi.out2").rm
}

object Sf2CoolApp extends App {
  import DefaultShell._
  import Flags._

  val > = DefaultShell
  
  > echo "gigi"
  >.echo ("gigi") > "gigi.out"
  assert (FP("gigi.out").exists)
  FP("gigi.out").rm
  assert (! FP("gigi.out").exists)
}
