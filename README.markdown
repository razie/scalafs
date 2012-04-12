    /**  ____    __    ____  ____  ____/___      ____  __  __  ____
     *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
     *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
     *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
     *                      
     *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
     */

What?
=====

Simple scala-based file system commands. 

Why?
----
Unified shell-like scripting for FS-commands.

How?
----

Good question!


Roadmap
-------

See Design.markdown

Examples
========

Syntax still in progress - here's some ideas that work so far:

    "dir2/dir21/dir211".mkdirs
    mkdir on "dir1" 
    rmdir ("dir1")    
    > echo "gigi"
    "gigi".echo > "gigi.out"
    "gigi.out" copyTo "gigi.out2"
    "dir3/gigi.out".rm -f
    echo ("gigi") > "gigi.out"
    cp ("gigi.out", "gigi.out2")


Use as SBT plugin
-----------------

This is a good use case, to create distributions from an SBT project.

To use it, do not use addSbtPlugin(...) , but rather this line to the <myproject>/project/plugins.sbt

    libraryDependencies += "com.razie" % "scalafs_2.9.1" % "0.3-SNAPSHOT"

Reason being, this is used just as a library, it doesn't do any sbt magic.

Then, in your project's settings add say a 'dist' command:

    commands += distCommand,

as in

    import sbt._
    import Keys._

    object HelloBuild extends Build {
    
      lazy val project = Project (
        "project",
        file ("."),
        settings = Defaults.defaultSettings ++ Seq(
          organization := "hello",
          name         := "world",
          version      := "1.0-SNAPSHOT",
          scalaVersion := "2.9.0-1",
     
          commands += distCommand,
     
        )
      )
    }

Now you can finally define the actual command implementation that moves files around:

    import razie.fs.proto2._
    import razie.fs.proto2.DefaultShell._
      
    def distCommand = Command.command ("dist") { state =>
      val baseDir = FP apply Project.extract(state).get(baseDirectory) // get project's directory
      val d       = baseDir / "mdist"
      
      out << "Creating distribution in %s \n".format(d.path)
      
      val ms = baseDir / "mutant/src/main/resources"
      val as = baseDir / "agent/src/main/resources"
      val es = baseDir / "media/src/main/resources"
      
      // prepare directory structure
     
      "lib plugins cfg user" split " " map (d / _) foreach (_.mkdirs)
      "lib plugins cfg" split " " map (d / "upgrade" / _) foreach (_.mkdirs)
      
      // copy files
      
      d << as / "cfg" / "agent.xml"
      d << as / "cfg" / "assets.xml"
      d << as / "cfg" / "template_agent.xml"
      d << es / "cfg" / "media.xml"
      d << ms / "cfg" / "user.xml"
      
      state // how to indicate success/failure?
      }

This works with sbt 0.10+


