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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.*;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.ser.Base64;
import edu.virginia.vcgr.genii.context.ContextNameValuePairType;
import edu.virginia.vcgr.genii.context.ContextType;

public class CallingContextImpl implements ICallingContext, Serializable
{
	static final long serialVersionUID = 0L;
	
	// serialization caches and ultility functions
	static protected int SERIALIZATION_CACHE_SIZE = 64;

	static protected LRUCache<String, Serializable> incomingBase64cache = 
		new LRUCache<String, Serializable>(SERIALIZATION_CACHE_SIZE);

	protected static final String CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA = 
		"edu.virginia.vcgr.genii.client.security.client-key-material-call-context-data";
	
	static protected Serializable retrieveBase64Decoded(String encoded) throws IOException {
		synchronized (incomingBase64cache) {
			Serializable retval = incomingBase64cache.get(encoded);
			if (retval == null) {
				ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(Base64.base64ToByteArray(encoded)));

				try {
					retval = (Serializable) ois.readObject();
					
					// optimize for later comparison checking
					if (retval instanceof SignedAssertion) {
						((SignedAssertion) retval)._encodedValue = encoded;
					}
				} catch (ClassNotFoundException e) { 
					throw new IOException(e.getMessage());
				}
				
				incomingBase64cache.put(encoded, retval);
			}
			return retval;
		}
	}

	static protected LRUCache<Serializable, String> outgoingBase64cache = 
		new LRUCache<Serializable, String>(SERIALIZATION_CACHE_SIZE);
	
	static protected String retrieveBase64Encoded(Serializable obj) throws IOException {
		synchronized (outgoingBase64cache) {
			String retval = outgoingBase64cache.get(obj);
			if (retval == null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 		ObjectOutputStream oos = new ObjectOutputStream(baos);
		 		oos.writeObject(obj);
		 		oos.close();

				retval = Base64.byteArrayToBase64(baos.toByteArray());
				outgoingBase64cache.put(obj, retval);
			}
			return retval;
		}
	}

	
	protected static final String CURRENT_PATH_KEY = "__$$Current Path$$";

	// multi-value list of name-value properties
	private HashMap<String, ArrayList<Serializable>> _properties = 
		new HashMap<String, ArrayList<Serializable>>();

	private CallingContextImpl _parent = null;

	// list of name-value properties to be kept separate from the
	// outgoing message properties
	private HashMap<String, Serializable> _transientProperties = 
		new HashMap<String, Serializable>();

	public CallingContextImpl(CallingContextImpl parent) {
		_parent = parent;
	}

	public CallingContextImpl(ContextType ct) throws IOException {

		if (ct != null) {
			// load the properties from the ContextType
			ContextNameValuePairType[] pairs = ct.getProperty();
			if (pairs != null) {
				for (ContextNameValuePairType pair : ct.getProperty()) {
					String name = pair.getName();
					ArrayList<Serializable> multiValue = _properties.get(name);
					if (multiValue == null) {
						multiValue = new ArrayList<Serializable>();
						_properties.put(name, multiValue);
					}
					
					multiValue.add(retrieveBase64Decoded(pair.getValue()));
				}
			}
		}
	}

	public CallingContextImpl(RNSPath root) {
		setCurrentPath(root);
	}

	public synchronized ArrayList<Serializable> getProperty(String name) {
		
		ArrayList<Serializable> multiValue = null;
		multiValue = _properties.get(name);
		if (multiValue == null && _parent != null)
			return _parent.getProperty(name);
		return multiValue;
	}
	
	public synchronized Serializable getSingleValueProperty(String name) {
		ArrayList<Serializable> multiValue = getProperty(name);
		if (multiValue == null) {
			return null;
		}
		return multiValue.get(0);
	}

	public synchronized void setProperty(String name, ArrayList<Serializable> multiValue) {
	    if ((multiValue != null) && (multiValue.isEmpty())) {
	    	throw new IllegalArgumentException("Illegal empty multiValue, use null instead");
	    }

		_properties.put(name, multiValue);
	}

	public synchronized void setSingleValueProperty(String name, Serializable value) {
		ArrayList<Serializable> multiValue = new ArrayList<Serializable>();
		multiValue.add(value);
		setProperty(name, multiValue);
	}

	public synchronized void removeProperty(String name) {
		_properties.remove(name);
		if (_parent != null) {
			_parent.removeProperty(name);
		}
	}

	public synchronized Serializable getTransientProperty(String name) {
		Serializable obj = _transientProperties.get(name);
		if (obj == null && _parent != null)
			obj = _parent.getTransientProperty(name);

		return obj;
	}

	public synchronized void setTransientProperty(String name, Serializable value) {
		_transientProperties.put(name, value);
	}

	public synchronized void removeTransientProperty(String name) {
		_transientProperties.remove(name);
		if (_parent != null)
			_parent.removeTransientProperty(name);
	}

	public synchronized void setActiveKeyAndCertMaterial(KeyAndCertMaterial clientKeyMaterial) throws GeneralSecurityException {
		// this transient property always gets put in the top parent context
		if (_parent != null) {
			_parent.setActiveKeyAndCertMaterial(clientKeyMaterial);
			return;
		}
		
		setTransientProperty(CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA, clientKeyMaterial);
		
	}
	
	public synchronized KeyAndCertMaterial getActiveKeyAndCertMaterial() throws GeneralSecurityException {
		return (KeyAndCertMaterial)	getTransientProperty(
				CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA);
	}

	public synchronized RNSPath getCurrentPath() {
		ArrayList<Serializable> multiValue = _properties.get(CURRENT_PATH_KEY);
		if (multiValue != null) {
			return (RNSPath) multiValue.get(0);
		}

		if (_parent != null) {
			return _parent.getCurrentPath();
		}

		return null;
	}

	public synchronized void setCurrentPath(RNSPath newPath) {
		ArrayList<Serializable> multiValue = new ArrayList<Serializable>();
		multiValue.add(newPath);
		_properties.put(CURRENT_PATH_KEY, multiValue);
	}

	public ContextType getSerialized() throws IOException {
		ContextType ct = new ContextType();

		ArrayList<ContextNameValuePairType> pairs = new ArrayList<ContextNameValuePairType>();
		accumulateProperties(pairs);
		ContextNameValuePairType[] pairsArray = 
			new ContextNameValuePairType[pairs.size()];
		pairs.toArray(pairsArray);
		ct.setProperty(pairsArray);

		return ct;
	}

	public ICallingContext deriveNewContext() {
		return new CallingContextImpl(this);
	}

	public ICallingContext deriveNewContext(ContextType serializedInformation) throws IOException  {
		CallingContextImpl retval = new CallingContextImpl(this);

		// load the properties from the ContextType
		if (serializedInformation != null) {
			ContextNameValuePairType[] pairs = serializedInformation.getProperty();
			if (pairs != null) {
				for (ContextNameValuePairType pair : serializedInformation.getProperty()) {
					String name = pair.getName();
					ArrayList<Serializable> multiValue = _properties.get(name);
					if (multiValue == null) {
						multiValue = new ArrayList<Serializable>();
						_properties.put(name, multiValue);
					}
					
					multiValue.add(retrieveBase64Decoded(pair.getValue()));
				}
			}
		}
		
		return retval;
	}

	private synchronized void accumulateProperties(ArrayList<ContextNameValuePairType> pairs) throws IOException {
		if (_parent != null) {
			_parent.accumulateProperties(pairs);
		}

		for (String name : _properties.keySet()) {
			ArrayList<Serializable> values = _properties.get(name);
			for (Serializable val : values) {
				String strVal = retrieveBase64Encoded(val);
				pairs.add(new ContextNameValuePairType(name, strVal));
			}
		}
	}

	public void serializeTransientProperties(ObjectOutput out)
			throws IOException {

		out.writeObject(_transientProperties);

		if (_parent != null) {
			out.writeBoolean(true);
			_parent.serializeTransientProperties(out);
		} else {
			out.writeBoolean(false);
		}

	}

	@SuppressWarnings("unchecked")
	public void deserializeTransientProperties(ObjectInput in)
			throws IOException {
		try {
			_transientProperties.putAll((HashMap<String, Serializable>) in.readObject());

			if (in.readBoolean()) {
				// read in another set from the parent
				deserializeTransientProperties(in);
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}

	void setTransientProperties(HashMap<String, Serializable> transientProps) {
		_transientProperties = transientProps;
	}
	
	private void collapseTransient(HashMap<String, Serializable> target)
	{
		if (_parent != null)
			_parent.collapseTransient(target);
		
		for (String key : _transientProperties.keySet())
		{
			target.put(key, _transientProperties.get(key));
		}
	}	
	
	public Object writeReplace() throws ObjectStreamException {
		ByteArrayOutputStream baos = null;
		OutputStreamWriter writer = null;
		
		try {
			writer = new OutputStreamWriter(
				(baos = new ByteArrayOutputStream()) );
			ContextStreamUtils.store(writer, this);
			
			writer.flush();
			
			HashMap<String, Serializable> transientCollapse = 
				new HashMap<String, Serializable>();
			collapseTransient(transientCollapse);
			
			return new SerializedContext(baos.toByteArray(), transientCollapse);
		} catch (IOException ioe) {
			throw new NotSerializableException("CallingContextImpl");
		} finally {
			StreamUtils.close(writer);
		}
	}	

}
