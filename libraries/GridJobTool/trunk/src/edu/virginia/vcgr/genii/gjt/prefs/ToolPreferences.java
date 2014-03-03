package edu.virginia.vcgr.genii.gjt.prefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class ToolPreferences
{
	static private Logger _logger = Logger.getLogger(ToolPreferences.class);

	static private Object fromBytes(byte[] bytes, Object defaultValue) throws IOException, ClassNotFoundException
	{
		if (bytes == null)
			return defaultValue;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	static private byte[] toByteArray(Object value) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(value);
		oos.close();
		return baos.toByteArray();
	}

	private Map<ToolPreference, Collection<ToolPreferenceListener>> _listeners =
		new EnumMap<ToolPreference, Collection<ToolPreferenceListener>>(ToolPreference.class);

	private Preferences _preferenceNode;

	private EnumMap<ToolPreference, Object> _preferences;

	protected void firePreferenceChanged(ToolPreference preference)
	{
		Collection<ToolPreferenceListener> listeners;
		Collection<ToolPreferenceListener> tmpListeners;

		synchronized (_listeners) {
			tmpListeners = _listeners.get(preference);
		}

		synchronized (tmpListeners) {
			listeners = new Vector<ToolPreferenceListener>(tmpListeners);
		}

		for (ToolPreferenceListener listener : listeners)
			listener.preferenceChanged(this, preference, _preferences.get(preference));
	}

	public ToolPreferences()
	{
		for (ToolPreference preference : ToolPreference.values())
			_listeners.put(preference, new LinkedList<ToolPreferenceListener>());

		_preferenceNode = Preferences.userNodeForPackage(ToolPreferences.class);

		_preferences = new EnumMap<ToolPreference, Object>(ToolPreference.class);

		for (ToolPreference preference : ToolPreference.values()) {
			Class<?> defaultType = preference.defaultValue().getClass();

			if (defaultType.equals(Boolean.class)) {
				_preferences.put(preference,
					_preferenceNode.getBoolean(preference.name(), ((Boolean) preference.defaultValue()).booleanValue()));
			} else if (defaultType.equals(Integer.class)) {
				_preferences.put(preference,
					_preferenceNode.getInt(preference.name(), ((Integer) preference.defaultValue()).intValue()));
			} else {
				try {
					_preferences.put(preference,
						fromBytes(_preferenceNode.getByteArray(preference.name(), null), preference.defaultValue()));
				} catch (Throwable cause) {
					_logger.error(String.format("Unable to read preference \"%s\".", preference), cause);
				}
			}
		}
	}

	public void addPreferenceListener(ToolPreferenceListener listener, ToolPreference... interestSet)
	{
		if (interestSet == null || interestSet.length == 0)
			interestSet = ToolPreference.values();

		for (ToolPreference interest : interestSet) {
			Collection<ToolPreferenceListener> listeners;

			synchronized (_listeners) {
				listeners = _listeners.get(interest);
			}

			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	public void removePreferenceListener(ToolPreferenceListener listener, ToolPreference... interestSet)
	{
		if (interestSet == null || interestSet.length == 0)
			interestSet = ToolPreference.values();

		for (ToolPreference interest : interestSet) {
			Collection<ToolPreferenceListener> listeners;

			synchronized (_listeners) {
				listeners = _listeners.get(interest);
			}

			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	public Object preference(ToolPreference preferenceType)
	{
		synchronized (_preferences) {
			return _preferences.get(preferenceType);
		}
	}

	public Map<ToolPreference, Object> preferences()
	{
		synchronized (_preferences) {
			return _preferences.clone();
		}
	}

	public void commit(Map<ToolPreference, Object> preferences)
	{
		Set<ToolPreference> changeSet = EnumSet.noneOf(ToolPreference.class);

		synchronized (_preferences) {
			for (ToolPreference preference : preferences.keySet()) {
				Object value = preferences.get(preference);
				if (value != null) {
					if (!value.equals(_preferences.get(preference))) {
						Class<?> preferenceType = value.getClass();

						if (preferenceType.equals(Boolean.class)) {
							_preferenceNode.putBoolean(preference.name(), ((Boolean) value).booleanValue());
						} else if (preferenceType.equals(Integer.class)) {
							_preferenceNode.putInt(preference.name(), ((Integer) value).intValue());
						} else {
							try {
								_preferenceNode.putByteArray(preference.name(), toByteArray(value));
							} catch (Throwable cause) {
								_logger.error(String.format("Unable to write preference \"%s\".", preference), cause);
							}
						}

						_preferences.put(preference, value);
						changeSet.add(preference);
					}
				}
			}

			try {
				_preferenceNode.flush();

				for (ToolPreference preference : changeSet) {
					firePreferenceChanged(preference);
				}
			} catch (Throwable cause) {
				_logger.warn("Unable to flush preferences to backing store.", cause);
			}
		}
	}
}