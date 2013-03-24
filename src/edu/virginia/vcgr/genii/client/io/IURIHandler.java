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
package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.morgan.util.io.DataTransferStatistics;

import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public interface IURIHandler
{
	public String[] getHandledProtocols();

	/**
	 * Determines whether or not this protocol allows for reading. This does not guarantee that the
	 * URI provided can be read from, only that the protocol in general allows it.
	 * 
	 * @param uriScheme
	 *            The uri scheme to test.
	 * @return True if the scheme given can be read from.
	 */
	public boolean canRead(String uriScheme);

	/**
	 * Determines whether or not this protocol allows for writing. This does not guarantee that the
	 * URI provided can be written to, only that the protocol in general allows it.
	 * 
	 * @param uriScheme
	 *            The uri scheme to test.
	 * @return True if the scheme given can be written to.
	 */
	public boolean canWrite(String uriScheme);

	public DataTransferStatistics get(URI source, File target, UsernamePasswordIdentity credential) throws IOException;

	public DataTransferStatistics put(File source, URI target, UsernamePasswordIdentity credential) throws IOException;
}
