package edu.virginia.vcgr.genii.ui.utils;

import javax.swing.TransferHandler;

public class DnDUtilities
{
	static private void addActionString(StringBuilder builder, String str)
	{
		if (builder.length() > 0)
			builder.append("|");
		builder.append(str);
	}

	static public String printActions(int actions)
	{
		StringBuilder builder = new StringBuilder();

		if ((actions & TransferHandler.COPY) > 0x0)
			addActionString(builder, "COPY");
		if ((actions & TransferHandler.MOVE) > 0x0)
			addActionString(builder, "MOVE");
		if ((actions & TransferHandler.LINK) > 0x0)
			addActionString(builder, "LINK");

		return builder.toString();
	}
}
