/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */

/**
 * The myjSip utilities.
 */
module org.mjsip.sip {

	requires transitive org.mjsip.sound;
	requires transitive org.mjsip.util;
	requires transitive org.mjsip.net;
	requires args4j;
	requires org.slf4j;
	requires java.desktop;

	opens org.mjsip.sip.provider to args4j;
	opens org.mjsip.pool to args4j;

	exports org.mjsip.media;
	exports org.mjsip.media.rx;
	exports org.mjsip.media.tx;
	exports org.mjsip.pool;
	exports org.mjsip.rtp;
	exports org.mjsip.sdp;
	exports org.mjsip.sdp.field;
	exports org.mjsip.sip.address;
	exports org.mjsip.sip.authentication;
	exports org.mjsip.sip.call;
	exports org.mjsip.sip.config;
	exports org.mjsip.sip.dialog;
	exports org.mjsip.sip.header;
	exports org.mjsip.sip.message;
	exports org.mjsip.sip.provider;
	exports org.mjsip.sip.transaction;
}