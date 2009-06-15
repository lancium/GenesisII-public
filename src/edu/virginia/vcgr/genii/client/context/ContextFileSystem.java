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
package edu.virginia.vcgr.genii.client.context;

import java.io.*;
import java.nio.channels.FileLock;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.RAFInputStream;
import edu.virginia.vcgr.genii.client.io.RAFOutputStream;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;

public class ContextFileSystem
{
	static private final int _DEFAULT_CACHE_SIZE = 128;
	
	static private class FileContextPair
	{
		public boolean updating;
		public File filename;
		public File transientFilename;
		public ICallingContext context;
		
		public FileContextPair(File _filename, File _transientFilename)
		{
			filename = _filename;
			transientFilename = _transientFilename;
			context = null;
			updating = true;
		}

	}
	
	static private FileContextPair []_cache = new FileContextPair[_DEFAULT_CACHE_SIZE];
	
	static public ICallingContext load(File filename, File transientFilename) 
		throws FileNotFoundException, IOException
	{
		boolean myResponsibility = false;
		FileContextPair pair;
		int hashValue = filename.hashCode();
		if (hashValue < 0)
			hashValue = -1 * hashValue;
		hashValue %= _DEFAULT_CACHE_SIZE;
		
		synchronized(_cache)
		{
			pair = _cache[hashValue];
			if (pair == null || !pair.filename.equals(filename))
			{
				myResponsibility = true;
				pair = new FileContextPair(filename, transientFilename);
				_cache[hashValue] = pair;
				
				if (transientFilename == null) {
					TransientCredentials._logger.debug(
						"This process is now unable to store current calling " +
						"context credentials for the session statefile \"" +
						filename + "\"."); 
				}
			}
		}

		synchronized(pair)
		{
			if (myResponsibility)
			{
				try
				{
					TransientCredentials._logger.debug(
						"Actively loading current calling context " +
						"credentials to session state from files \"" + 
						filename + "\", \"" + transientFilename + "\"");
					pair.context = loadContext(filename);
					loadTransient(transientFilename, pair.context);
				}
				finally
				{
					pair.updating = false;
					pair.notifyAll();
				}
			} else
			{
				while (pair.updating)
				{
					try
					{
						pair.wait();
					}
					catch (InterruptedException ie)
					{
						throw new IOException(
							"Thread interrupted trying to load context.");
					}
				}
				if (((pair.filename == null) && (filename != null)) ||
					((pair.filename != null) && (filename == null)) ||
					((pair.transientFilename != null) && (transientFilename == null)) ||
					((pair.transientFilename == null) && (transientFilename != null)) ||
					(!pair.filename.equals(filename)) ||
					(!pair.transientFilename.equals(transientFilename))) {

					TransientCredentials._logger.warn(
						"Incorrectly loaded current calling context " +
						"credentials from unexpected source state.  " +
						"Loaded from: (" + 
						pair.filename + ", " + pair.transientFilename + 
						"), expected: (" + 
						filename + ", " + transientFilename + 
						").  Please contact VCGR with this error message " +
						"at genesisII@virginia.edu");
				}

			}
			
			return pair.context;
		}
	}
	
	static public void store(File filename, File transientFilename, ICallingContext context)
		throws FileNotFoundException, IOException
	{
		FileContextPair pair;
		int hashValue = filename.hashCode();
		if (hashValue < 0)
			hashValue = -1 * hashValue;
		hashValue %= _DEFAULT_CACHE_SIZE;
		
		synchronized(_cache)
		{
			pair = _cache[hashValue];
			if (pair == null || !pair.filename.equals(filename))
			{
				pair = new FileContextPair(filename, transientFilename);
				_cache[hashValue] = pair;
				
				if (transientFilename == null) {
					TransientCredentials._logger.debug(
						"This process is now unable to store current " +
						"calling context credentials for the session " +
						"statefile \"" + filename + "\"."); 
				}
				
			}
		}

		synchronized(pair)
		{
			try
			{
				storeContext(filename, context);
				if (transientFilename != null) { 
					TransientCredentials._logger.debug(
							"Storing current calling context credentials to " +
							"session state in files " + 
							pair.filename + ", " + pair.transientFilename);

					storeTransient(transientFilename, context);
				}

			
			}
			finally
			{
				pair.context = context;
				pair.updating = false;
				pair.notifyAll();
			}
		}
	}
	
	static private ICallingContext loadContext(File filename)
		throws FileNotFoundException, IOException
	{
		FileLock lock = null;
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			lock = raf.getChannel().lock();
			InputStream in = new RAFInputStream(raf);
			return ContextStreamUtils.load(in);
		}
		finally
		{
			if (lock != null)
				try { lock.release(); } catch (Throwable t) {}
			if (raf != null)
				StreamUtils.close(raf);
		}
	}
	
	static private void storeContext(File filename, ICallingContext context)
		throws FileNotFoundException, IOException
	{
		FileLock lock = null;
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			lock = raf.getChannel().lock();
			raf.setLength(0);
			raf.seek(0);
			OutputStream out = new RAFOutputStream(raf);
			Writer writer = new OutputStreamWriter(out);
			ContextStreamUtils.store(writer, context);
			writer.flush();
		}
		finally
		{
			if (lock != null)
				try { lock.release(); } catch (Throwable t) {}
			if (raf != null)
				StreamUtils.close(raf);
		}
	}
	
	
	static private void loadTransient(File filename, ICallingContext context)
		throws FileNotFoundException, IOException
	{
		FileLock lock = null;
		RandomAccessFile raf = null;
		
		if ((filename == null) || !filename.exists())
			return;
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			lock = raf.getChannel().lock();
			ObjectInputStream in = new ObjectInputStream(new RAFInputStream(raf));
			context.deserializeTransientProperties(in);
			
			if ((TransientCredentials.getTransientCredentials(context)._credentials.size() == 0)) {
				TransientCredentials._logger.debug(
						"Loaded empty calling context credentials from session statefile " + 
						filename);
			}
		}
		finally
		{
			if (lock != null)
				try { lock.release(); } catch (Throwable t) {}
			if (raf != null)
				StreamUtils.close(raf);
		}
	}	
	
	static private void storeTransient(File filename, ICallingContext context)
	throws FileNotFoundException, IOException
	{
		FileLock lock = null;
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			lock = raf.getChannel().lock();
			raf.setLength(0);
			ObjectOutputStream out = new ObjectOutputStream(new RAFOutputStream(raf));
			context.serializeTransientProperties(out);
			out.flush();
		}
		finally
		{
			if (lock != null)
				try { lock.release(); } catch (Throwable t) {}
			if (raf != null)
				StreamUtils.close(raf);
		}
	}	
}


