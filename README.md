# mjSIP - a complete Java-based SIP stack implementation 

SIP (Session Initiation Protocol) is the IETF (Internet Engineering Task Force) signaling standard for managing 
multimedia session initiation defined in RFC 3261 commonly used in VOIP communication. SIP can be used to initiate 
voice, video and multimedia sessions, for both interactive applications (e.g. an VOIP phone calls or a video 
conference applications) and non-interactive ones (e.g. video streaming). 

The mjSIP stack has been used in research activities by Dept. of Engineering and Architecture at University of Parma and 
by DIE - University of Roma "Tor Vergata" and several commercial products.

## mjSIP Features

mjSIP includes all classes and methods for creating SIP-based applications. It implements the complete layered stack 
architecture as defined in RFC 3261 (Transport, Transaction, and Dialog layers), and is fully compliant with RFC 3261 
and successive standard RFCs. Moreover it includes higher level interfaces for Call Control and User Agent 
implementations. mjSIP comes with a core package implementation that includes:

* all standard SIP layers and components,
* various SIP extensions (already defined within IETF),
* some useful call control APIs (e.g. Call-Control, UserAgent, etc.),
* a reference implementation of some SIP systems (Proxy Server, Session Border Controlleer, and User Agent).

## Changes since 1.8

* Added Maven build.
* Split source into core modules `sip`, `server`, `ua`, and supporting modules `net`, `sound`, and `util`. Extracted 
  examples into modules `examples` and `phone`. 
* Code cleanup: Added type parameter, added override annotations, reduced excessive logging, made fields private final
  where possible, removed mutable static fields, replaced lazy initialization with defined initialization order, reduced
  number of constructors, enhanced configuration file parsing with `args4j`, applied Java naming conventions, encapsulated
  fields. Access configuration from production code through read-only interfaces.
* Replaced self-made logging with `slf4j` over `tinylog`.
* Clarified transaction timeout handling with separate handler methods for each timeout. 
* Modernized scheduling using ScheduledThreadPoolExecutor.
* Implemented listening on `DTMF` info messages.
* Implemented port pool for `RTP` media streams.
* Fixed parsing of `SDP` messages with well-known formats that are not explained in `rtpmap` fields. 
* Separated creation of concrete media streams from user agent handling. Removed audio file configuration (for steaming 
  and recording) from core components.
* Pulled out system audio sounds played when calls are incoming, accepted, or terminated to client code.
* Added audio filter for silence stripping.
* Added IPv6 support.
* Added Java module support.

## License

mjSIP is available open source under the terms of the GNU GPL license (General Public Licence) 
as published by the Free Software Foundation.

## See also

The project's original home page is at: http://mjsip.org/
 
There are several independent forks of the project on Github:
 
* https://github.com/VovaSokol/mjsip (1.8) - Added support for Opus and H264 codec.
* https://github.com/mrichardsdb/mjsip (1.8) - Turned into a maven project.
* https://github.com/inckie/mjsip (1.8)
* https://github.com/opentelecoms-org/MjSIP-fork (1.6)
* https://github.com/moki80/OpenComm/tree/master/sipvoip
