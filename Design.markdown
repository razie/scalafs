
Commands
========

pwd
cd
ls
rm
mkdir
rmdir

cat
cp
sed

diff
grep
find
echo
set
$

Features
========

- plugin FS providers (local, remote, ftp, DOM/XML, webdav, objects etc)
- pretty much any tree content structure, i.e. Hulu shows?
- support stuff across FS (i.e. copy)
- asynchronous commands - integrate with Razie's Gremlins
- pipes - can't reuse the OS pipes, since they can go across content providers
- security
- hookup with Razie's Scripster for online scripting
- use Java async NIO with FS notifications

Use cases
=========

Deployment scripts
------------------

deploy stuff regardless of OS

- copy sutff around, explode zips
- sed, change property files
- zip/jar/war/ear
- run OS-specific commands


Administration scripts
----------------------

administer stuff regardless of OS

- run OS-specific commands


File sync service
-----------------

sync folders on remote computers with different OS
- trap NIO notifications and diff
