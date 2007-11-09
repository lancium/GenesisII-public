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
import java.security.GeneralSecurityException;
import java.util.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.context.ContextType;

public interface ICallingContext
{
	// Properties are intended to be serialized within outgoing 
	// messages
	
	/**
	 * Gets a multi-value property.  Null if does not exist, otherwise 
	 * a non-empty ArrayList
	 */
	public ArrayList<Serializable> getProperty(String name);

	/**
	 * Sets a multi-value property.  Overwrites any prevous properties
	 * at this level (Overloads, but does not overwrite a same-named 
	 * parent property.) 
	 */
	public void setProperty(String name, ArrayList<Serializable> values);
	
	/**
	 * Removes a multi-value property from all levels (including parent). 
	 */
	public void removeProperty(String name);
	
	/**
	 * Gets a single-value property.  If the name refers to a mulit-value 
	 * property, the first value is returened.  Returns null if does not 
	 * exist
	 */
	public Serializable getSingleValueProperty(String name);

	/**
	 * Sets a single-value property.  Overwrites any prevous properties
	 * at this level (Overloads, but does not overwrite a same-named 
	 * parent property.) 
	 */
	public void setSingleValueProperty(String name, Serializable value);

	public void setActiveKeyAndCertMaterial(KeyAndCertMaterial clientKeyMaterial) throws GeneralSecurityException;
	public KeyAndCertMaterial getActiveKeyAndCertMaterial() throws GeneralSecurityException;
	
	
	public Serializable getTransientProperty(String name);
	public void setTransientProperty(String name, Serializable value);
	public void removeTransientProperty(String name);
	
	public RNSPath getCurrentPath();
	public void setCurrentPath(RNSPath newPath);
	
	public ContextType getSerialized() throws IOException ;
	
	public ICallingContext deriveNewContext();
	public ICallingContext deriveNewContext(ContextType serializedInformation) throws IOException;
	
	public void serializeTransientProperties(ObjectOutput out) throws IOException;
	public void deserializeTransientProperties(ObjectInput in) throws IOException;
	
}
