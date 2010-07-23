package edu.virginia.vcgr.genii.ui.shell;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.prefs.general.GeneralUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;
import edu.virginia.vcgr.genii.ui.shell.history.History;
import edu.virginia.vcgr.genii.ui.shell.history.HistoryIterator;
import edu.virginia.vcgr.genii.ui.shell.history.HistorySearch;
import edu.virginia.vcgr.genii.ui.shell.tokenizer.LineTokenizer;
import edu.virginia.vcgr.genii.ui.shell.tokenizer.Token;

public class CommandField extends JTextField
{
	static final private int DEFAULT_COLUMNS = 75;
	
	static final long serialVersionUID = 0L;
	
	private LineBasedReader _reader = null;
	
	private ExecutionContext _executionContext;
	private UIContext _uiContext;
	private Display _display;
	private JLabel _label;
	
	private String stripEscapesAndQuotes(String word)
	{
		StringBuilder builder = new StringBuilder();
		for (int lcv = 0; lcv < word.length(); lcv++)
		{
			char c = word.charAt(lcv);
			if (c == '\\')
			{
				lcv++;
				if (lcv >= word.length())
					continue;
				c = word.charAt(lcv);
				builder.append(c);
			} else if (c == '"')
				continue;
			else
				builder.append(c);
		}
		
		return builder.toString();
	}
	
	private String insertEscapesOrQuotes(boolean preferQuotes,
		String word)
	{
		StringBuilder builder = new StringBuilder();
		boolean needQuotes = false;
		
		for (int lcv = 0; lcv < word.length(); lcv++)
		{
			char c = word.charAt(lcv);
			if (c == '\\')
				builder.append("\\\\");
			else if (c == '"')
				builder.append("\\\"");
			else if (Character.isSpaceChar(c))
			{
				if (preferQuotes)
					needQuotes = true;
				else
					builder.append("\\");
				
				builder.append(c);
			} else
				builder.append(c);
		}
		
		if (needQuotes || preferQuotes)
			return String.format("\"%s\"", builder.toString());
		
		return builder.toString();
	}
			
	private String findCommonStart(String []words)
	{
		String ret = words[0];
		
		for (int lcv = 1; lcv < words.length; lcv++)
		{
			String test = words[lcv];
			for (int i = 0; (i < test.length()) && (i < ret.length()); i++)
			{
				if (ret.charAt(i) != test.charAt(i))
					ret = ret.substring(0, i);
			}
		}
		
		return ret;
	}
	
	public CommandField(UIContext uiContext, JLabel label, Display display,
		ExecutionContext executionContext, int columns)
	{
		super(columns);
		
		_label = label;
		_display = display;
		_uiContext = uiContext;
		_executionContext = executionContext;
		
		InputBindings inputBindings = uiContext.preferences().preferenceSet(
			GeneralUIPreferenceSet.class).createBindings();
		
		setHighlighter(null);
		
		setFocusTraversalKeysEnabled(false);
		addKeyListener(inputBindings);
		inputBindings.addBindingActionListener(new FieldActioner());
	}
	
	public CommandField(UIContext uiContext, JLabel label,
		Display display, ExecutionContext executionContext)
	{
		this(uiContext, label, display, executionContext, DEFAULT_COLUMNS);
	}
	
	private class FieldActioner extends BindingActionAdapter
	{
		private History _history = new History();
		private HistoryIterator _historyIterator = null;
		private HistorySearch _historySearch = null;
		
		@Override
		public void addCharacter(char c)
		{
			Caret caret = getCaret();
			
			if (_historySearch != null)
			{
				Pair<String, String> pair = _historySearch.addCharacter(c);
				if (pair == null)
					beep();
				else
				{
					setText(pair.first() + pair.second());
					caret.setDot(pair.first().length());
				}
			} else
			{	
				String text = getText();
				int position = caret.getDot();
				setText(text.substring(0, position) + c +
					text.substring(position));
				getCaret().setDot(position + 1);
				
				_historyIterator = null;
			}
		}

		@Override
		public void backspace()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			String text = getText();
			int position = caret.getDot();
			if (position == 0)
				beep();
			else
			{
				position--;
				setText(text.substring(0, position) +
					text.substring(position + 1));
				caret.setDot(position);
			}
			
			_historyIterator = null;
		}

