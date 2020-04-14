# CidReader-PDF - Collaborative Realtime PDF Reader

Cid is an open source Android PDF reader (I am making a web version too) able to share realtime interaction online.
It is created to help Networked Music students and performers to interact on music sheet on a distance or locally.
Works with UDP sockets and on any modern android tablet.

![Paper/Description page](http://enricopietrocola.com/cidreader/)

![Community Page](https://www.reddit.com/r/CidReaderPDF/)

How to use:

- Choose a document you want to open by the app main page (currently showing only the Downloads Folder) or with the "Open With" options in your Android File Manager
- Drag your finger on a page to draw, if the other device has the app open, annotation data and current page will be synchronized

How to connect
- When viewing a document, open the menu by clicking the top right menu icon in the action menu
- Type the address you would like to connect to and click "ADD TO CONNECTIONS".
- Close the menu, all your interactions are now synchronized

![ExampleImage](https://media.giphy.com/media/UvWuE9d1LzedVJLtGz/giphy.gif)

![Video example](https://www.youtube.com/watch?v=5DPnnhvZc1Q)

![Video example for a CidReader-PDF session](https://www.youtube.com/watch?v=5DPnnhvZc1Q)

![Trello board with tasks and useful info](https://trello.com/b/FIkBy6M9/ciddev)

* CidReaderPDF is based on the ![MuPDF](https://mupdf.com/) renderer by Artifex:
In particular the version development started from, is the ![Mupdf Android Mini Viewer](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini)

* In particular the Android SDK that can be found ![here](https://mupdf.com/downloads/)

* Currently used ![colorpicker](https://github.com/skydoves/ColorPickerView) for annotations:

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
* Save/Load annotation files
* Easy to use multi-node connections
* Update new users by sending them all annotations at realtime
* Platform: Android

Being planned:
* A Web Version using WebRTC + Pdf.js
* Customizable permissions systems for mutliple groups of users

What is missing but would be great:
* A simple way for NAT traversal
* File transfer (at the moment you HAVE to have the PDF file in your download folder to be able to see it) (it's easy to implement but laws are pretty strict on this and before implementing this I would like to be sure we are not breaking any copyright by simply allowing FTP)


While the NAT traversal solution is discussed, a simple way to connect through NATs that has been tested it by using ZeroTier https://www.zerotier.com/ (open source Hamachi-like app with an android app). It adds some latency but it's acceptable for common uses.

Contributing, main pieces of code:

![MainActivity.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/app/src/main/java/com/pietrocola/enrico/CidMainMenu/MainActivity.java) is the main menu, this is where you setup connection and scroll documents.

![DocumentActivity.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/DocumentActivity.java) is the main document part of code. Here you manage document, annotaion, touch, global variables, instances of paintviews with connections (![original mupdf-mini version](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini/blob/master/lib/src/main/java/com/artifex/mupdf/mini/DocumentActivity.java) .

![PageView.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/PageView.java) is where PDF pages are managed (slightly edited version of the original from ![MuPDF mini](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini/blob/master/lib/src/main/java/com/artifex/mupdf/mini/PageView.java)).

![PaintView](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/PaintView.java) is the annotation class, it stores strokes for each page. Each user owns an instance of PaintView in  DocumentActivity (each user has a layer to draw on).

This project was possible thanks to the "Orio Carlini" Scholarships from Consortium GARR and hosted by Conservatorio Giuseppe Verdi di Milano 

![GarrLogo](https://owncloud.com/wp-content/uploads/2014/04/Garr.png)
![ConsMILogo](https://lh3.googleusercontent.com/proxy/Rr_g9Wg2zgmGp2Ea6xKjedfYq2tWWaR59GEzW7ub7v2RSNSjKLBkyu-lgGsTZeA8I5BVSRx2gSooHgmHdFQCO5ykuCszNz-7ho035pmJVQBv89x4CZlaf-isE4keBMibSGGw954lhzRj6MCccYHOfbmrztBnGyRQzJBGEUejwvzkcToO_d_e)

License
AGPL License


