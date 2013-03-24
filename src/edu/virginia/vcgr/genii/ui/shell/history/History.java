package edu.virginia.vcgr.genii.ui.shell.history;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.morgan.util.Pair;

public class History
{
	static final public int DEFAULT_HISTORY_SIZE = 1024;

	private int _historySize;
	private LinkedList<String> _history = new LinkedList<String>();

	public History(int historySize)
	{
		_historySize = historySize;
	}

	public History()
	{
		this(DEFAULT_HISTORY_SIZE);
	}

	final public void addLine(String line)
	{
		_history.addFirst(line);
		while (_history.size() > _historySize)
			_history.removeLast();
	}

	final public HistoryIterator startIteration()
	{
		return new HistoryIteratorImpl(_history.listIterator());
	}

	final public HistorySearch startSearch()
	{
		return new HistorySearchImpl(_history.iterator());
	}

	static private class HistoryIteratorImpl implements HistoryIterator
	{
		private boolean _wasBackword = true;
		private ListIterator<String> _listIterator;

		private HistoryIteratorImpl(ListIterator<String> listIterator)
		{
			_listIterator = listIterator;
		}

		@Override
		final public String searchBackword()
		{
			if (!_wasBackword && _listIterator.hasNext())
				_listIterator.next();

			if (!_listIterator.hasNext())
				return null;

			return _listIterator.next();
		}

		@Override
		final public String searchForward()
		{
			if (_wasBackword && _listIterator.hasPrevious())
				_listIterator.previous();

			if (!_listIterator.hasPrevious())
				return null;

			return _listIterator.previous();
		}
	}

	static private class HistorySearchImpl implements HistorySearch
	{
		private String _searchWord;
		private String _currentLine;
		private Iterator<String> _historyIterator;

		private HistorySearchImpl(Iterator<String> historyIterator)
		{
			_searchWord = "";
			_historyIterator = historyIterator;
			_currentLine = null;
		}

		@Override
		public Pair<String, String> addCharacter(char c)
		{
			_searchWord = _searchWord + c;
			if (_currentLine != null && _currentLine.contains(_searchWord))
				return toPair();

			while (_historyIterator.hasNext()) {
				_currentLine = _historyIterator.next();
				if (_currentLine.contains(_searchWord))
					return toPair();
			}

			return null;
		}

		@Override
		public String getActualLine()
		{
			return _currentLine;
		}

		@Override
		public String getSearchWord()
		{
			return _searchWord;
		}

		@Override
		public Pair<String, String> search()
		{
			while (_historyIterator.hasNext()) {
				_currentLine = _historyIterator.next();
				if (_currentLine.contains(_searchWord))
					return toPair();
			}

			return null;
		}

		@Override
		public String toString()
		{
			return String.format("(reverse-i-search)`%s':  %s", _searchWord, _currentLine);
		}

		public Pair<String, String> toPair()
		{
			StringBuilder first = new StringBuilder(String.format("(reverse-i-search)`%s':  ", _searchWord));
			int index = _currentLine.indexOf(_searchWord);
			first.append(_currentLine.substring(0, index));
			return new Pair<String, String>(first.toString(), _currentLine.substring(index));
		}
	}
}