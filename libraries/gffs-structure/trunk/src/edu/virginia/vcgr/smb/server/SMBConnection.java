package edu.virginia.vcgr.smb.server;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.ogrsh.server.comm.CommUtils;
import edu.virginia.vcgr.smb.server.cmd.SMBCheckDirectory;
import edu.virginia.vcgr.smb.server.cmd.SMBClose;
import edu.virginia.vcgr.smb.server.cmd.SMBCreate;
import edu.virginia.vcgr.smb.server.cmd.SMBCreateDirectory;
import edu.virginia.vcgr.smb.server.cmd.SMBCreateNew;
import edu.virginia.vcgr.smb.server.cmd.SMBDelete;
import edu.virginia.vcgr.smb.server.cmd.SMBDeleteDirectory;
import edu.virginia.vcgr.smb.server.cmd.SMBEcho;
import edu.virginia.vcgr.smb.server.cmd.SMBFindClose2;
import edu.virginia.vcgr.smb.server.cmd.SMBFlush;
import edu.virginia.vcgr.smb.server.cmd.SMBLogoffAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBNTCreateAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBNegotiate;
import edu.virginia.vcgr.smb.server.cmd.SMBOpen;
import edu.virginia.vcgr.smb.server.cmd.SMBOpenAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBQueryInformation;
import edu.virginia.vcgr.smb.server.cmd.SMBQueryInformation2;
import edu.virginia.vcgr.smb.server.cmd.SMBQueryInformationDisk;
import edu.virginia.vcgr.smb.server.cmd.SMBRead;
import edu.virginia.vcgr.smb.server.cmd.SMBReadAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBRename;
import edu.virginia.vcgr.smb.server.cmd.SMBSearch;
import edu.virginia.vcgr.smb.server.cmd.SMBSeek;
import edu.virginia.vcgr.smb.server.cmd.SMBSessionSetupAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBSetInformation;
import edu.virginia.vcgr.smb.server.cmd.SMBSetInformation2;
import edu.virginia.vcgr.smb.server.cmd.SMBTransaction2;
import edu.virginia.vcgr.smb.server.cmd.SMBTreeConnect;
import edu.virginia.vcgr.smb.server.cmd.SMBTreeConnectAndX;
import edu.virginia.vcgr.smb.server.cmd.SMBTreeDisconnect;
import edu.virginia.vcgr.smb.server.cmd.SMBWrite;
import edu.virginia.vcgr.smb.server.cmd.SMBWriteAndX;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2FindFirst;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2FindNext;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2QueryFileInformation;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2QueryFsInfo;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2QueryPathInformation;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2SetFileInformation;
import edu.virginia.vcgr.smb.server.trans2.SMBTrans2SetPathInformation;

public class SMBConnection implements Runnable
{
	private SocketChannel client;
	private int maxBufferSize = 0x20000;
	private SMBDialect dialect = SMBDialect.CORE;

	static private Log _logger = LogFactory.getLog(SMBConnection.class);

