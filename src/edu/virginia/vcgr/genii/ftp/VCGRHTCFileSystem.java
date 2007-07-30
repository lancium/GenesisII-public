/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.ftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class VCGRHTCFileSystem extends UnauthenticatedVCGRFileSystem
{
	static private Log _logger = LogFactory.getLog(VCGRHTCFileSystem.class);
	
	public VCGRHTCFileSystem(ICallingContext context)
	{
		super(context);
	}
	
	public String pwd() throws FTPException
	{
		return _context.getCurrentPath().pwd();
	}

	public long size(String entry) throws FTPException
	{
		try
		{
			RNSPath newPath = _context.getCurrentPath().lookup(entry,
				RNSPathQueryFlags.MUST_EXIST);
			TypeInformation typeInfo = new TypeInformation(
				newPath.getEndpoint());
			if (!typeInfo.isByteIO())
				throw new FTPException(451, "Entry does not represent a file.");
			return Long.parseLong(typeInfo.describeByteIO());
		}
		catch (NumberFormatException nfe)
		{
			throw new FTPException(451, "Couldn't retrieve the file size.");
		}
		catch (RNSException re)
		{
			throw new PathDoesNotExistException(entry);
		}
	}

	public void cwd(String path) throws FTPException
	{
		try
		{
			RNSPath newPath = _context.getCurrentPath().lookup(path,
				RNSPathQueryFlags.MUST_EXIST);
			_context.setCurrentPath(newPath);
		}
		catch (RNSException re)
		{
			throw new PathDoesNotExistException(path);
		}
	}

	public ListEntry[] list() throws FTPException
	{
		FilePermissions rwx = new FilePermissions(0x7, 0x7, 0x7);
		
		try
		{
			RNSPath []paths = _context.getCurrentPath().list(".*", 
				RNSPathQueryFlags.DONT_CARE);
			if (paths.length == 1 && !paths[0].exists())
				paths = new RNSPath[0];
			ListEntry []ret = new ListEntry[paths.length];
			for (int lcv = 0; lcv < paths.length; lcv++)
			{
				if (paths[lcv].isDirectory())
				{
					ret[lcv] = new ListEntry(paths[lcv].getName(),
						new Date(), 0, "mmm2a", "vcgr", rwx, 1, true);
				} else if (paths[lcv].isFile())
				{
					String typeDesc = new TypeInformation(
						paths[lcv].getEndpoint()).getTypeDescription();
					
					long size = 0;
					try
					{
						size = Long.parseLong(typeDesc);
					}
					catch (NumberFormatException nfe)
					{
					}
					
					ret[lcv] = new ListEntry(paths[lcv].getName(),
						new Date(), size, "mmm2a", "vcgr", rwx, 1, false); 
				} else
				{
					RedirectFile rd = new RedirectFile(
						paths[lcv].getEndpoint());
					ret[lcv] = new ListEntry(
						paths[lcv].getName() + ".html",
						new Date(), rd.getSize(), "mmm2a", "vcgr",
						new FilePermissions(0x5, 0x5, 0x5), 1, false);
				}
			}
			
			return ret;
		}
		catch (RNSException rne)
		{
			_logger.error(rne, rne);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	public String mkdir(String newDir) throws FTPException
	{
		try
		{
			RNSPath path = _context.getCurrentPath();
			RNSPath newPath = path.lookup(newDir, RNSPathQueryFlags.MUST_NOT_EXIST);
			newPath.mkdir();
			return newPath.pwd();
		}
		catch (RNSException rne)
		{
			_logger.error(rne, rne);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	public void removeDirectory(String directory) throws FTPException
	{
		delete(directory);
	}

	public IFTPFileSystem authenticate(String user, String password)
			throws FTPException
	{
		throw new InternalException(
			"This connection is already authenticated.");
	}

	public void delete(String entry) throws FTPException
	{
		try
		{
			RNSPath path = _context.getCurrentPath();
			path = path.lookup(entry, RNSPathQueryFlags.MUST_EXIST);
			path.delete();
		}
		catch (RNSException rne)
		{
			_logger.error(rne, rne);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	public void rename(String oldEntry, String newEntry) throws FTPException
	{
		try
		{
			RNSPath path = _context.getCurrentPath();
			RNSPath oldPath = path.lookup(oldEntry, RNSPathQueryFlags.MUST_EXIST);
			RNSPath newPath = path.lookup(newEntry, RNSPathQueryFlags.MUST_NOT_EXIST);
			newPath.link(oldPath.getEndpoint());
			oldPath.unlink();
		}
		catch (RNSPathAlreadyExistsException e)
		{
			throw new PathAlreadyExistsException(newEntry);
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new PathDoesNotExistException(oldEntry);
		}
		catch (RNSException rne)
		{
			_logger.error(rne, rne);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	public void retrieve(String entry, OutputStream out) throws FTPException
	{
		try
		{
			byte []data = 
				new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];
			int read;
			RNSPath path = _context.getCurrentPath().lookup(
				entry, RNSPathQueryFlags.DONT_CARE);
			
			if (!path.exists())
			{
				if (entry.endsWith(".html"))
				{
					path = _context.getCurrentPath().lookup(
						entry.substring(0, entry.length() - 5), 
						RNSPathQueryFlags.MUST_EXIST);
				}
			}
			
			InputStream in = null;
			try
			{
				if (path.isFile())
					in = new ByteIOInputStream(path);
				else
					in = (new RedirectFile(
						path.getEndpoint())).getStream();
				
				while ( (read = in.read(data)) >= 0)
				{
					out.write(data, 0, read);
				}
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new PathDoesNotExistException(entry);
		}
		catch (RNSMultiLookupResultException e)
		{
			throw new PathDoesNotExistException(entry);
		}
		catch (Exception e)
		{
			_logger.error(e, e);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	public void store(String entry, InputStream in) throws FTPException
	{
		try
		{
			byte []data = 
				new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];
			int read;
			RNSPath path = _context.getCurrentPath().lookup(
				entry, RNSPathQueryFlags.DONT_CARE);
			
			if (path.exists())
			{
				if (!path.isFile())
					throw new FTPException("451 Path is not a file.");
			} else
			{
				path.createFile();
			}
			
			OutputStream out = null;
			try
			{
				out = new ByteIOOutputStream(path.getEndpoint());
				while ( (read = in.read(data, 0, data.length)) >= 0)
				{
					out.write(data, 0, read);
				}
				
				out.flush();
			}
			finally
			{
				StreamUtils.close(out);
			}
		}
		catch (Throwable t)
		{
			_logger.error(t, t);
			throw new FTPException("451 Unknown internal error.");
		}
	}

	public boolean exists(String entry) throws FTPException
	{
		try
		{
			_context.getCurrentPath().lookup(
				entry, RNSPathQueryFlags.MUST_EXIST);
			
			return true;
		}
		catch (Throwable t)
		{
			return false;
		}
	}
}
