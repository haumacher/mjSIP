/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */

/**
 * The myjSip utilities.
 */
module org.mjsip.net {

	requires org.mjsip.util;

	requires args4j;
	requires org.slf4j;

	requires java.desktop;

	exports org.mjsip.net;
	exports org.zoolu.net;
}