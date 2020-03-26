# CidReader-PDF - Collaborative Realtime PDF Reader

Cid is an open source Android PDF reader able to share realtime interaction online.
It is created to help Networked Music students and performers to interact on music sheet on a distance or locally.
Works with UDP sockets and on any modern android tablet.

Description page http://enricopietrocola.com/cidreader/
Community Page https://www.reddit.com/r/CidReaderPDF/

How to use:

- Set the IP address of the device you would like to connect with
- Clicking on a document you would like to open will start communication and open the first page (at the moment the app is only  listing documents in the downloads folder) 
- Drag your finger on a page to draw, if the other device has the app open, annotation data and current page will be synchronized

![ExampleImage](https://media.giphy.com/media/UvWuE9d1LzedVJLtGz/giphy.gif)

![Video example](https://www.youtube.com/watch?v=5DPnnhvZc1Q)

Video example for a CidReader-PDF session
https://www.youtube.com/watch?v=5DPnnhvZc1Q

Trello board with tasks and useful info
https://trello.com/b/FIkBy6M9/ciddev

* CidReaderPDF is based on the MuPDF renderer by Artifex:
https://mupdf.com/

* In particular the Android SDK that can be found here:
https://mupdf.com/downloads/

* Currently used colorpicker for annotations:
https://github.com/skydoves/ColorPickerView

* Annotation layer is based on Ssaurel's FingerPath object to store touch data and draw strokes
https://gist.github.com/ssaurel/747c5c591f783450a30925543ba93c10
https://medium.com/@ssaurel/learn-to-create-a-paint-application-for-android-5b16968063f8

Right now Cid features:
* Connectivity with other users through UDP sockets
* PDF, XPS, OpenXPS, CBZ, EPUB, and FictionBook 2 rendering
* Syncronized annotations to highlight or draw with a finger or a smart pen
* Syncronized page turning
* Color picker
* Hide/show annotations easily
* A menu of connected users
* Fast rendering, works on old devices
* Platform: Android

Being planned:
* A Web Version using WebRTC + Pdf.js
* Save/Load annotation files
* Easy to use multi-node connections
* Customizable permissions on users

What is missing but would be great:
* A simple way for NAT traversal
* File transfer (at the moment you HAVE to have the PDF file in your download folder to be able to see it) (it's easy to implement but laws are pretty strict on this and before implementing this I would like to be sure we are not breaking any copyright by simply allowing FTP)


While the NAT traversal solution is discussed, a simple way to connect through NATs that has been tested it by using ZeroTier https://www.zerotier.com/ (open source Hamachi-like app with an android app). It add some latency but it's acceptable for common uses.

Contributing
![MainActivity.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/app/src/main/java/com/pietrocola/enrico/CidMainMenu/MainActivity.java) is the main menu, this is where you setup connection and scroll documents
DocumentActivity.java is 

License
AGPL License