	public static SMBCommand[] commands = new SMBCommand[256];
	static {
		// Core (0x0 - 0x12)
		commands[0x0] = new SMBCreateDirectory();
		commands[0x1] = new SMBDeleteDirectory();
		commands[0x2] = new SMBOpen();
		commands[0x3] = new SMBCreate();
		commands[0x4] = new SMBClose();
		commands[0x5] = new SMBFlush();
		commands[0x6] = new SMBDelete();
		commands[0x7] = new SMBRename();
		commands[0x8] = new SMBQueryInformation();
		commands[0x9] = new SMBSetInformation();
		commands[0xA] = new SMBRead();
		commands[0xB] = new SMBWrite();
		commands[0xF] = new SMBCreateNew();
		commands[0x10] = new SMBCheckDirectory();
		commands[0x12] = new SMBSeek();
		// CorePlus (0x13 - 0x14)
		// Unused (0x15 - 0x19)
		// CorePlus (0x1A, READ_RAW)
		// LM10 (0x1B - 0x1C)
		// CorePlus (0x1D, WRITE_RAW)
		// LM10 (0x1E - 0x20)
		// Reserved (0x21)
		// LM10 (0x22 - 0x2F)
		commands[0x22] = new SMBSetInformation2();
		commands[0x23] = new SMBQueryInformation2();
		commands[0x2B] = new SMBEcho();
		commands[0x2D] = new SMBOpenAndX();
		commands[0x2E] = new SMBReadAndX();
		commands[0x2F] = new SMBWriteAndX();
		// Reserved (0x30)
		// NTLM (0x31, CLOSE_AND_TREE_DISC)
		// LM12 (0x32 - 0x35)
		commands[0x32] = new SMBTransaction2();
		commands[0x34] = new SMBFindClose2();
		// Unused (0x36 - 0x5F)
		// Reserved (0x60 - 0x6F)
		// Core (0x70 - 0x72)
		commands[0x70] = new SMBTreeConnect();
		commands[0x71] = new SMBTreeDisconnect();
		commands[0x72] = new SMBNegotiate();
		// LM10
		commands[0x73] = new SMBSessionSetupAndX();
		// LM12
		commands[0x74] = new SMBLogoffAndX();
		// LM10 (0x75)
		commands[0x75] = new SMBTreeConnectAndX();
		// Unused (0x76 - 0x7D)
		// LM10 (0x7E, SECURITY_PACKAGE_ANDX)
		// Unused (0x7F)
		// Core (0x80 - 0x81)
		commands[0x80] = new SMBQueryInformationDisk();
		commands[0x81] = new SMBSearch(false, false, false);
		// LM10 (0x82 - 0x84)
		commands[0x82] = new SMBSearch(true, false, false);
		commands[0x83] = new SMBSearch(false, true, false);
		commands[0x84] = new SMBSearch(false, false, true);
		// Unused (0x85 - 0x9f)
		// NTLM (0xA0 - 0xA2)
		commands[0xA2] = new SMBNTCreateAndX();
		// Unused (0xA3)
		// NTLM (0xA4 - 0xA5)
		// Unused (0xA6 - 0xBF)
		// Core (0xC0 - 0xC3)
		// Unused (0xC4 - 0xCF)
		// Reserved (0xD0 - 0xDA)
		// Unused (0xDB - 0xFD)
		// LM10 (0xFE, INVALID)
		// LM10 (0xFF, NO_ANDX_COMMAND)
	}

	public boolean negotiated = false;

	private HashMap<Integer, SMBTree> trees = new HashMap<Integer, SMBTree>();

	/**
	 * Are we parsing a transaction that requires multiple messages.
	 */
	private SMBTransactionInfo transPending;

	private static SMBTrans2Command[] cmdTrans2 = new SMBTrans2Command[256];
	static {
		cmdTrans2[1] = new SMBTrans2FindFirst();
		cmdTrans2[2] = new SMBTrans2FindNext();
		cmdTrans2[3] = new SMBTrans2QueryFsInfo();
		cmdTrans2[5] = new SMBTrans2QueryPathInformation();
		cmdTrans2[6] = new SMBTrans2SetPathInformation();
		cmdTrans2[7] = new SMBTrans2QueryFileInformation();
		cmdTrans2[8] = new SMBTrans2SetFileInformation();
	};

	public SMBConnection(SocketChannel socketChannel)
	{
		this.client = socketChannel;
	}

	/**
	 * Allocate a new TID for the newly opened tree.
	 * 
	 * @param tree
	 *            The tree to attach
	 * @return The TID for the tree.
	 * @throws SMBException
	 *             All TIDs have been exhausted.
	 */
	public int allocateTID(SMBTree tree) throws SMBException
	{
		/* future: improve */
		for (int i = 0; i < 0xffff; i++) {
			if (trees.get(i) == null) {
				trees.put(i, tree);

				return i;
			}
		}

		throw new SMBException(NTStatus.INSUFF_SERVER_RESOURCES);
	}

	/**
	 * Return the tree at the TID, raising an exception if the TID is invalid.
	 * 
	 * @param TID
	 * @return The tree root.
	 * @throws SMBException
	 *             The TID is invalid.
	 */
	public SMBTree verifyTID(int TID) throws SMBException
	{
		SMBTree tree = trees.get(TID);
		if (tree == null)
			throw new SMBException(NTStatus.SMB_BAD_TID);
		return tree;
	}

	public void releaseTID(short tid)
	{
		trees.remove(tid);
	}

