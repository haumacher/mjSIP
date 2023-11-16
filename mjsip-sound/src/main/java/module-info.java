/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */

/**
 * The myjSip utilities.
 */
module org.mjsip.sound {

	requires org.mjsip.util;

	requires args4j;
	requires org.slf4j;
	requires transitive java.desktop; 

	exports org.mjsip.sound;
	exports org.zoolu.sound;
	exports org.zoolu.sound.codec;
	exports org.zoolu.sound.codec.amr;
	exports org.zoolu.sound.codec.g711;
	exports org.zoolu.sound.codec.g726;
	exports org.zoolu.sound.codec.gsm;
}