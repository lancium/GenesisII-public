package org.morgan.ftp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.morgan.ftp.cmd.CWDCommandHandler;
import org.morgan.ftp.cmd.DeleteCommandHandler;
import org.morgan.ftp.cmd.ListCommandHandler;
import org.morgan.ftp.cmd.MkdirCommandHandler;
import org.morgan.ftp.cmd.NLSTCommandHandler;
import org.morgan.ftp.cmd.PASVCommandHandler;
import org.morgan.ftp.cmd.PWDCommandHandler;
import org.morgan.ftp.cmd.PassCommandHandler;
import org.morgan.ftp.cmd.QUITCommandHandler;
import org.morgan.ftp.cmd.RenameFromCommandHandler;
import org.morgan.ftp.cmd.RenameToCommandHandler;
import org.morgan.ftp.cmd.RetrieveCommandHandler;
import org.morgan.ftp.cmd.RmDirCommandHandler;
import org.morgan.ftp.cmd.SizeCommandHandler;
import org.morgan.ftp.cmd.StoreCommandHandler;
import org.morgan.ftp.cmd.TypeCommandHandler;
import org.morgan.ftp.cmd.UserCommandHandler;
import org.morgan.util.io.StreamUtils;

public class FTPSession implements Runnable, Closeable
{
	static private Logger _logger = Logger.getLogger(FTPSession.class);
	
	private int _authAttemptCount = 0;
	private FTPConfiguration _configuration;
	
	private Socket _socket;
	private HashMap<String, ICommand> _commands;
	private FTPSessionState _sessionState;
	
	private IdleTimer _idleTimer = new IdleTimer();
	
	private void addCommand(ICommand handler)
	{
		for (String verb : handler.getHandledVerbs())
		{
			_commands.put(verb, handler);
		}
	}
	
	private void addCommands() throws NoSuchMethodException
	{
		addCommand(new ReflectiveCommand(UserCommandHandler.class, "USER"));
		addCommand(new ReflectiveCommand(PassCommandHandler.class, "PASS"));
		addCommand(new ReflectiveCommand(PASVCommandHandler.class, "PASV"));
		addCommand(new ReflectiveCommand(ListCommandHandler.class, "LIST"));
		addCommand(new ReflectiveCommand(NLSTCommandHandler.class, "NLST"));
		addCommand(new ReflectiveCommand(CWDCommandHandler.class, "CWD", "XCWD"));
		addCommand(new ReflectiveCommand(QUITCommandHandler.class, "QUIT"));
		addCommand(new ReflectiveCommand(MkdirCommandHandler.class, "MKD"));
		addCommand(new ReflectiveCommand(PWDCommandHandler.class, "PWD"));
		addCommand(new ReflectiveCommand(RetrieveCommandHandler.class, "RETR"));
		addCommand(new ReflectiveCommand(TypeCommandHandler.class, "TYPE"));
		addCommand(new ReflectiveCommand(StoreCommandHandler.class, "STOR"));
		addCommand(new ReflectiveCommand(SizeCommandHandler.class, "SIZE"));
		addCommand(new ReflectiveCommand(RmDirCommandHandler.class, "RMD"));
		addCommand(new ReflectiveCommand(RenameFromCommandHandler.class, "RNFR"));
		addCommand(new ReflectiveCommand(RenameToCommandHandler.class, "RNTO"));
		addCommand(new ReflectiveCommand(DeleteCommandHandler.class, "DELE"));
	}
	
	FTPSession(FTPListenerManager manager,
		FTPConfiguration configuration, int sessionID, IBackend backend, Socket socket)
	{
		_configuration = configuration;
		_socket = socket;
		
		_commands = new HashMap<String, ICommand>();
		_sessionState = new FTPSessionState(manager, configuration, backend, sessionID);
		
		try
		{
			addCommands();
		}
		catch (NoSuchMethodException nsme)
		{
			_logger.error("Problem registering command handlers.", nsme);
		}
		
		manager.fireSessionOpened(sessionID);
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}

	public Date getLastActivity()
	{
		RollingCommandHistory history = _sessionState.getHistory();
		
		synchronized(history)
		{
			FTPAction action = history.lastCommand();
			if (action == null)
				return null;
			
			Date completed = action.completed();
			if (completed == null)
				return new Date();
			
			return completed;
		}
	}
	
	@Override
	public void run()
	{
		RollingCommandHistory history = _sessionState.getHistory();
		FTPAction action;
		
		BufferedReader reader = null;
		PrintStream out = null;
		
		String line;
		Pattern verbExtractor = Pattern.compile(
			"\\s*(\\w+)\\s*(.*)\\s*");
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(
				_socket.getInputStream()));
			out = new FTPPrintStream(_socket.getOutputStream(), true);
			
			out.println("220 " + _sessionState.getBackend().getGreeting());
			_logger.info("220 " + _sessionState.getBackend().getGreeting());
			
			while ( (line = reader.readLine()) != null)
			{
				synchronized(_idleTimer)
				{
					_idleTimer.noteActivity();
				}
				
				_logger.debug("FTP Session Received \"" + line + "\".");
				
				Matcher matcher = verbExtractor.matcher(line);
				if (!matcher.matches())
				{
					_logger.error("FTP command unrecognized.");
					continue;
				}
				
				String verb = matcher.group(1);
				String parameters = matcher.group(2);
				
				ICommand command = _commands.get(verb);
				
				try
				{
					if (command == null)
						throw new UnimplementedException(verb);
					
					ICommandHandler handler = command.createHandler();
					synchronized(history)
					{
						action = history.addCommand(handler);
					}
					
					try
					{
						handler.handleCommand(_sessionState, verb, parameters, out);
					}
					finally
					{
						synchronized(history)
						{
							action.complete();
						}
					}
				}
				catch (AuthorizationFailedException ue)
				{
					if (++_authAttemptCount >= _configuration.getMissedAuthenticationsLimit())
					{
						_logger.info("Too many authentication failures...closing session.");
						
						ConnectionCloseException cce = new ConnectionCloseException();
						cce.communicate(_logger);
						cce.communicate(out);
						
						break;
					}
					
					ue.communicate(_logger);
					ue.communicate(out);
				}
				catch (FTPException ftpe)
				{
					ftpe.communicate(_logger);
					ftpe.communicate(out);
				}
			}
		}
		catch (IOException ioe)
		{
			_logger.warn("Unknown IO Exception in FTP Session.", ioe);
		}
		finally
		{
			_logger.info("Closing FTP Session.");
			
			StreamUtils.close(out);
			StreamUtils.close(reader);
			
			StreamUtils.close(this);
			
			_sessionState.getListenerManager().fireSessionClosed(_sessionState.getSessionID());
		}
	}
	
	public long getIdleTime()
	{
		synchronized(_idleTimer)
		{
			return _idleTimer.idleTime();
		}
	}
	
	public int getSessionID()
	{
		return _sessionState.getSessionID();
	}
	
	public long getIdleTimeout()
	{
		return _sessionState.getConfiguration().getIdleTimeoutSeconds() * 1000;
	}

	@Override
	synchronized public void close() throws IOException
	{
		StreamUtils.close(_sessionState);
		
		if (_socket != null)
		{
			try { _socket.shutdownInput(); } catch (Throwable t) {}
			try { _socket.shutdownOutput(); } catch (Throwable t) {}
			StreamUtils.close(_socket);
			_socket = null;
		}
	}
}