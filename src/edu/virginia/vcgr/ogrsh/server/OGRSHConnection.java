package edu.virginia.vcgr.ogrsh.server;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.ogrsh.server.comm.CommUtils;
import edu.virginia.vcgr.ogrsh.server.comm.InvocationMatcher;
import edu.virginia.vcgr.ogrsh.server.comm.OGRSHOperation;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;
import edu.virginia.vcgr.ogrsh.server.handlers.DirectoryHandler;
import edu.virginia.vcgr.ogrsh.server.handlers.FileHandler;
import edu.virginia.vcgr.ogrsh.server.handlers.TestingHandler;
import edu.virginia.vcgr.ogrsh.server.packing.DefaultOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.session.Session;
import edu.virginia.vcgr.ogrsh.server.session.SessionManager;

public class OGRSHConnection implements Runnable
{
	static private Log _logger = LogFactory.getLog(OGRSHConnection.class);
	
	static private final String _EXCEPTION = "exception";
	static private final String _RESPONSE = "response";
	
	private SessionManager _sessionManager;
	private Session _mySession = null;
	private GUID _serverSecret;
	private SocketChannel _socketChannel;
	private boolean _done = false;
	private ByteBuffer _intBuffer;
	private ByteOrder _byteOrder;
	private InvocationMatcher _matcher;
	
	private void determineByteOrder() throws IOException
	{
		ByteBuffer shortBuffer = ByteBuffer.allocate(2);
		CommUtils.readFully(_socketChannel, shortBuffer);
		shortBuffer.flip();

		byte firstByte = shortBuffer.get();
		byte secondByte = shortBuffer.get();
		if (firstByte == (byte)0 && secondByte == (byte)1)
			_byteOrder = ByteOrder.BIG_ENDIAN;
		else if (firstByte == (byte)1 && secondByte == (byte)0)
			_byteOrder = ByteOrder.LITTLE_ENDIAN;
		else
			throw new IOException("Unable to determine byte order of client.");
		
		_intBuffer.order(_byteOrder);
	}
	
	private void validateConnection() throws IOException
	{
		CommUtils.readFully(_socketChannel, _intBuffer);
		_intBuffer.flip();
		int size = _intBuffer.getInt();
		if (size > 0)
		{
			ByteBuffer tmp = ByteBuffer.allocate(size);
			tmp.order(_byteOrder);
			CommUtils.readFully(_socketChannel, tmp);
			tmp.flip();
			String transmittedSecretString = new String(
				tmp.array(), "UTF-8");
			GUID transmittedSecret = GUID.fromString(transmittedSecretString);
			if (transmittedSecret.equals(_serverSecret))
				return;
		}
		
		throw new IOException("Unable to validate connection.  " +
			"Disconnecting unauthorized client.");
	}

	private ByteBuffer handleInvocation(ByteBuffer request) throws IOException
	{
		OGRSHException exception = null;
		DefaultOGRSHWriteBuffer writeBuffer = new DefaultOGRSHWriteBuffer(
			_byteOrder);
		
		writeBuffer.writeRaw(new byte[4], 0, 4);
		ByteBuffer ret = null;

		try
		{
			Object oRet = _matcher.invoke(request);
			writeBuffer.writeObject(_RESPONSE);
			writeBuffer.writeObject(oRet);
			ret = writeBuffer.compact();
		}
		catch (OGRSHException oe)
		{
			exception = oe;
		}
		catch (Throwable cause)
		{
			exception = new OGRSHException(cause);
		}
	
		if (ret == null)
		{	
			writeBuffer = new DefaultOGRSHWriteBuffer(_byteOrder);
			writeBuffer.writeRaw(new byte[4], 0, 4);
			writeBuffer.writeObject(_EXCEPTION);
			writeBuffer.writeObject(exception);
			ret = writeBuffer.compact();
		}

		ret.flip();
		ret.putInt(ret.remaining() - 4);
		ret.rewind();
		return ret;
	}
	
	public OGRSHConnection(SessionManager sessionManager,
		SocketChannel channel, GUID serverSecret)
	{
		_serverSecret = serverSecret;
		_socketChannel = channel;
		_sessionManager = sessionManager;
		
		_intBuffer = ByteBuffer.allocate(4);
		_matcher = new InvocationMatcher();
		addHandlers();
	}
	