	/**
	 * Convert the long filename to 8.3 format iff it can be done losslessly (such that it is invertible).
	 * 
	 * @param input
	 * @return
	 */
	public static String filenameShortenLossless(String input)
	{
		if (input.equals(".") || input.equals(".."))
			return input;

		// String valid = input.replaceAll("[\"\\\\/ \\[\\]:;=,]", "");
		// if (!valid.equals(input))
		// return null;

		String trim = input.replaceFirst("[.]*^", "");
		if (!trim.equals(input))
			return null;

		int pos = trim.lastIndexOf(".");
		String suffix, prefix;
		if (pos == -1) {
			suffix = "";
			prefix = input;
		} else {
			suffix = input.substring(pos + 1);
			prefix = input.substring(0, pos);
		}

		if (prefix.length() > 8)
			return null;

		if (suffix.length() > 3)
			return null;

		// prefix = (prefix + "        ").substring(0, 8);
		// suffix = (suffix + "   ").substring(0, 3);

		if (suffix.isEmpty()) {
			return prefix;
			// return prefix.toUpperCase();
		} else {
			return prefix + "." + suffix;
			// return prefix.toUpperCase() + "." + suffix.toUpperCase();
		}
	}

	public static String filenameShorten(String input)
	{
		String valid = input.replaceAll("[.\"/\\\\ \\[\\]:;=,]", "");
		String trim = valid.replaceFirst("[.]*^", "");
		int pos = trim.lastIndexOf(".");
		String suffix, prefix;
		if (pos == -1) {
			suffix = "   ";
			prefix = trim;
		} else {
			suffix = trim.substring(pos + 1) + "   ";
			suffix = suffix.substring(0, 3);
			prefix = trim.substring(0, pos);
		}

		prefix += "      ";
		prefix = prefix.substring(0, 6);

		// future: simple prefix addition here needs to be more sensible.
		prefix += "~1";

		return prefix.toUpperCase() + "." + suffix.toUpperCase();
	}

	public void defaultReplyFlags(SMBHeader h)
	{
		h.flags &= SMBHeader.FLAGS_CASE_INSENSITIVE;
		h.flags |= SMBHeader.FLAGS_REPLY;
		h.flags2 &= SMBHeader.FLAGS2_LONG_NAMES | SMBHeader.FLAGS2_UNICODE;
		h.flags2 |= SMBHeader.FLAGS2_NT_STATUS;
	}

	public void send(SMBBuffer buffer) throws IOException
	{
		CommUtils.writeFully(client, buffer.preparePacket());
	}

	public void sendError(SMBHeader h, SMBBuffer acc, int status) throws IOException
	{
		h.status = status;
		defaultReplyFlags(h);

		acc.emptyParamBlock();
		acc.emptyDataBlock();
		acc.putHeader(h);
		acc.flip();

		send(acc);
	}

	public void sendSuccess(SMBHeader h, SMBBuffer acc) throws IOException
	{
		h.status = NTStatus.SUCCESS;
		defaultReplyFlags(h);

		acc.putHeader(h);
		acc.flip();

		send(acc);
	}

	/**
	 * Does the basic command or the next command in a ANDX chain.
	 * 
	 * @param h
	 * @param command
	 * @param input
	 * @param output
	 * @throws SMBException
	 */
	public void doCommand(SMBHeader h, int command, SMBBuffer message) throws IOException
	{
		SMBBuffer acc = SMBBuffer.allocatePacket(maxBufferSize);
		// Remember the position so we can undo any changes if an exception occurs
		int fix = acc.position();

		/* Locate the command handler */
		SMBCommand handler = commands[command];
		_logger.trace("Handling command " + command);
		if (handler == null) {
			sendError(h, acc, NTStatus.NOT_IMPLEMENTED);
			return;
		}

		try {
			int param_words = message.get() & 0xff;
			SMBBuffer params = message.getBuffer(param_words * 2);

			int data_bytes = message.getShort() & 0xffff;
			SMBBuffer data = message.getBuffer(data_bytes);

			/* Execute the command */
			handler.execute(this, h, params, data, message, acc);
		} catch (SMBException e) {
			acc.position(fix);
			sendError(h, acc, e.getStatus());
			return;
		} catch (IndexOutOfBoundsException e) {
			acc.position(fix);
			_logger.debug("Buffer out of bounds", e);
			sendError(h, acc, NTStatus.INVALID_SMB);
		} catch (BufferUnderflowException e) {
			acc.position(fix);
			_logger.debug("Buffer underflow", e);
			sendError(h, acc, NTStatus.INVALID_SMB);
		} catch (BufferOverflowException e) {
			acc.position(fix);
			_logger.debug("Buffer overflow", e);
			// future: this might be BUFFER_TOO_SMALL actually
			sendError(h, acc, NTStatus.BUFFER_OVERFLOW);
		}
	}

