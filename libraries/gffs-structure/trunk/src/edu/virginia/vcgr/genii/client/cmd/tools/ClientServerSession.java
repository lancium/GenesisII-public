package edu.virginia.vcgr.genii.client.cmd.tools;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.morgan.ftp.ConnectionSession;
import org.morgan.ftp.FTPConfiguration;
import org.morgan.ftp.FTPListenerManager;
import org.morgan.ftp.FTPPrintStream;
import org.morgan.ftp.FTPSession;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.IdleTimer;
import org.morgan.ftp.ReflectiveCommand;
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

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ExceptionHandlerManager;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;

public class ClientServerSession extends ConnectionSession implements Runnable, Closeable
{
	static private Logger _logger = Logger.getLogger(FTPSession.class);
	// 2020-10-10 by ASG. 
	// We are having a race condition when multiple client server sessions are active at a time. We do not know why. We
	// are looking into it. Until then we will serialize the sessions by performing a schronized block around the actual 
	// implementation code.
	//static private String sync=new String("sync var");
	// end of update

//	private int _authAttemptCount = 0;
	private FTPConfiguration _configuration;

	private Socket _socket;
	private HashMap<String, ICommand> _commands;
	private FTPSessionState _sessionState;

	private IdleTimer _idleTimer = new IdleTimer();

	private void addCommand(ICommand handler)
	{
		for (String verb : handler.getHandledVerbs()) {
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

	ClientServerSession(FTPListenerManager manager, FTPConfiguration configuration, int sessionID, IBackend backend, Socket socket)
	{
		_configuration = configuration;
		_socket = socket;

		_commands = new HashMap<String, ICommand>();
		_sessionState = new FTPSessionState(manager, _configuration, backend, sessionID);

		try {
			addCommands();
		} catch (NoSuchMethodException nsme) {
			_logger.error("Problem registering command handlers.", nsme);
		}

		manager.fireSessionOpened(sessionID);
	}

	protected void finalize() throws Throwable
	{
		super.finalize();

		close();
	}

	@Override
	public void run()
	{
		BufferedReader reader = null;
		PrintStream out = null;
		PrintWriter outwriter = null;

		String line;
		// 2020-10-10 by ASG. Added to serialize calls while we figure out the race.
//		synchronized(sync) {
			// End of update
		try {
			// get the local identity's key material (or create one if necessary)
			ICallingContext initialCallingContext = ContextManager.getCurrentOrMakeNewContext();
	
			reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			out = new FTPPrintStream(_socket.getOutputStream(), true);
			outwriter=new PrintWriter(_socket.getOutputStream());
//			out.println("220 " + _sessionState.getBackend().getGreeting());
//			_logger.info("220 " + _sessionState.getBackend().getGreeting());

			// sentinel that lets us know it's okay to execute commands.
			boolean gotNonceOrLogin = false;
			
			while ((line = reader.readLine()) != null) {
				synchronized (_idleTimer) {
					_idleTimer.noteActivity();
				}
//				System.out.println(line);
				// 2019-09-18 by ASg. So, we have a problem that if a password in a login has a "$" in it, formCommandLine does variable expansion.
				// We cannot allow this to happen. So we are adding a new parameter to formCommandLine so that variables are not expanded.
				// Then, if it is a login ... we do NOT expand variables in the command line.
				String[] args = null;
				if ((line.indexOf("context") == 0) && (line.indexOf("create-user") == -1)) {
					args = CommandLineFormer.formCommandLine(line);
				}
				else {
					args = CommandLineFormer.formCommandLine(line,false);
				}
				if (args.length == 0)
					continue;
				CommandLineRunner runner = new CommandLineRunner();
				int firstArg = 0;
				String nonce = null;
				// First lets check for "context" as the first word
				if (line.indexOf("context") == 0) {
					// The line is of the form "context <some-nonce> command arguments"
					// Now we need to set first arg to 2 to skip past the first two
					firstArg=2;
					// Now look up the nonce
					nonce = args[1];
					_logger.debug("nonce requested: " + nonce);
					ICallingContext context = null;
					try {
						context = ContextManager.grab(nonce);
					} catch (ClassNotFoundException e) {
						_logger.error("exception occurred while grabbing context", e);
					}
					if (context == null) {
						outwriter.println("#nonce-invalid=" + nonce);
					} else {	
						// use a memory based resolver to load our saved context; we don't want to use the file based one.
						MemoryBasedContextResolver ourResolver = new MemoryBasedContextResolver(context);			
						ContextManager.setResolver(ourResolver);
						
						// now set that we have a valid nonce.
						gotNonceOrLogin = true;
					}
				} else if (line.indexOf("login") == 0) {
					// the only other thing they can do is create a nonce with a login command, so we allow that.
					gotNonceOrLogin = true;
					// make sure we are prepared for the login.
					LogoutTool.logoutAll(initialCallingContext);
				}
				
				try {
					if (!gotNonceOrLogin) {
						String msg = "#failed to create nonce or acquire nonce context before attempting to run a command; closing connection";
						outwriter.println(msg);
						_logger.error(msg);
						outwriter.println("#return=1");
						return; 
					}					

					long startTime = System.currentTimeMillis();
					String[] passArgs = new String[args.length - firstArg];
					System.arraycopy(args, firstArg, passArgs, 0, passArgs.length);

					int lastExit = runner.runCommand(passArgs, outwriter, outwriter, reader);
					outwriter.println("#return=" + lastExit);

					long elapsed = System.currentTimeMillis() - startTime;

					// if the nonce was set, then store the context again after possible changes.
					if (nonce != null) {
						ICallingContext context = ContextManager.getCurrentContext();
						ContextManager.stash(nonce, context);
					}

					long hours = elapsed / (60 * 60 * 1000);
					if (hours != 0)
						elapsed = elapsed % (hours * 60 * 60 * 1000);
					long minutes = elapsed / (60 * 1000);
					if (minutes != 0)
						elapsed = elapsed % (minutes * 60 * 1000);
					long seconds = elapsed / (1000);
					if (seconds != 0)
						elapsed = elapsed % (seconds * 1000);
					outwriter.println("#elapsed time: " + hours + "h:" + minutes + "m:" + seconds + "s." + elapsed + "ms");
				} catch (Throwable cause) {
					int toReturn = ExceptionHandlerManager.getExceptionHandler().handleException(cause, outwriter);
					outwriter.println("#exception handler invoked=" + toReturn);
					outwriter.println("#return=1");
				} 
				
				// Use a break to get out. It should really just be straight line code.
				break;
			}
		} catch (Throwable e) {
			if (outwriter != null) {
				outwriter.println("#Throwable exception seen");
				outwriter.println(e.getMessage());
				outwriter.println("#return=1");
			}
			_logger.error("caught exception in outer block of client server session", e);
		} finally {
			_logger.info("Closing ClientServer Session.");

			if (outwriter != null) outwriter.flush();

			StreamUtils.close(out);
			StreamUtils.close(reader);

			StreamUtils.close(this);

			_sessionState.getListenerManager().fireSessionClosed(_sessionState.getSessionID());
		}
//		}
	}

	public long getIdleTime()
	{
		synchronized (_idleTimer) {
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

		if (_socket != null) {
			try {
				_socket.shutdownInput();
			} catch (Throwable t) {
			}
			try {
				_socket.shutdownOutput();
			} catch (Throwable t) {
			}
			StreamUtils.close(_socket);
			_socket = null;
		}
	}
}