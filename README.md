Restrospect
===========

This document describes how to install the binary "distribution" of the Retrospect project, which includes the implementation of Holographic JVMs and a version of the Eclipse Memory Analysis Tool (MAT) that has been modified to take advantage of it.

Requirements
------------

- Mac OS X
-- A couple of mirror-based native methods are UNIX file system specific. Implementing the same for other operating systems should not be difficult but I have not yet done so.
-- The experimental setup uses a convenient trick for extracting the PID for a Java process that may not be valid for non-UNIX-like operating systems.
- Java 7
- Eclipse
-- I'd recommend a fresh installation for this purpose
-- I've tested successfully on Eclipse Classic 4.2.2, although I developed the code on Eclipse build 20110615-0604 so I expect older builds would be fine too.

Installation
------------

1. Before launching Eclipse, add "-XX:-UseSplitVerifier" to the bottom of Eclipse.app/Contents/MacOS/eclipse.ini. The newer Java 7 bytecode verifier doesn't like something in my generated bytecode. This allows the JVM to fall back to the older mechanism.
2. In Eclipse, select Help | Install New Software...
3. Click the "Add..." button to add a new software site/repository
4. Click the "Local..." button and select the RetrospectUpdateSite folder
5. Click "OK"
6. Uncheck "Group items by category" (otherwise nothing shows up)
7. Select the "Retrospect" feature" and complete the installation wizard
8. Restart Eclipse when prompted to

Using Holographic JVMs
----------------------

To use the modified version of the Eclipse MAT (see documentation here):
1. Click Window | Open Perspective | Other..., and select the Memory Analysis perspective
2. Use File | Open Heap Dump... or Acquire Heap Dump... to start browsing a .hprof heap dump file
3. With an object of interest selected:
    - Window | Show View | Inspector - the "Value" tab displays the result of executing toString() on the object
    - Right-click the object and select ExpressionQuery to evaluate an arbitrary expression

It should also be relatively easy to write client code to use the API exposed by a holographic VM. The code in the experimental setup application below should be a helpful guide. Note that the bytecode generated for classes while using a holographic JVM on a heap dump file at /path/to/dump.hprof will be cached on disk at /path/to/dump_hologram_classes/.

Building the plugins

If you wish, you can modify and rebuild the code yourself using Eclipse. All of the top-level folders can be imported as existing Eclipse projects. The update site can be rebuilt by right-clicking the site.xml file and selecting PDE Tools | Build Site.
Beware that getting Eclipse installations to recognize the newer versions of the feature seems to be tricky, however - I frequently resorted to starting over with a fresh installation.
