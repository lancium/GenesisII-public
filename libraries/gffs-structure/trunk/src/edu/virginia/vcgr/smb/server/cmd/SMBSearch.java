package edu.virginia.vcgr.smb.server.cmd;
import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBTime;
import edu.virginia.vcgr.smb.server.SMBTree;


public class SMBSearch implements SMBCommand {
	private boolean closeable, unique, close;
	public SMBSearch(boolean closeable, boolean unique, boolean close) {
		this.closeable = closeable;
		this.unique = unique;
		this.close = close;
	}
	
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws SMBException, IOException {
		int maxcount = params.getUShort();
		int attrs = params.getUShort();
		
		String reqPath = data.getSMBString(h.isUnicode());
		SMBBuffer resumeKey = data.getVarBlock();
		
		if ((attrs & SMBFileAttributes.VOLUME) != 0) {
			acc.startParameterBlock();
			acc.putShort((short)0x1);
			acc.finishParameterBlock();
			
			acc.startDataBlock();
			int startBuffer = acc.startDataBuffer();
			acc.put("                     ".getBytes());
			acc.put((byte)SMBFileAttributes.VOLUME);
			acc.putShort((short)0);
			acc.putShort((short)0);
			acc.putInt(0);
			acc.putString("GENESIS2.GRD", false);
			acc.finishDataBuffer(startBuffer);
			acc.finishDataBlock();
			
			c.sendSuccess(h, acc);
			
			return;
		}

		attrs |= SMBFileAttributes.READONLY;
		attrs |= SMBFileAttributes.ARCHIVE;
		
		SMBTree tree = c.verifyTID(h.tid);
		
		int consumer = 0;
		int SID;
		
		SMBSearchState search;
		
		int keyLength = resumeKey.remaining();
		if (keyLength == 0) {
			// New search
			search = tree.search(reqPath, new SMBFileAttributes(attrs), h.isCaseSensitive());
			SID = tree.allocateSID(search);
		} else if (keyLength == 0x15) {
			byte [] prefixBuf = new byte[8];
			byte [] suffixBuf = new byte[3];
			
			resumeKey.get();
			resumeKey.get(prefixBuf);
			resumeKey.get(suffixBuf);
			resumeKey.get();
			resumeKey.getShort();
			SID = resumeKey.getUShort();
			consumer = resumeKey.getInt();
			
			String prefix = new String(prefixBuf, "US-ASCII");
			String suffix = new String(suffixBuf, "US-ASCII");
			
			prefix = prefix.trim();
			suffix = suffix.trim();
			
			String cont = prefix;
			if (!suffix.isEmpty())
				cont += "." + suffix;
			
			search = tree.verifySID(SID);
			search.reset(cont);
		} else {
			throw new SMBException(NTStatus.INVALID_SMB);
		}
		
		acc.startParameterBlock();
		int countOffset = acc.skip(2);
		acc.finishParameterBlock();
		
		acc.startDataBlock();
		int startBuffer = acc.startDataBuffer();
		
		// List the files
		int count = 0;
		while (count < maxcount) {
			if (!search.hasNext())
				break;
			
			SMBSearchState.Entry cur = search.next();
			String name = cur.getName();
			RNSPath path = cur.getPath();
			String shortname = SMBConnection.filenameShortenLossless(name);
			// The specification says to not return entries if they don't have an 8.3 filename
			if (shortname == null)
				continue;
			
			count++;
			
			Date write = new Date();
			long size = 0;
			int type = SMBFileAttributes.SYSTEM;
			try {
				TypeInformation info = new TypeInformation(path.getEndpoint());
				type = SMBFileAttributes.fromTypeInfo(info);
				
				if (info.isByteIO()) {
					write = info.getByteIOModificationTime();
					size = info.getByteIOSize();
				}
			} catch (RNSPathDoesNotExistException e) {
				// ?
			}
			
			String prefix;
			String suffix = "   ";
			
			if (shortname.equals(".")) {
				prefix = shortname + "       ";
			} else if (shortname.equals("..")) {
				prefix = shortname + "      ";
			} else {
				String [] comp = shortname.split("[.]");
				prefix = (comp[0] + "        ").substring(0, 8);
				if (comp.length > 1)
					suffix = (comp[1] + "   ").substring(0, 3);
			}
			
			shortname = (shortname + "            ").substring(0, 12);
			
			acc.put((byte)0);
			acc.put(prefix.getBytes("US-ASCII"));
			acc.put(suffix.getBytes("US-ASCII"));
			acc.put((byte)1);
			acc.putShort((short)0);
			acc.putShort((short)SID);
			acc.putInt(consumer);
			acc.put((byte)type);//File Attributes
			SMBTime.fromDate(write).encode(acc);
			SMBDate.fromDate(write).encode(acc);
			acc.putInt((int)size);//File Size
			acc.putString(shortname, false);
		}
		
		// Any files to list
		if (close) {
			tree.releaseSID(SID);
		} else if (unique || (!search.hasNext() && !closeable)) {
			tree.releaseSID(SID);
			throw new SMBException(NTStatus.NO_MORE_FILES);
		}
		
		acc.putShort(countOffset, (short)count);
		acc.finishDataBuffer(startBuffer);
		acc.finishDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
