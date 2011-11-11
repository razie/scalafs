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

