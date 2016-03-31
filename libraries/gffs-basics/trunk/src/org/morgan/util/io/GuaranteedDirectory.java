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
package org.morgan.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

/**
 * A utility class which acts like a file, but upon successful creation guarantees that the names path is an existing directory.
 * 
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class GuaranteedDirectory extends File
{
	static final long serialVersionUID = 0;
	static private Log _logger = LogFactory.getLog(GuaranteedDirectory.class);

	// true if only the owner should have write and execute permissions.
	private boolean _ownerOnly = false;

	private void enforceGuarantee() throws IOException
	{
		if (exists()) {
			if (!isDirectory())
				throw new IOException("Path \"" + getAbsolutePath() + "\" is not a directory.");
		} else {
			if (!mkdirs())
				throw new IOException("Unable to create directory \"" + getAbsolutePath() + "\".");
			if (_ownerOnly) {
				// if we were asked to give only the owner permission, we do it here.
				if (!OperatingSystemType.isWindows()) {
					// we think we're good to try this; this doesn't seem to be a windows variant.
					Runtime r = Runtime.getRuntime();

					/*
					 * for now, since we need to keep supporting java 6, we're going with the ugly but working approach.
					 */
					/*
					 * hmmm: note, we no longer support java 6. the below code could be updated to use the PosixFilePermissions.
					 */

					String[] cmds = { "chmod", "u+rwx,g-rwx,o-rwx", getAbsolutePath() };
					Process p = r.exec(cmds);
					int retval = -1;
					try {
						retval = p.waitFor();
					} catch (InterruptedException e) {
						_logger.error("interrupted while waiting for chmod process.");
					}
					if (retval != 0) {
						_logger.warn("may have failed to set permissions for owner only.");
					}

				}

			}
		}
	}

	public GuaranteedDirectory(String path) throws IOException
	{
		super(path);
		enforceGuarantee();
	}

	public GuaranteedDirectory(String path, boolean ownerOnly) throws IOException
	{
		super(path);
		_ownerOnly = ownerOnly;
		enforceGuarantee();
	}

	public GuaranteedDirectory(String parent, String child) throws IOException
	{
		super(parent, child);
		enforceGuarantee();
	}

	public GuaranteedDirectory(File dir) throws IOException
	{
		super(dir.getAbsolutePath());
		enforceGuarantee();
	}

	public GuaranteedDirectory(File parent, String child) throws IOException
	{
		super(parent, child);
		enforceGuarantee();
	}

	public GuaranteedDirectory(URI path) throws IOException
	{
		super(path);
		enforceGuarantee();
	}
}
