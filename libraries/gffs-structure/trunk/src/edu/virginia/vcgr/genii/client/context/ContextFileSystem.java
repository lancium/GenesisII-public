/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.client.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.io.RAFInputStream;
import edu.virginia.vcgr.genii.client.io.RAFOutputStream;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

/**
 * provides support for storing a calling context to a disk file and for regenerating the context from disk.
 */
public class ContextFileSystem
{
	static private final int _DEFAULT_CACHE_SIZE = 128;

	static private Log _logger = LogFactory.getLog(ContextFileSystem.class);

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

	static private FileContextPair[] _cache = new FileContextPair[_DEFAULT_CACHE_SIZE];

	/**
	 * this is the common genesis II context loader, which stores the general parts of the context separately from the transient objects.
	 */
	static public ICallingContext load(File filename, File transientFilename) throws FileNotFoundException, IOException
	{
		boolean myResponsibility = false;
		FileContextPair pair;
		int hashValue = filename.hashCode();
		if (hashValue < 0)
			hashValue = -1 * hashValue;
		hashValue %= _DEFAULT_CACHE_SIZE;

		synchronized (_cache) {
			pair = _cache[hashValue];
			if (pair == null || !pair.filename.equals(filename)) {
				myResponsibility = true;
				pair = new FileContextPair(filename, transientFilename);
				_cache[hashValue] = pair;

				if (transientFilename == null) {
					if (_logger.isDebugEnabled())
						_logger.debug("This process is now unable to store current calling context credentials for the session statefile \""
							+ filename + "\".");
				}
			}
		}

		synchronized (pair) {
			if (myResponsibility) {
				FileLock fl = null;
				try {
					if (_logger.isTraceEnabled())
						_logger.debug("Actively loading current calling context credentials to session state from files \"" + filename
							+ "\", \"" + transientFilename + "\"");
					fl = FileLock.lockFile(filename);
					pair.context = unlockedLoadContext(filename);
					unlockedLoadTransient(transientFilename, pair.context);
				} finally {
					StreamUtils.close(fl);
					pair.updating = false;
					pair.notifyAll();
				}
			} else {
				while (pair.updating) {
					try {
						pair.wait();
					} catch (InterruptedException ie) {
						throw new IOException("Thread interrupted trying to load context.");
					}
				}
				if (((pair.filename == null) && (filename != null)) || ((pair.filename != null) && (filename == null))
					|| ((pair.transientFilename != null) && (transientFilename == null))
					|| ((pair.transientFilename == null) && (transientFilename != null)) || (!pair.filename.equals(filename))
					|| (!pair.transientFilename.equals(transientFilename))) {
					_logger.warn("Incorrectly loaded current calling context " + "credentials from unexpected source state.  "
						+ "Loaded from: (" + pair.filename + ", " + pair.transientFilename + "), expected: (" + filename + ", "
						+ transientFilename + ").  Please contact VCGR with this error message " + "at genesisII@virginia.edu");
				}
			}
			return pair.context;
		}
	}

	/**
	 * stores the calling context from "context" into two places: the main context storage goes into "contextFilename" and the transients go
	 * into "transientFilename".
	 */
	static public void store(File contextFilename, File transientFilename, ICallingContext context) throws FileNotFoundException, IOException
	{
		FileContextPair pair;
		int hashValue = contextFilename.hashCode();
		if (hashValue < 0)
			hashValue = -1 * hashValue;
		hashValue %= _DEFAULT_CACHE_SIZE;

		synchronized (_cache) {
			pair = _cache[hashValue];
			if (pair == null || !pair.filename.equals(contextFilename)) {
				pair = new FileContextPair(contextFilename, transientFilename);
				_cache[hashValue] = pair;

				if (transientFilename == null) {
					if (_logger.isTraceEnabled()) {
						_logger.debug("This process is now *unable* to store current calling context credentials for the session statefile \""
							+ contextFilename + "\".");
					}
				}

			}
		}

		synchronized (pair) {
			FileLock fl = null;
			try {
				fl = FileLock.lockFile(contextFilename);
				unlockedStoreContext(contextFilename, context);
				unlockedStoreTransient(transientFilename, context);
			} finally {
				StreamUtils.close(fl);
				pair.context = context;
				pair.updating = false;
				pair.notifyAll();
			}
		}
	}

