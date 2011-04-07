package razie.fs.proto2

object Sf2App extends Application {
  import DefaultShell._
  import Flags._
    
  log ("pwd", pwd)
  
  log ("ls", ls)
  log ("ls", ls("\\..*"))
  
  "dir1".mkdir  ;  assert (P("dir1").exists)
  "dir1".rmdir	;  assert (! P("dir1").exists)
  "dir2/dir21/dir211".mkdirs	;  assert (P("dir2/dir21/dir211").exists)
  "dir2".rm	;      assert (P("dir2").exists)
  "dir2".rm (r)	;  assert (! P("dir2").exists)

  mkdir on "dir1"  ; assert (exists ("dir1"))
  rmdir ("dir1")  ;  assert (exists ("dir1"))
}
