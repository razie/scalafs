package razie.fs.proto1

object Sf1App extends Application {
  import DefaultShell._
  import Flags._
    
  log ("pwd", pwd)
  
  log ("ls", ls)
  log ("ls", ls("\\..*"))
  
  "dir1".mkdir  ;  log (P("dir1").exists)
  "dir1".rmdir	;  log (P("dir1").exists)
  "dir2/dir21/dir211".mkdirs	;  log (P("dir2/dir21/dir211").exists)
  "dir2".rm	;      log (P("dir2").exists)
  "dir2".rm (r)	;  log (P("dir2").exists)

  // just having fun now
  val > = DefaultShell
  >mkdir "dir1"  ;  log (>exists "dir1")
  rmdir ("dir1")  ;  log (exists ("dir1"))
}