	public void run()
	{
		try
		{
			try
			{
				determineByteOrder();
				validateConnection();
			}
			catch (IOException ioe)
			{
				_logger.fatal("Unable to establish client connection.", ioe);
				return;
			}
			
			try
			{
				while (!_done)
				{
					_intBuffer.rewind();
					try
					{
						CommUtils.readFully(_socketChannel, _intBuffer);
					}
					catch (IOException ioe)
					{
						_logger.debug(
							"Closing server because client closed socket.", ioe);
						_done = true;
						break;
					}
					_intBuffer.flip();
					int messageSize = _intBuffer.getInt();
					if (messageSize < 0)
					{
						_done = true;
					} else
					{
						ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
						messageBuffer.order(_byteOrder);
						CommUtils.readFully(_socketChannel, messageBuffer);
						messageBuffer.flip();
						ByteBuffer responseBuffer = handleInvocation(messageBuffer);
						responseBuffer.rewind();
						CommUtils.writeFully(_socketChannel, responseBuffer);
					}
				}
			}
			catch (IOException ioe)
			{
				_logger.error("Error reading/writing communication channel.", ioe);
			}
			
			try { _socketChannel.close(); } catch (Throwable cause) {}
		}
		finally
		{
			if (_mySession != null)
			{
				_sessionManager.releaseSession(_mySession);
			}
		}
	}
	
	private void addHandlers()
	{
		_matcher.addHandlerInstance(this);
		_matcher.addHandlerInstance(new TestingHandler());
		_matcher.addHandlerInstance(new DirectoryHandler());
		_matcher.addHandlerInstance(new FileHandler());
	}
	
	@OGRSHOperation
	public String setupConnection(String requestedSessionID)
		throws OGRSHException
	{
		if (_mySession != null)
			_sessionManager.releaseSession(_mySession);
		
		if (requestedSessionID == null)
		{
			_logger.debug("Creating a brand new session.");
			
			// Create new session
			_mySession = _sessionManager.createNewSession();
		} else
		{
			_logger.debug("Duplicating an existing session (" 
				+ requestedSessionID + ").");
			
			// load existing session
			GUID desiredSessionKey = GUID.fromString(requestedSessionID);
			_mySession = _sessionManager.duplicateSession(desiredSessionKey);
		}
		
		_logger.debug("Returning session \"" + _mySession.getSessionID() + "\".");
		return _mySession.getSessionID().toString();
	}
	
	@OGRSHOperation
	public int connectNet(String rootRNSUrl)
		throws OGRSHException
	{
		try
		{
			ICallingContext ctxt = ContextStreamUtils.load(new URL(rootRNSUrl));
			_mySession.setCallingContext(ctxt);
			ctxt.getActiveKeyAndCertMaterial();
			return 0;
		}
		catch (GeneralSecurityException gse)
		{
			throw new OGRSHException(OGRSHException.PERMISSION_DENIED,
				"Unable to initialize key and cert material.");
		}
		catch (MalformedURLException mue)
		{
			throw new OGRSHException(OGRSHException.MALFORMED_URL,
				"The URL \"" + rootRNSUrl + "\" is malformed.");
		}
		catch (IOException ioe)
		{
			throw new OGRSHException(OGRSHException.IO_EXCEPTION,
				"An IO Exception occured while trying to acquire root RNS EPR.");
		}
	}
	
	@OGRSHOperation
	public int loginSession(String file, String password, String patternOrUsername)
		throws OGRSHException
	{
		GamlLoginTool tool = new GamlLoginTool();
		
		try
		{
			if (file.startsWith("rns:"))
			{
				tool.addArgument("--username=" + patternOrUsername);
				tool.addArgument("--password=" + password);
				tool.addArgument(file);
			} else
			{
				tool.addArgument("--password=" + password);
				tool.addArgument("--pattern=" + patternOrUsername);
				tool.addArgument(file);
			}
			
			return tool.run(System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
		}
		catch (ToolException te)
		{
			// This shouldn't happen
			_logger.error("Unexpected login exception.", te);
			throw new OGRSHException("Unexpected login exception.", te);
		}
		catch (Throwable t)
		{
			_logger.warn("Unable to log in to grid from OGRSH.", t);
			throw new OGRSHException("Unable to log in to grid.", t);
		}
	}
}
