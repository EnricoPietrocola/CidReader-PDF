# CidReader-PDF - Collaborative Realtime PDF Reader

Join our Discord!
[![Discord](https://theme.zdassets.com/theme_assets/678183/cc59daa07820943e943c2fc283b9079d7003ff76.svg)](https://discord.gg/AAe6rx6kbK )


Cid is an open source Android PDF reader (I am making a web version too) able to share realtime interaction online.
It is created to help Networked Music students and performers to interact on music sheet on a distance or locally.
Works with UDP sockets and on any modern android tablet.

[Open Beta Access](https://play.google.com/apps/testing/com.pietrocola.enrico.CidReader)

[Paper/Description page](http://enricopietrocola.com/cidreader/)

[Community Page](https://www.reddit.com/r/CidReaderPDF/)

[Facebook Page](https://www.facebook.com/CidInteraction)

How to use:

- Choose a document you want to open by the app main page (currently showing only the Downloads Folder) or with the "Open With" options in your Android File Manager
- Drag your finger on a page to draw, if the other device has the app open, annotation data and current page will be synchronized

How to connect
- When viewing a document, open the menu by clicking the top right menu icon in the action menu
- Type the address you would like to connect to and click "ADD TO CONNECTIONS".
- Close the menu, all your interactions are now synchronized

![ExampleImage](https://media.giphy.com/media/UvWuE9d1LzedVJLtGz/giphy.gif)

[![IntroductionToCidReader](https://raw.githubusercontent.com/EnricoPietrocola/Cid/master/Introduction%20to%20CidReader1.JPG)](https://www.youtube.com/watch?v=ofszkfZpO4k)

[![EasilyConnectWithZeroTier](https://raw.githubusercontent.com/EnricoPietrocola/Cid/master/easily%20connect%20thumnail1.JPG)](https://www.youtube.com/watch?v=2SYC5RHXCCY)

[Video example](https://www.youtube.com/watch?v=5DPnnhvZc1Q/)

[Video example for a CidReader-PDF session](https://www.youtube.com/watch?v=5DPnnhvZc1Q/)

[Trello board with tasks and useful info](https://trello.com/b/FIkBy6M9/ciddev/)

* CidReaderPDF is based on the [MuPDF](https://mupdf.com/) renderer by Artifex:
In particular the version development started from, is the [Mupdf Android Mini Viewer](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini)

* In particular the Android SDK that can be found ![here](https://mupdf.com/downloads/)

* Currently used [colorpicker](https://github.com/skydoves/ColorPickerView) for annotations:

* Annotation layer is based on [Ssaurel's FingerPath object](https://gist.github.com/ssaurel/747c5c591f783450a30925543ba93c10) to store touch data and draw strokes

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


While the NAT traversal solution is discussed, a simple way to connect through NATs that has been tested it by using [ZeroTier](https://www.zerotier.com/) (open source Hamachi-like app with an android app port). It adds some latency but it's acceptable for common uses.

Contributing, main pieces of code:

[MainActivity.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/app/src/main/java/com/pietrocola/enrico/CidMainMenu/MainActivity.java) is the main menu, this is where you setup connection and scroll documents.

[DocumentActivity.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/DocumentActivity.java) is the main document part of code. Here you manage document, annotaion, touch, global variables, instances of paintviews with connections (![original mupdf-mini version](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini/blob/master/lib/src/main/java/com/artifex/mupdf/mini/DocumentActivity.java) .

[PageView.java](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/PageView.java) is where PDF pages are managed (slightly edited version of the original from [MuPDF mini](https://github.com/ArtifexSoftware/mupdf-android-viewer-mini/blob/master/lib/src/main/java/com/artifex/mupdf/mini/PageView.java)).

[PaintView](https://github.com/EnricoPietrocola/CidReader-PDF/blob/master/CidReaderPDF/mupdf-lib/src/main/java/com/artifex/mupdf/mini/PaintView.java) is the annotation class, it stores strokes for each page. Each user owns an instance of PaintView in  DocumentActivity (each user has a layer to draw on).

![Instruction1](https://github.com/EnricoPietrocola/Cid/blob/master/instructions1.png)
![Instruction2](https://github.com/EnricoPietrocola/Cid/blob/master/instructions2.png)
![Instruction3](https://github.com/EnricoPietrocola/Cid/blob/master/instructions3.png)


This project was possible thanks to the "Orio Carlini" Scholarships from [Consortium GARR](https://www.garr.it/en/) and hosted by [Conservatorio Giuseppe Verdi di Milano](http://www.consmilano.it/en/)

![GarrLogo](https://owncloud.com/wp-content/uploads/2014/04/Garr-400x175.png)

![ConsMILogo](https://raw.githubusercontent.com/EnricoPietrocola/Cid/master/consmilogo1.jpg)

License

Following MuPDF license, this project must be under the AGPL License. 

"If your software is open source, you may use MuPDF under the terms of the GNU Affero General Public License.
This means that all of the source code for your complete app must be released under a compatible open source license!
It also means that you may not use any proprietary closed source libraries or components in your app. This includes (but is not limited to) Google Play Services, Google Mobile Services, AdMob by Google, Crashlytics, Answers, etc.
Just because a library ships with Android or is made by Google does not make it AGPL compatible!
If you cannot or do not want to comply with these restrictions, you must acquire a commercial license instead."
