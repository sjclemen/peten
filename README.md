
PETEN, AN UPLOADER FOR MAYAN EDMS
=================================

This is a hastily thrown together uploader for Mayan EDMS. It will watch a
directory for new files, and prompt a user to upload them. Users are offered
the ability to add tags or an indexable field to each item. It can upload .bmp
files to a Mayan EDMS installation, compressing them to PNG (single page) or
merging multiple pages into a lossless PDF. It can also directly upload PDFs.

By moving compression + page merging from the scanning software into another
piece of software, it is possible to scan and upload large numbers of
documents faster.

Peten is written in Java, using SWT+JFace for the GUI.

GETTING STARTED
===============

Grab RCP for Eclipse Luna, extract the jars, and put the following in a
directory named jface-jars:
* org.eclipse.core.commands_3.6.100.v20140528-1422.jar
* org.eclipse.core.databinding_1.4.2.v20140729-1044.jar
* org.eclipse.core.databinding.beans_1.2.200.v20140214-0004.jar
* org.eclipse.core.databinding.observable_1.4.1.v20140210-1835.jar
* org.eclipse.core.databinding.property_1.4.200.v20140214-0004.jar
* org.eclipse.equinox.common_3.6.200.v20130402-1505.jar
* org.eclipse.jface_3.10.2.v20141021-1035.jar
* org.eclipse.jface.databinding_1.6.200.v20140528-1422.jar
* org.eclipse.swt_3.103.2.v20150203-1313.jar
* org.eclipse.swt.cocoa.macosx.x86_64_3.103.2.v20150203-1351.jar
* org.eclipse.swt.gtk.linux.x86_3.103.2.v20150203-1351.jar
* org.eclipse.swt.gtk.linux.x86_64_3.103.2.v20150203-1351.jar
* org.eclipse.swt.win32.win32.x86_3.103.2.v20150203-1351.jar
* org.eclipse.swt.win32.win32.x86_64_3.103.2.v20150203-1351.jar

Then run build-repository.sh.

The project should now build with Maven.

Edit pom.xml to indicate the SWT platform (i.e. windows 64-bit) you want
to build it with.

Edit Globals.java so that API_BASE points to your API.

Run the following to build it:
``mvn clean compile assembly:single``

In order to use this with Mayan EDMS, you'll need to patch a bug in your
Mayan EDMS installation by using the included mayan.diff.

COPYRIGHT
=========

All files (C) 2014-2016 Stephen Clement, except
* GreyscaleLosslessFactory.java
* BackendListenableFutureTask.java

which list their copyright information in their respective files.

Licensed under the GNU LGPL version 3 or higher. See LICENSE.
