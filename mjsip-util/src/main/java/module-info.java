/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */

/**
 * The myjSip utilities.
 */
module org.mjsip.util {

	exports org.mjsip.config;
	exports org.mjsip.time;
	exports org.zoolu.util;
	
	opens org.mjsip.config to args4j;
	opens org.mjsip.time to args4j;
	
	requires args4j;
	requires org.slf4j;
	requires java.desktop;
	
}