		@Override
		public void backwardHistory()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			if (_historyIterator == null)
				_historyIterator = _history.startIteration();
			
			String newLine = _historyIterator.searchBackword();
			if (newLine == null)
				beep();
			else
			{
				setText(newLine);
				getCaret().setDot(newLine.length());
			}
		}

		@Override
		public void beep()
		{
			Toolkit.getDefaultToolkit().beep();
		}

		@Override
		public void clear()
		{
			LineBasedReader reader = _reader;
			
			if (reader != null)
			{
				_reader = null;
				StreamUtils.close(reader);
			}
			
			if (_historySearch != null)
				_historySearch = null;
			
			setText("");
			getCaret().setDot(0);
			
			_historyIterator = null;
		}

		@Override
		public void delete()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			String text = getText();
			int position = caret.getDot();
			if (position >= text.length())
				beep();
			else
			{
				setText(text.substring(0, position) + 
					text.substring(position + 1));
				caret.setDot(position);
			}
			
			_historyIterator = null;
		}

		@Override
		public void end()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			caret.setDot(getText().length());
		}

		@Override
		public void enter()
		{
			if (_historySearch != null)
			{
				setText(_historySearch.getActualLine());
				getCaret().setDot(0);
				_historySearch = null;
			}
			
			String line = getText();
			clear();
			
			LineBasedReader reader = _reader;
			if (reader == null)
			{
				line = line.trim();
				if (line.length() == 0)
					return;
				
				if (line.equals("quit") || line.equals("exit"))
				{
					SwingUtilities.windowForComponent(CommandField.this).dispose();
					return;
				}
				
				_history.addLine(line);
				
				_label.setText("");
				_reader = reader = new LineBasedReader();
				_display.start();
				_display.header().format("Command:  ");
				_display.command().println(line);
				_uiContext.progressMonitorFactory().monitor(CommandField.this, 
					"Executing Command", "Executing command.", 1000L,
					new CommandExecutionTask(line, reader),
					new CommandCompletionListener());
			} else
			{
				reader.addLine(line);
			}
			
			_historyIterator = null;
		}

		@Override
		public void forwardHistory()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			if (_historyIterator == null)
				_historyIterator = _history.startIteration();
			
			String newLine = _historyIterator.searchForward();
			if (newLine == null)
				beep();
			else
			{
				setText(newLine);
				getCaret().setDot(newLine.length());
			}
		}

		@Override
		public void home()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			caret.setDot(0);
		}

		@Override
		public void left()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			int position = caret.getDot();
			if (position == 0)
				beep();
			else
				caret.setDot(position - 1);
		}

		@Override
		public void right()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			int position = caret.getDot();
			if (position >= getText().length())
				beep();
			else
				caret.setDot(position + 1);
		}
		
		@Override
		public void complete()
		{
			if (_historySearch != null)
			{
				beep();
				return;
			}
			
			Caret caret = getCaret();
			int position = caret.getDot();
			String left = getText();
			String right = left.substring(position);
			left = left.substring(0, position);
			Token []words = LineTokenizer.tokenize(left);
			
			int wordCount = 0;
			WordCompleter completer = null;
			for (Token word : words)
			{
				if (!word.isSpaceToken())
					wordCount++;
			}
			
			String lastWord = "";
			String partial = "";
			if (words.length > 0)
				lastWord = words[words.length - 1].token();
			if(lastWord.startsWith("-"))
			{
				completer = _executionContext.optionCompleter();
				partial = stripEscapesAndQuotes(left);
			}
			else 
			{
				partial = stripEscapesAndQuotes(lastWord);
				if (wordCount <= 1)
					completer = _executionContext.commandCompleter();
				else
					completer = _executionContext.pathCompleter();
			}
			
			if (completer == null)
				beep();
			else
			{
				setText("forming completions...");
				setEnabled(false);
				_uiContext.progressMonitorFactory().monitor(
					CommandField.this, "Forming Completions",
					"Forming completions.", 1000L,
					new CompleterTask(completer, partial),
					new CompletionFinisher(lastWord, right, words, left));
			}
			
			_historyIterator = null;
		}
		
		@Override
		public void search()
		{
			Pair<String, String> pair;
			
			if (_historySearch == null)
				_historySearch = _history.startSearch();
			
			pair = _historySearch.search();
			if (pair == null)
			{
				beep();
			} else
			{
				setText(pair.first() + pair.second());
				getCaret().setDot(pair.first().length());
			}
			
			_historyIterator = null;
		}
		
		@Override
		public void stopSearch()
		{
			if (_historySearch != null)
			{
				setText(_historySearch.getActualLine());
				getCaret().setDot(0);
				_historySearch = null;
			}
		}
	}
	
	private class CompleterTask extends AbstractTask<String[]>
	{
		private WordCompleter _completer;
		private String _partial;
		
		private CompleterTask(WordCompleter completer, String partial)
		{
			_completer = completer;
			_partial = partial;
		}
		
		@Override
		public String[] execute(TaskProgressListener progressListener)
				throws Exception
		{
			return _completer.completions(_partial);
		}

		@Override
		public boolean showProgressDialog()
		{
			return true;
		}
	}
	
	private class CompletionFinisher
		implements TaskCompletionListener<String[]>
	{
		private String _lastWord;
		private String _right;
		private Token []_words;
		private String _originalLeft;
		
		private CompletionFinisher(String lastWord, String right,
			Token []words, String originalLeft)
		{
			_lastWord = lastWord;
			_right = right;
			_words = words;
			_originalLeft = originalLeft;
		}
		
		@Override
		public void taskCancelled(Task<String[]> task)
		{
			setEnabled(true);
			grabFocus();
			_display.output().println("Completions cancelled.");
			setText(_originalLeft + _right);
			getCaret().setDot(_originalLeft.length());
		}

		@Override
		public void taskCompleted(Task<String[]> task, String[] completions)
		{
			String newWord;
			
			setEnabled(true);
			grabFocus();
			
			if (completions == null || completions.length == 0)
			{
				setText(_originalLeft + _right);
				getCaret().setDot(_originalLeft.length());
				Toolkit.getDefaultToolkit().beep();
			} else
			{
				if (completions.length == 1)
					newWord = insertEscapesOrQuotes(
						_lastWord.startsWith("\""), completions[0]);
				else
				{
					_display.start();
					_display.header().println("Completions:\n");
					
					for (String completion : completions)
						_display.output().format("\t%s\n", completion);
					
					newWord = insertEscapesOrQuotes(
						_lastWord.startsWith("\""),
						findCommonStart(completions));
				}
				
				StringBuilder builder = new StringBuilder();
				for (int lcv = 0; lcv < _words.length - 1; lcv++)
					builder.append(_words[lcv].token());
				if (newWord.endsWith("\"") && _right.startsWith("\""))
					_right = _right.substring(1);
				builder.append(newWord);
				int position = builder.length();
				if (newWord.endsWith("\""))
					position--;
				builder.append(_right);
				setText(builder.toString());
				getCaret().setDot(position);
			}
		}

		@Override
		public void taskExcepted(Task<String[]> task, Throwable cause)
		{
			setEnabled(true);
			grabFocus();
			_display.error().println(
				"Unable to generate completions.");
			setText(_originalLeft + _right);
			getCaret().setDot(_originalLeft.length());
		}
	}
	
	private class CommandExecutionTask extends AbstractTask<Integer>
	{
		private String _line;
		private LineBasedReader _reader;
		
		private CommandExecutionTask(String line, LineBasedReader reader)
		{
			_line = line;
			_reader = reader;
		}
		
		@Override
		public Integer execute(TaskProgressListener progressListener)
				throws Exception
		{
			_executionContext.executeCommand(_line, _display, _reader);
			return null;
		}

		@Override
		public boolean showProgressDialog()
		{
			return false;
		}
	}
	
	private class CommandCompletionListener
		implements TaskCompletionListener<Integer>
	{
		private void finishCommand()
		{
			_label.setText("Command");
			_reader = null;
			CommandField.this.grabFocus();
		}
		
		@Override
		public void taskCancelled(Task<Integer> task)
		{
			_display.output().println("Cancelled!");
			finishCommand();
		}

		@Override
		public void taskCompleted(Task<Integer> task, Integer result)
		{
			finishCommand();
		}

		@Override
		public void taskExcepted(Task<Integer> task, Throwable cause)
		{
			_display.error().println("Task failed to execute!");
			cause.printStackTrace(_display.error());
			finishCommand();
		}
	}
}