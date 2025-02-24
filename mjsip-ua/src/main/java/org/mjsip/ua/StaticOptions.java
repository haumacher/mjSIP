/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.sip.address.NameAddress;

/**
 * The non-user-specific aspects of {@link ClientOptions}.
 */
public interface StaticOptions {

	/** Media address (use it if you want to use a media address different from the via address) */
	String getMediaAddr();

	/** No offer in the invite */
	boolean getNoOffer();

	/**
	 * Response time in seconds; it is the maximum time the user can wait before responding to an
	 * incoming call; after such time the call is automatically declined (refused).
	 * 
	 * <p>
	 * A negative value means "no automatic refuse".
	 * </p>
	 */
	int getRefuseTime();

	/** 
	 * The flow direction.
	 */
	Direction getDirection();
	
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
			public String getSipUser() {
				return user.getSipUser();
			}
			
			@Override
			public String getProxy() {
				return user.getProxy();
			}
			
			@Override
			public int getRefuseTime() {
				return StaticOptions.this.getRefuseTime();
			}
			
			@Override
			public boolean getNoOffer() {
				return StaticOptions.this.getNoOffer();
			}
			
			@Override
			public String getMediaAddr() {
				return StaticOptions.this.getMediaAddr();
			}
			
			@Override
			public Direction getDirection() {
				return StaticOptions.this.getDirection();
			}
			
			@Override
			public ClientOptions forUser(UserOptions user) {
				return StaticOptions.this.forUser(user);
			}
		};
	}
	
}
