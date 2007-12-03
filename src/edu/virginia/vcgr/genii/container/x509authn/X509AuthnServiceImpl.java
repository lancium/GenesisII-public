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
package edu.virginia.vcgr.genii.container.x509authn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.security.cert.CertificateEncodingException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.ReadNotPermittedFaultType;
import org.ggf.byteio.TransferInformationType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.ggf.byteio.WriteNotPermittedFaultType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.AppendResponse;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.ReadResponse;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.TruncAppendResponse;
import org.ggf.rbyteio.TruncateNotPermittedFaultType;
import org.ggf.rbyteio.Write;
import org.ggf.rbyteio.WriteResponse;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.DelegatedAssertion;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.byteio.TransferAgent;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.security.*;

import edu.virginia.vcgr.genii.x509authn.*;

import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

public class X509AuthnServiceImpl extends GenesisIIBase implements
		X509AuthnPortType {
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(X509AuthnServiceImpl.class);

	public X509AuthnServiceImpl() throws RemoteException {
		this(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE.getLocalPart());
	}

	protected X509AuthnServiceImpl(String serviceName) throws RemoteException {
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}

	protected Object translateConstructionParameter(MessageElement property)
			throws Exception {

		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.IDP_DELEGATED_IDENITY_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.IDP_USERNAME_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.IDP_PASSWORD_QNAME)) {
			return property.getValue();
		} else {
			return super.translateConstructionParameter(property);
		}
	}

	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters)
			throws ResourceException, BaseFaultType, RemoteException {

		// make sure the specific IDP doesn't yet exist
		String username = (String) constructionParameters
				.get(SecurityConstants.IDP_USERNAME_QNAME);
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IRNSResource serviceResource = (IRNSResource) serviceKey.dereference();
		Collection<String> entries = serviceResource.listEntries();
		if (entries.contains(username)) {
			throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(
					null, null, null, null, null, null, username));
		}

		// add the delegated identity to the service's list of IDPs
		serviceResource.addEntry(new InternalEntry(username, newEPR, null));
		serviceResource.commit();

		// store the delegated identity in the resource's state
		String encodedAssertion = (String) constructionParameters
				.get(SecurityConstants.IDP_DELEGATED_IDENITY_QNAME);
		IResource resource = rKey.dereference();
		resource.setProperty(SecurityConstants.IDP_DELEGATED_IDENITY_QNAME
				.getLocalPart(), encodedAssertion);

		super.postCreate(rKey, newEPR, constructionParameters);
	}

	protected void setAttributeHandlers() throws NoSuchMethodException {
		super.setAttributeHandlers();
		new X509AuthnAttributeHandlers(getAttributePackage());
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType requestSecurityToken(
			RequestSecurityTokenType request) throws java.rmi.RemoteException {
		return null;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(
			RequestSecurityTokenType request) throws java.rmi.RemoteException {
		return null;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFile)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType {
		throw new RemoteException("\"createFile\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType, RNSFaultType {
		throw new RemoteException("\"add\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List list) throws RemoteException,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType {

		String entry_name_regexp = list.getEntry_name_regexp();
		IRNSResource resource = null;
		Collection<InternalEntry> entries;

		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRNSResource) rKey.dereference();
		entries = resource.retrieveEntries(entry_name_regexp);

		EntryType[] ret = new EntryType[entries.size()];
		int lcv = 0;
		for (InternalEntry entry : entries) {
			ret[lcv++] = new EntryType(entry.getName(), entry.getAttributes(),
					entry.getEntryReference());
		}

		return new ListResponse(ret);
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move move) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType {
		throw new RemoteException("\"move\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query q) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType {
		throw new RemoteException("\"query\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove remove) throws RemoteException,
			ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType,
			RNSFaultType {
		throw new RemoteException("\"remove\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public ReadResponse read(Read read) throws RemoteException,
			CustomFaultType, ReadNotPermittedFaultType,
			UnsupportedTransferFaultType, ResourceUnknownFaultType {

		// load up the delegated assertion
		byte[] backingData = null;
		try {

			ResourceKey rKey = ResourceManager.getCurrentResource();
			IResource resource = rKey.dereference();
			String encodedAssertion = (String) resource
					.getProperty(SecurityConstants.IDP_DELEGATED_IDENITY_QNAME
							.getLocalPart());
			DelegatedAssertion delegatedAssertion = (DelegatedAssertion) DelegatedAssertion
					.base64decodeAssertion(encodedAssertion);
			X509Certificate[] assertingIdentity = delegatedAssertion
					.getAssertingIdentityCertChain();

			backingData = assertingIdentity[0].getEncoded();

		} catch (IOException ioe) {
			throw FaultManipulator
					.fillInFault(new CustomFaultType(
							null,
							null,
							null,
							null,
							new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
									ioe.toString()) }, null));
		} catch (ClassNotFoundException ioe) {
			throw FaultManipulator
					.fillInFault(new CustomFaultType(
							null,
							null,
							null,
							null,
							new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
									ioe.toString()) }, null));
		} catch (CertificateEncodingException ioe) {
			throw FaultManipulator
					.fillInFault(new CustomFaultType(
							null,
							null,
							null,
							null,
							new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
									ioe.toString()) }, null));
		}

		int bytesPerBlock = read.getBytesPerBlock();
		int numBlocks = read.getNumBlocks();
		int startOffset = (int) read.getStartOffset();
		int stride = (int) read.getStride();
		TransferInformationType transferInformation = read
				.getTransferInformation();
		byte[] data = new byte[bytesPerBlock * numBlocks];

		int amtRead = 0;
		for (int block = 0; block < numBlocks; block++) {

			int amtAvail = backingData.length
					- (startOffset + (block * stride));
			if (amtAvail <= 0) {
				break;
			} else if (amtAvail > bytesPerBlock) {
				amtAvail = bytesPerBlock;
			}

			System.arraycopy(backingData, startOffset + (block * stride), data,
					bytesPerBlock * block, amtAvail);

			amtRead += amtAvail;
		}

		// resize data array if necessary
		if (amtRead < data.length) {
			byte[] tmp = data;
			data = new byte[amtRead];
			System.arraycopy(tmp, 0, data, 0, amtRead);
		}

		TransferAgent.sendData(data, transferInformation);

		return new ReadResponse(transferInformation);
	}

	@RWXMapping(RWXCategory.WRITE)
	public WriteResponse write(Write write) throws RemoteException,
			CustomFaultType, WriteNotPermittedFaultType,
			UnsupportedTransferFaultType, ResourceUnknownFaultType {
		throw new RemoteException("\"write\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public AppendResponse append(Append append) throws RemoteException,
			CustomFaultType, WriteNotPermittedFaultType,
			UnsupportedTransferFaultType, ResourceUnknownFaultType {
		throw new RemoteException("\"append\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public TruncAppendResponse truncAppend(TruncAppend truncAppend)
			throws RemoteException, CustomFaultType,
			WriteNotPermittedFaultType, TruncateNotPermittedFaultType,
			UnsupportedTransferFaultType, ResourceUnknownFaultType {
		throw new RemoteException("\"truncAppend\" not applicable.");
	}

	static public class X509AuthnAttributeHandlers extends
			AbstractAttributeHandler {
		public X509AuthnAttributeHandlers(AttributePackage pkg)
				throws NoSuchMethodException {
			super(pkg);
		}

		public Collection<MessageElement> getTransferMechsAttr() {
			ArrayList<MessageElement> ret = new ArrayList<MessageElement>();

			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_DIME_URI));
			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_MTOM_URI));

			return ret;
		}

		@Override
		protected void registerHandlers() throws NoSuchMethodException {
			addHandler(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					"getTransferMechsAttr");
		}
	}

}
