/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener lists with copy on write semantics.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public abstract class CopyOnWriteListeners<L, M> {

	private boolean _iterating;

	private List<L> _list = new ArrayList<>();

	/**
	 * Adds the given element.
	 */
	public boolean add(L listener) {
		if (_list.contains(listener)) {
			return false;
		}
		return copyWhileIterating().add(listener);
	}

	private List<L> copyWhileIterating() {
		if (_iterating) {
			_list = new ArrayList<>(_list);
		}
		return _list;
	}

	/**
	 * Removes the given element.
	 */
	public boolean remove(L listener) {
		if (!_list.contains(listener)) {
			return false;
		}
		return copyWhileIterating().remove(listener);
	}

	/**
	 * Clears this list.
	 */
	public void clear() {
		copyWhileIterating().clear();
	}

	/**
	 * Delivers the message to all listeners.
	 */
	public void notify(M msg) {
		boolean before = _iterating;
		try {
			_iterating = true;
			for (L listener : _list) {
				handle(listener, msg);
			}
		} finally {
			if (_iterating) {
				_iterating = before;
			}
		}
	}

	/**
	 * Callback actually delivering the message to the given listener.
	 */
	protected abstract void handle(L listener, M msg);

}
