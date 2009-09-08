package edu.virginia.vcgr.genii.ui.shell.history;

import org.morgan.util.Pair;

public interface HistorySearch
{
	public Pair<String, String> addCharacter(char c);
	public Pair<String, String> search();
	public String getActualLine();
	public String getSearchWord();
}