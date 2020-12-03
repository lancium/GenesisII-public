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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.algorithm.encryption.Base64;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.context.ContextNameValuePairType;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class CallingContextImpl implements ICallingContext, Serializable
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(CallingContextImpl.class);

	protected static final String CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA =
		"edu.virginia.vcgr.genii.client.security.client-key-material-call-context-data";

	protected static final String CURRENT_PATH_KEY = "__$$Current Path$$";

	// multi-value list of name-value properties
	private HashMap<String, Collection<Serializable>> _properties = new HashMap<String, Collection<Serializable>>();

	private CallingContextImpl _parent = null;

	/*
	 * list of name-value properties to be kept separate from the outgoing message properties.
	 */
	private HashMap<String, Serializable> _transientProperties = new HashMap<String, Serializable>();

	public CallingContextImpl(CallingContextImpl parent)
	{
		_parent = parent;
	}

	@Override
	public ICallingContext getParent()
	{
		return _parent;
	}

	public CallingContextImpl(ContextType ct) throws IOException
	{
		if (ct != null) {
			// load the properties from the ContextType
			ContextNameValuePairType[] pairs = ct.getProperty();
			if (pairs != null) {
				for (ContextNameValuePairType pair : ct.getProperty()) {
					String name = pair.getName();
					if (_logger.isTraceEnabled()) {
						_logger.trace("adding context entry for: " + name);
					}
					Collection<Serializable> multiValue = _properties.get(name);
					if (multiValue == null) {
						multiValue = new ArrayList<Serializable>();
						_properties.put(name, multiValue);
						if (_logger.isTraceEnabled()) {
							_logger.trace("adding empty multivalue for null property called: " + name);
						}
					}
					multiValue.add(retrieveBase64Decoded(pair.getValue()));
				}
			}
		}
	}

	public CallingContextImpl(RNSPath root)
	{
		setCurrentPath(root);
	}

	@Override
	public synchronized Collection<Serializable> getProperty(String name)
	{
		Collection<Serializable> multiValue = null;
		multiValue = _properties.get(name);
		if (multiValue == null && _parent != null)
			return _parent.getProperty(name);
		return multiValue;
	}

	@Override
	public synchronized Serializable getSingleValueProperty(String name)
	{
		Collection<Serializable> multiValue = getProperty(name);
		if (multiValue == null) {
			return null;
		}
		return multiValue.iterator().next();
	}

	@Override
	public synchronized void setProperty(String name, Collection<Serializable> multiValue)
	{
		if ((multiValue != null) && (multiValue.isEmpty())) {
			throw new IllegalArgumentException("Illegal empty multiValue, use null instead");
		}
		_properties.put(name, multiValue);
	}

	@Override
	public synchronized void setSingleValueProperty(String name, Serializable value)
	{
		if (value == null) {
			_logger.error("attempting to store a null Serializable object.");
		}
		ArrayList<Serializable> multiValue = new ArrayList<Serializable>();
		multiValue.add(value);
		setProperty(name, multiValue);
	}

	@Override
	public synchronized void removeProperty(String name)
	{
		_properties.remove(name);
		if (_parent != null) {
			_parent.removeProperty(name);
		}
	}

	@Override
	public synchronized Serializable getTransientProperty(String name)
	{
		Serializable obj = _transientProperties.get(name);
		if (obj == null && _parent != null)
			obj = _parent.getTransientProperty(name);

		return obj;
	}

	@Override
	public synchronized void setTransientProperty(String name, Serializable value)
	{
		_transientProperties.put(name, value);
	}

	@Override
	public synchronized void removeTransientProperty(String name)
	{
		_transientProperties.remove(name);
		if (_parent != null)
			_parent.removeTransientProperty(name);
	}

	/**
	 * helper method for debugging; this shows what the context contains, at least as far as key names.
	 */
	@Override
	public String dumpContext()
	{
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(
			"====\ncontext has " + _properties.size() + " normal properties and " + _transientProperties.size() + " transient properties.\n");
		toReturn.append("normal property names:\n");
		Set<String> propnames = _properties.keySet();
		int indy = 0;
		for (String name : propnames) {
			toReturn.append("prop#" + indy++ + ": " + name + "\n");
		}
		toReturn.append("transient property names:\n");
		Set<String> tpropnames = _transientProperties.keySet();
		indy = 0;
		for (String name : tpropnames) {
			toReturn.append("tran#" + indy++ + ": " + name + "\n");
		}
		toReturn.append("====\n");
		return toReturn.toString();
	}

	@Override
	public synchronized void setActiveKeyAndCertMaterial(KeyAndCertMaterial clientKeyMaterial) throws AuthZSecurityException
	{
		// this transient property always gets put in the top parent context
		if (_parent != null) {
			_parent.setActiveKeyAndCertMaterial(clientKeyMaterial);
			return;
		}
		setTransientProperty(CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA, clientKeyMaterial);
	}

	@Override
	public synchronized KeyAndCertMaterial getActiveKeyAndCertMaterial() throws AuthZSecurityException
	{
		KeyAndCertMaterial toReturn = (KeyAndCertMaterial) getTransientProperty(CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA);
		// 2020-12-2 by ASG
		// Since we have no made it possible that there is no active key and cert material for this object, as it may
		// have no X.509 at all, I am commenting out the whole next block.
		
		if (toReturn == null) {
			if (!ConfigurationManager.getCurrentConfiguration().isClientRole()) {
				_logger.error("failure: no active key material for container role!  this is a problem.");
				new Exception().printStackTrace();
			}
		}
		
		return toReturn;
	}

	@Override
	public synchronized RNSPath getCurrentPath()
	{
		Collection<Serializable> multiValue = _properties.get(CURRENT_PATH_KEY);
		if (multiValue != null) {
			return (RNSPath) multiValue.iterator().next();
		}

		if (_parent != null) {
			return _parent.getCurrentPath();
		}
		return null;
	}

	@Override
	public synchronized void setCurrentPath(RNSPath newPath)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("current path being set to: " + (newPath == null ? "null" : newPath.toString()));
		setSingleValueProperty(CURRENT_PATH_KEY, newPath);
	}

	@Override
	synchronized public ContextType getSerialized() throws IOException
	{
		ContextType ct = new ContextType();

		LinkedList<ContextNameValuePairType> pairs = new LinkedList<ContextNameValuePairType>();
		accumulateProperties(pairs);
		ContextNameValuePairType[] pairsArray = new ContextNameValuePairType[pairs.size()];
		pairs.toArray(pairsArray);
		ct.setProperty(pairsArray);

		return ct;
	}

	@Override
	public ICallingContext deriveNewContext()
	{
		return new CallingContextImpl(this);
	}

	@Override
	public ICallingContext deriveNewContext(ContextType serializedInformation) throws IOException
	{
		CallingContextImpl retval = new CallingContextImpl(this);

		// load the properties from the ContextType
		if (serializedInformation != null) {
			ContextNameValuePairType[] pairs = serializedInformation.getProperty();
			if (pairs != null) {
				for (ContextNameValuePairType pair : pairs) {
					String name = pair.getName();
					Collection<Serializable> multiValue = retval._properties.get(name);
					if (multiValue == null) {
						multiValue = new ArrayList<Serializable>();
						retval._properties.put(name, multiValue);
					}

					multiValue.add(retrieveBase64Decoded(pair.getValue()));
				}
			}
		}

		return retval;
	}

	private synchronized void accumulateProperties(List<ContextNameValuePairType> pairs) throws IOException
	{
		if (_parent != null) {
			_parent.accumulateProperties(pairs);
		}

		for (String name : _properties.keySet()) {
			Collection<Serializable> values = _properties.get(name);
			for (Serializable val : values) {
				String strVal = retrieveBase64Encoded(val);
				pairs.add(0, new ContextNameValuePairType(name, strVal));
			}
		}
	}

	@Override
	public void serializeTransientProperties(ObjectOutput out) throws IOException
	{

		out.writeObject(_transientProperties);
		if (_parent == this) {
			String msg = "parent is set to this - about to do infinite recurrsion";
			_logger.error(msg);
			throw new IOException(msg);
		}

		if (_parent != null) {
			// _logger.debug("after writing self, recursing into parent for serialization");
			out.writeBoolean(true);
			_parent.serializeTransientProperties(out);
		} else {
			out.writeBoolean(false);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserializeTransientProperties(ObjectInput in) throws IOException
	{
		try {
			_transientProperties.putAll((HashMap<String, Serializable>) in.readObject());
			if (in.readBoolean()) {
				// read in another set from the parent.
				// _logger.debug("recursing a level to read nested transient properties for context.");
				deserializeTransientProperties(in);
			}
		} catch (InvalidClassException e) {
			_logger.warn("could not load transient properties; old serialization found: " + e.getMessage());
			// we drop the exception on the floor; it's expected after unicore7 update.
		} catch (ClassNotFoundException e) {
			_logger.error("failure loading transient properties:", e);
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}

	void setTransientProperties(HashMap<String, Serializable> transientProps)
	{
		_transientProperties = transientProps;
	}

	private void collapseTransient(HashMap<String, Serializable> target)
	{
		if (_parent != null) {
			// _logger.debug("recursing back on parent to collapse its properties first.");
			_parent.collapseTransient(target);
		}

		for (String key : _transientProperties.keySet()) {
			target.put(key, _transientProperties.get(key));
		}
	}

	public Object writeReplace() throws ObjectStreamException
	{
		ByteArrayOutputStream baos = null;
		OutputStreamWriter writer = null;

		try {
			baos = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(baos);
			ContextStreamUtils.store(writer, this);
			writer.flush();
			HashMap<String, Serializable> transientCollapse = new HashMap<String, Serializable>();
			collapseTransient(transientCollapse);
			return new SerializedContext(baos.toByteArray(), transientCollapse);
		} catch (IOException ioe) {
			throw new NotSerializableException("CallingContextImpl");
		} finally {
			StreamUtils.close(writer);
		}
	}

	private ContextDescription describe(ContextDescription desc)
	{
		if (_parent != null)
			desc = _parent.describe(desc);

		for (String property : _properties.keySet())
			desc.setProperty(property, _properties.get(property));
		for (String property : _transientProperties.keySet())
			desc.setTransientProperty(property, _transientProperties.get(property));

		return desc;
	}

	@Override
	public ContextDescription describe()
	{
		return describe(new ContextDescription());
	}

	static private Serializable retrieveBase64Decoded(String encoded) throws IOException
	{
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.base64ToByteArray(encoded)));
		try {
			return (Serializable) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}

	static private String retrieveBase64Encoded(Serializable obj) throws IOException
	{
		String retval = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.close();
		retval = Base64.byteArrayToBase64(baos.toByteArray());
		return retval;
	}
}
