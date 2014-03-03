package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;

public class LevelIcon
{
	static final private int SIZE = 16;

	static public Map<HistoryEventLevel, Icon> ICON_MAP;

	static {
		ICON_MAP = new EnumMap<HistoryEventLevel, Icon>(HistoryEventLevel.class);

		for (HistoryEventLevel level : HistoryEventLevel.values())
			ICON_MAP.put(level, iconForLevel(level));
	}

	static public Icon iconForLevel(HistoryEventLevel level)
	{
		switch (level) {
			case Error:
				return IconFactory.createErrorIcon(SIZE);

			case Warning:
				return IconFactory.createWarningIcon(SIZE);

			default:
				return IconFactory.createEmptyIcon(SIZE);
		}
	}
}