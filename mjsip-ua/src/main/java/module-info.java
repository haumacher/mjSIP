/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */

/**
 * The myjSip utilities.
 */
module org.mjsip.ua {

	requires org.mjsip.sound;
	requires org.mjsip.util;
	requires transitive org.mjsip.sip;
	requires org.mjsip.net;
	requires args4j;
	requires org.slf4j;
	requires java.desktop;
	
	opens org.mjsip.ua to args4j;

	exports org.mjsip.ua;
	exports org.mjsip.ua.clip;
	exports org.mjsip.ua.registration;
	exports org.mjsip.ua.sound;
	exports org.mjsip.ua.streamer;
}