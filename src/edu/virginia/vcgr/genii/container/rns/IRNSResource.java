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
package edu.virginia.vcgr.genii.container.rns;

import java.util.Collection;

import org.ggf.rns.RNSEntryExistsFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IRNSResource extends IResource
{
	public void addEntry(InternalEntry entry) 
		throws ResourceException, RNSEntryExistsFaultType;
	public Collection<String> listEntries() throws ResourceException;
	public Collection<InternalEntry> retrieveEntries(String regex)
		throws ResourceException;
	public Collection<String> removeEntries(String regex)
		throws ResourceException;
}