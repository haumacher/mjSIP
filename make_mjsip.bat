javac -d classes src/org/zoolu/net/*.java src/org/zoolu/sound/*.java src/org/zoolu/sound/codec/*.java src/org/zoolu/sound/codec/amr/*.java src/org/zoolu/sound/codec/g711/*.java src/org/zoolu/sound/codec/g726/*.java src/org/zoolu/sound/codec/gsm/*.java src/org/zoolu/util/*.java src/org/mjsip/rtp/*.java src/org/mjsip/sdp/*.java src/org/mjsip/sdp/field/*.java src/org/mjsip/media/*.java src/org/mjsip/net/*.java 
javac -classpath classes -d classes src/org/mjsip/sip/address/*.java src/org/mjsip/sip/authentication/*.java src/org/mjsip/sip/call/*.java src/org/mjsip/sip/dialog/*.java src/org/mjsip/sip/header/*.java src/org/mjsip/sip/message/*.java src/org/mjsip/sip/provider/*.java  src/org/mjsip/sip/transaction/*.java
javac -classpath classes -d classes src/org/mjsip/ua/*.java src/org/mjsip/ua/cli/*.java src/org/mjsip/ua/gui/*.java
javac -classpath classes -d classes src/org/mjsip/server/*.java src/org/mjsip/server/sbc/*.java
cd classes
jar -cf ../lib/sip.jar org/zoolu org/mjsip/rtp org/mjsip/sdp org/mjsip/sip -C ../lib COPYRIGHT.txt -C ../lib license.txt
jar -cf ../lib/ua.jar org/mjsip/media org/mjsip/net org/mjsip/ua -C ../resources media/org/mjsip/ua -C ../lib COPYRIGHT.txt -C ../lib license.txt
jar -cf ../lib/server.jar org/mjsip/net org/mjsip/server -C ../lib COPYRIGHT.txt -C ../lib license.txt
cd ..
