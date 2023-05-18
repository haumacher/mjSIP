# mjSIP - a complete java-based SIP stack implementation 

It provides in the same time the API and implementation bound together into the mjSIP packages. mjSIP is available open 
source under the terms of the GNU GPL license (General Public Licence) as published by the Free Software Foundation.

SIP (Session Initiation Protocol) is the IETF (Internet Engineering Task Force) signaling standard for managing 
multimedia session initiation; it is currently defined in RFC 3261. SIP can be used to initiate voice, video and 
multimedia sessions, for both interactive applications (e.g. an IP phone call or a videoconference) and not interactive 
ones (e.g. a Video Streaming), and it is the more promising candidate as call setup signaling for the present day and 
future IP based telephony services. SIP has been also proposed for session initiation related uses, such as for 
messaging, gaming, etc.

The mjSIP stack has been used in research activities by Dept. of Engineering and Architecture at University of Parma and 
by DIE - University of Roma "Tor Vergata" and several commercial products.

## mjSIP Features

mjSIP includes all classes and methods for creating SIP-based applications. It implements the complete layered stack 
architecture as defined in RFC 3261 (Transport, Transaction, and Dialog sublayers), and is fully compliant with RFC 3261 
and successive standard RFCs. Moreover it includes higher level interfaces for Call Control and User Agent 
implementations. mjSIP comes with a core package implementation that includes:

* all standard SIP layers and components,
* various SIP extensions (already defined within IETF),
* some useful Call Control APIs (e.g. Call-Control, UserAgent, etc.),
* a reference implementation of some SIP systems (Proxy Server, Session Border Controlleer, and UA).

## See also

The project's original home page is at: http://mjsip.org/
 
There are several independent forks of the project on Github:
 
* https://github.com/inckie/mjsip (1.8)
* https://github.com/opentelecoms-org/MjSIP-fork (1.6)
* https://github.com/moki80/OpenComm/tree/master/sipvoip
