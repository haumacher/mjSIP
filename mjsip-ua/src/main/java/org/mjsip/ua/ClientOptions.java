/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.sip.address.NameAddress;

/**
 * Options for controlling a {@link UserAgent}.
 * 
 * @see UAOptions
 */
public interface ClientOptions extends StaticOptions, UserOptions {

	/**
	 * This {@link ClientOptions} with for the given user.
	 */
	default ClientOptions forUser(UserOptions user) {
		return new ClientOptions() {
			
			@Override
			public NameAddress getUserURI() {
				return user.getUserURI();
			}
			
			@Override
			public String getAuthUser() {
				return user.getAuthUser();
			}
			
			@Override
			public String getAuthRealm() {
				return user.getAuthRealm();
			}
			
			@Override
			public String getAuthPasswd() {
				return user.getAuthPasswd();
			}
			
			@Override
			public String getUser() {
				return user.getUser();
			}
			
			@Override
			public String getProxy() {
				return user.getProxy();
			}
			
			@Override
			public int getRefuseTime() {
				return ClientOptions.this.getRefuseTime();
			}
			
			@Override
			public boolean getNoOffer() {
				return ClientOptions.this.getNoOffer();
			}
			
			@Override
			public String getMediaAddr() {
				return ClientOptions.this.getMediaAddr();
			}
			
			@Override
			public Direction getDirection() {
				return ClientOptions.this.getDirection();
			}
			
			@Override
			public ClientOptions forUser(UserOptions user) {
				return ClientOptions.this.forUser(user);
			}
		};
	}
	
}
