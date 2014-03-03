package edu.virginia.g3.fsview.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractFSViewInformationModel<InfoType> implements
		FSViewInformationModel<InfoType> {
	private String _modelName;

	private Collection<FSViewInformationListener<InfoType>> _listeners = new LinkedList<FSViewInformationListener<InfoType>>();

	final protected void fireContentsChanged() {
		Collection<FSViewInformationListener<InfoType>> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<FSViewInformationListener<InfoType>>(
					_listeners);
		}

		for (FSViewInformationListener<InfoType> listener : listeners)
			listener.contentsChanged(this);
	}

	protected AbstractFSViewInformationModel(String modelName) {
		_modelName = modelName;
	}

	@Override
	final public String modelName() {
		return _modelName;
	}

	@Override
	final public void addInformationListener(
			FSViewInformationListener<InfoType> listener) {
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	@Override
	final public void removeInformationListener(
			FSViewInformationListener<InfoType> listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}
}