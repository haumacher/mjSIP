/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */
package org.mjsip.sip.dialog;

/**
 * States of a {@link Dialog}
 */
public enum DialogStatus {

	/** Dialog state INIT */
	D_INIT(0),

	/** Dialog state WAITING */
	D_WAITING(1),

	/** Dialog state RE-WAITING */
	D_ReWAITING(11),

	/** Dialog state INVITING */
	D_INVITING(2),

	/** Dialog state RE-INVITING */
	D_ReINVITING(12),

	/** Dialog state INVITED */
	D_INVITED(3),

	/** Dialog state RE-INVITED */
	D_ReINVITED(13),

	/** Dialog state REFUSED */
	D_REFUSED(4),

	/** Dialog state RE-REFUSED */
	D_ReREFUSED(14),

	/** Dialog state ACCEPTED */
	D_ACCEPTED(5),

	/** Dialog state RE-ACCEPTED */
	D_ReACCEPTED(15),

	/** Dialog state CALL */
	D_CALL(6),

	/** Dialog state BYEING */
	D_BYEING(7),

	/** Dialog state BYED */
	D_BYED(8),

	/** Dialog state CLOSE */
	D_CLOSE(9),

	;

	private int _id;

	/**
	 * Creates a {@link DialogStatus}.
	 */
	DialogStatus(int id) {
		_id = id;
	}

	/**
	 * The legacy id of this status.
	 */
	int getId() {
		return _id;
	}

	/**
	 * Whether the call is not yet accepted.
	 */
	boolean isEarly() {
		return ordinal() < DialogStatus.D_ACCEPTED.ordinal();
	}

	/**
	 * Whether the call is alive (accepted and not yet closed).
	 */
	boolean isConfirmed() {
		int ordinal = ordinal();
		return ordinal >= DialogStatus.D_ACCEPTED.ordinal() && ordinal < DialogStatus.D_CLOSE.ordinal();
	}
}