	public void doAndX(SMBHeader h, SMBAndX chain, SMBBuffer message, SMBBuffer acc) throws IOException
	{
		int command = chain.getCommand();
		int offset = chain.getOffset();

		if (command == 0xff) {
			// End of chain
			sendSuccess(h, acc);
			return;
		}

		/* Locate the command handler */
		SMBCommand handler = commands[command];
		_logger.trace("Handling command " + command);
		if (handler == null) {
			sendError(h, acc, NTStatus.NOT_IMPLEMENTED);
			return;
		}

		// Remember the position so we can undo any changes if an exception occurs
		int fix = acc.position();

		try {
			message.position(offset);

			int param_words = message.get() & 0xff;
			SMBBuffer params = message.getBuffer(param_words * 2);

			int data_bytes = message.getShort() & 0xffff;
			SMBBuffer data = message.getBuffer(data_bytes);

			/* Execute the command */
			handler.execute(this, h, params, data, message, acc);
		} catch (SMBException e) {
			acc.position(fix);
			sendError(h, acc, e.getStatus());
		} catch (IndexOutOfBoundsException e) {
			acc.position(fix);
			_logger.debug("Buffer out of bounds", e);
			sendError(h, acc, NTStatus.INVALID_SMB);
		} catch (BufferUnderflowException e) {
			acc.position(fix);
			_logger.debug("Buffer underflow", e);
			sendError(h, acc, NTStatus.INVALID_SMB);
		} catch (BufferOverflowException e) {
			acc.position(fix);
			_logger.debug("Buffer overflow", e);
			// future: this might be BUFFER_TOO_SMALL actually
			sendError(h, acc, NTStatus.BUFFER_OVERFLOW);
		}
	}

	public boolean doPacket() throws IOException
	{
		/* First the packet length */
		ByteBuffer netbios = ByteBuffer.allocate(4);
		CommUtils.readFully(client, netbios);
		netbios.flip();
		if (netbios.get() != 0)
			return true;

		int high = netbios.get();
		int mid = netbios.get();
		int low = netbios.get();

		int length = ((high & 0xff) << 16) | ((mid & 0xff) << 8) | (low & 0xff);
		if (length > 0x1ffff) {
			/* Server SHOULD disconnect the connection */
			client.close();
			return false;
		}

		_logger.trace("Receiving packet of size " + length);

		/* Now the actual SMB packet */
		ByteBuffer packet = ByteBuffer.allocate(length);
		CommUtils.readFully(client, packet);
		packet.flip();

		/* Convert into an easy to parse form */
		SMBBuffer buffer = SMBBuffer.wrap(packet);

		/* Now read the header */
		SMBHeader header;
		try {
			header = SMBHeader.decode(buffer);
		} catch (SMBException e) {
			return false;
		}

		if (_logger.isDebugEnabled())
			_logger.debug("got smb request with header: " + header);

		doCommand(header, header.command, buffer);

		return true;
	}

	public void run()
	{
		try {
			while (true) {
				boolean ok = doPacket();
				if (!ok)
					break;
			}
		} catch (IOException e) {
			_logger.error("io exception in run()", e);
		} finally {
			try {
				_logger.info("SMB connection closed");
				client.close();
			} catch (IOException e) {
				/* Nothing to do here, as the client is effectively gone */
			}
		}
	}

	public void setCurrentTransaction(SMBTransactionInfo pending)
	{
		transPending = pending;
	}

	public SMBTransactionInfo getCurrentTransaction()
	{
		return transPending;
	}

	public void doTrans2(SMBHeader h, SMBTransactionInfo trans, SMBBuffer acc) throws IOException, SMBException
	{
		int cmd = trans.getSetup().getUShort();
		SMBTrans2Command handler = cmdTrans2[cmd];
		if (handler == null) {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}

		handler.execute(this, h, trans, acc);
	}

	public void setMaxBufferSize(int size)
	{
		this.maxBufferSize = size;
	}

	public SMBDialect getDialect()
	{
		return dialect;
	}

	public void setDialect(SMBDialect dialect)
	{
		this.dialect = dialect;
	}

	public void stop()
	{
		try {
			this.client.close();
		} catch (IOException e) {

		}
	}
}
