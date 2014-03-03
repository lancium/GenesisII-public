package edu.virginia.vcgr.genii.ui.plugins.logs.panels.entry;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.common.LogEntryType;

public class LevelColumn extends
		AbstractRowTableColumnDefinition<LogEntryType, String> {
	static final private String COLUMN_NAME = "Severity Level";
	static final private Class<String> COLUMN_CLASS = String.class;
	static final private int PREFERRED_WIDTH = 128;

	public LevelColumn() {
		super(COLUMN_NAME, COLUMN_CLASS, PREFERRED_WIDTH);
	}

	@Override
	public String extract(LogEntryType row) {
		String entryLevel = row.getLevel();
		return (entryLevel == null) ? "" : entryLevel;
	}
}
