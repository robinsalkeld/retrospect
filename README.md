Retrospect
===========

The Retrospect project is the implementation for my doctoral thesis, tentatively titled "Time-travel Programming." It enables executing code in the context of a previous execution of a program using a library that implements a "holographic" virtual machine. This makes the running state of a program language pluggable, so that it can be provided by an execution recording instead of native state in the virtual machine. 

To date, the primary application has been debugging and analyzing Java heap dumps as if they were live processes, making it possible to evaluate arbitrary source expressions, or load and run additional code in order to analyze state. However, holographic VMs also have broader applications, which will be explored in future research. Holographic VMs and applying them to Java heap dumps are covered in detail in my OOPSLA 2013 paper (to appear): [Interacting with Dead Objects](https://www.cs.ubc.ca/~rsalkeld/publications/oopsla2013.pdf).

Requirements
------------

The Retrospect code base is packaged as a collection of Equinox plugins, and the easiest way to use them is to install them into Eclipse. The code has been tested with the newest version of Eclipse (4.2.2), but was developed using quite an old version (Indigo) and will likely work with even older versions.

Although the overall architecture of holographic JVMs is portable and generic, there are small pieces that do depend on the operating system the heap dump was taken on. Both Windows and UNIX-like platforms (included Mac OS) should be supported. If you find your environment doesn't work for you please contact me, as these problems are usually very easy to fix.

Installation
------------

The Retrospect plugin binaries can be installed from the update site included in this repository. I'd recommend using a fresh Eclipse installation just to be safe.

1. Before launching Eclipse, add "-XX:-UseSplitVerifier" to the bottom of Eclipse.app/Contents/MacOS/eclipse.ini. 
    * The newer Java 7 bytecode verifier doesn't like something in my generated bytecode. This allows the JVM to fall back to the older mechanism.
2. In Eclipse, select Help | Install New Software...
3. Click the "Add..." button to add a new software site/repository
4. Paste the update site URL (https://github.com/robinsalkeld/retrospect/raw/master/RetrospectUpdateSite) in the "Location" field. Note the "raw" path element is necessary. Name the update site as desired.
5. Click "OK"
6. Uncheck "Group items by category" (otherwise nothing shows up)
7. Select the "Retrospect" feature and complete the installation wizard.
8. Restart Eclipse when prompted to.

Using Holographic JVMs
----------------------

The easiest way to try out the library is via the integration with the [Memory Analyzer](http://www.eclipse.org/mat/) tool, as the Retrospect plugins include custom queries that can be executed from the tool.

1. Install the Memory Analyzer tool from the core Eclipse update site (e.g. [http://download.eclipse.org/releases/kepler]()) or as instructed on the Memory Analyzer [downloads](http://eclipse.org/mat/downloads.php) page.
1. Click Window | Open Perspective | Other..., and select the Memory Analysis perspective.
2. Use File | Open Heap Dump... or Acquire Heap Dump... to start browsing a .hprof heap dump file.
3. Create or import at least one Java project. 
    * This is a limitation with the current integration with the Java Developer Tools that will hopefully be addressed in the future.
4. With an object of interest selected, right-click the object and select "Evaluate Expression" to evaluate an arbitrary expression (e.g. "toString()").
5. Alternatively, select the "Load and Run Code" query from the dropdown, and provide the path to a class file that defines a static method to run on a collection of objects.

The details for both queries are documented in their respective dialog windows.

Configuring the Holographic File System
---------------------------------------

Because Java heap dumps don't include any bytecode, the location of the bytecode for a holographic JVM has to be provided in a configuration file. Namely, if the heap dump file is located at /path/to/dump.hprof, a holographic JVM will look for the file /path/to/dump_hfs.ini. The format of this file is a series of pairs separated by "=", one per line, mapping directories from the system the heap dump came from to directories on your own machine. For example:

C:/Program Files/Java/jre6=/path/to/java  
C:/code/my\_project/bin=/users/john/code/my\_project/bin

Usually the easiest workflow is to attempt to run code on the holographic JVM and just see what paths it complains about not being able to find, locating the needed code and filling in the ini file incrementally.

Note that holographic JVMs are implemented by translating the original bytecode, and the first attempt to run a holographic VM for a particular heap dump will likely be quite slow. The modified bytecode is cached on disk, however (at /path/to/dump_hologram_classes/ for a heap dump file at /path/to/dump.hprof).

Building on the Retrospect library
----------------------------------

Integrating holographic JVMs into other tools, implementing other sources of program state, etc. should be relatively straight forward. The source code for the Memory Analyzer queries (e.g. ExpressionQuery.java in the edu.ubc.mirrors.eclipse.mat plugin) should serve as a good example of how to use the library. 

Enjoy!
------

If you have any difficulties please don't hesitate to contact me! \(rsalkeld "at" cs.ubc.ca\)