	/**
	 * specialized calling context loader for unicore usage. this takes one file that has all the information available in it, and we
	 * deserialize the objects in the file using the current object deserialization methods.
	 */
	static public ICallingContext load(File combinedFile) throws FileNotFoundException, IOException
	{
		InputStream fin = null;
		FileLock fl = null;

		try {
			fl = FileLock.lockFile(combinedFile);
			fin = new FileInputStream(combinedFile);
			ContextType ct = (ContextType) ObjectDeserializer.deserialize(new InputSource(fin), ContextType.class);
			StreamUtils.close(fin);
			FileLock.unlockFile(fl);

			ICallingContext callContext = CallingContextUtilities.setupCallingContextAfterCombinedExtraction(new CallingContextImpl(ct));
			KeyAndCertMaterial keyAndCert = UnicoreContextWorkAround.loadUnicoreContextDelegateeInformation();
			callContext.setActiveKeyAndCertMaterial(keyAndCert);

			/*
			 * Retrieve and authenticate other accumulated message-level credentials (e.g., GII delegated assertions, etc.)
			 */
			@SuppressWarnings("unchecked")
			ArrayList<NuCredential> bearerCredentials =
				(ArrayList<NuCredential>) callContext.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);

			/* Finally add all of our callerIds to the calling-context's outgoing credentials. */
			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
			for (NuCredential cred : bearerCredentials) {
				transientCredentials.add(cred);
			}
			return callContext;
		} catch (GeneralSecurityException e) {
			String msg = "Unable to generate key and cert material!";
			_logger.error(msg);
			throw new IOException(msg, e);
		} finally {
			// unlock in case we bombed out due to exception.
			StreamUtils.close(fin);
			FileLock.unlockFile(fl);
		}
	}

	static private ICallingContext unlockedLoadContext(File filename) throws FileNotFoundException, IOException
	{
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "rw");
			InputStream in = new RAFInputStream(raf);
			ICallingContext toReturn = ContextStreamUtils.load(in);
			return toReturn;
		} finally {
			StreamUtils.close(raf);
		}
	}

	/**
	 * stores the calling context to the "filename". does not lock! external caller must arrange file locking.
	 */
	static private void unlockedStoreContext(File filename, ICallingContext context) throws FileNotFoundException, IOException
	{
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "rw");
			raf.setLength(0);
			raf.seek(0);
			OutputStream out = new RAFOutputStream(raf);
			Writer writer = new OutputStreamWriter(out);
			ContextStreamUtils.store(writer, context);
			writer.flush();
		} finally {
			StreamUtils.close(raf);
		}
	}

	static private void unlockedLoadTransient(File filename, ICallingContext context) throws FileNotFoundException, IOException
	{
		RandomAccessFile raf = null;
		if ((filename == null) || !filename.exists())
			return;
		try {
			raf = new RandomAccessFile(filename, "rw");
			ObjectInputStream in = new ObjectInputStream(new RAFInputStream(raf));
			context.deserializeTransientProperties(in);
			if ((TransientCredentials.getTransientCredentials(context).isEmpty())) {
				if (_logger.isTraceEnabled()) {
					_logger.debug("Loaded empty calling context credentials from session statefile " + filename);
				}
			}
		} finally {
			StreamUtils.close(raf);
		}
	}

	/**
	 * file locking must be arranged externally to this method. this just takes care of the raw storage of the transients.
	 */
	static private void unlockedStoreTransient(File filename, ICallingContext context) throws FileNotFoundException, IOException
	{
		if (filename == null) {
			return;
		}
		RandomAccessFile raf = null;
		try {
			if (_logger.isTraceEnabled()) {
				_logger.debug("Storing credentials to session state in file '" + filename + "'");
			}
			raf = new RandomAccessFile(filename, "rw");
			raf.setLength(0);
			ObjectOutputStream out = new ObjectOutputStream(new RAFOutputStream(raf));
			context.serializeTransientProperties(out);
			out.flush();
		} finally {
			StreamUtils.close(raf);
		}
	}
}
