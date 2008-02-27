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
package edu.virginia.vcgr.genii.container.certGenerator;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainRequestType;
import edu.virginia.vcgr.genii.certGenerator.CertGeneratorPortType;
import edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainResponseType;
import edu.virginia.vcgr.genii.certGenerator.InvalidCertificateRequestFaultType;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.CertGeneratorUtils;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.common.security.PublicKeyType;
import edu.virginia.vcgr.genii.common.security.X509NameType;
import edu.virginia.vcgr.genii.common.security.CertificateChainType;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;

public class CertGeneratorServiceImpl extends GenesisIIBase implements CertGeneratorPortType
{	
	static private Log _logger = LogFactory.getLog(CertGeneratorServiceImpl.class);
	
	public CertGeneratorServiceImpl() throws RemoteException
	{
		super("CertGeneratorPortType");
		
		addImplementedPortType(WellKnownPortTypes.CERT_GENERATOR_SERVICE_PORT_TYPE);
	}
	
	protected CertGeneratorServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		addImplementedPortType(WellKnownPortTypes.CERT_GENERATOR_SERVICE_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.CERT_GENERATOR_SERVICE_PORT_TYPE;
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
    public GenerateX509V3CertificateChainResponseType generateX509V3CertificateChain(GenerateX509V3CertificateChainRequestType request) 
		throws java.rmi.RemoteException, 
			edu.virginia.vcgr.genii.certGenerator.InvalidCertificateRequestFaultType, 
			ResourceUnknownFaultType
	{
		ICertGeneratorResource resource = null;
		GenerateX509V3CertificateChainResponseType response = null;
		
		if (request == null)
		{
			throw FaultManipulator.fillInFault(
				new InvalidCertificateRequestFaultType());
		}
		
		PublicKeyType pkt = request.getPublicKey();
		X509NameType x509Name = request.getX509Name();
		if (x509Name == null)
		{
			throw FaultManipulator.fillInFault(
				new InvalidCertificateRequestFaultType());
		}

		PublicKey pk = null;
		try
		{
			pk = SecurityUtils.deserializePublicKey(pkt.getPublicKey());
		}
		catch(Throwable t)
		{
			_logger.error("Invalid Certificate Request", t);
			throw FaultManipulator.fillInFault(
				new InvalidCertificateRequestFaultType());
		}

		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ICertGeneratorResource)rKey.dereference();
		
		// get CA certificate from resource state.
		long defaultDuration = resource.getDefaultValidity();
		X509Certificate[] issuerChain = resource.getIssuerChain();
		PrivateKey issuerPrivateKey = resource.getIssuerPrivateKey();

		try
		{
			X509Certificate issuerCert = issuerChain[0];
			issuerCert.checkValidity(new Date());
		
			PublicKey issuerPK = null;
			if (issuerChain.length == 1)
				issuerPK = issuerCert.getPublicKey();
			else
			{
				X509Certificate parentCert = issuerChain[1];
				issuerPK = parentCert.getPublicKey();
			}
			issuerCert.verify(issuerPK);
		}
		catch(Exception e)
		{
			System.err.println("Sanity check failed for issuer certificate");
		}

		X509Certificate newCert = null;
		
		try
		{
			newCert = CertTool.createIntermediateCert(x509Name.getX509Name(), defaultDuration, pk, issuerPrivateKey, issuerChain[0]);

			int count = issuerChain.length + 1;
			byte[][] newCertChainBytes = new byte[count][];
			newCertChainBytes[0] = newCert.getEncoded();
			for (int i = 0; i < issuerChain.length; i++) {
				newCertChainBytes[i+1] = issuerChain[i].getEncoded();
			}
			CertificateChainType certificateChain = new CertificateChainType(count, newCertChainBytes);
			response = new GenerateX509V3CertificateChainResponseType(certificateChain);
		}
		catch(GeneralSecurityException gse)
		{
			_logger.error("A security exception occurred.", gse);
			// TODO:  Add a better exception for these types of failures...
			throw FaultManipulator.fillInFault(
				new InvalidCertificateRequestFaultType());
		}
		
		return response;
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> creationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		_logger.debug("Creating new certGenerator Resource.");
		
		super.postCreate(rKey, newEPR, creationParameters);
		
		
		ICertGeneratorResource resource = null;
		
		resource = (ICertGeneratorResource)rKey.dereference();
		resource.setCertificateIssuerInfo(creationParameters);
		resource.commit();
	}
		
	protected Object translateConstructionParameter(MessageElement parameter)
		throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(CertGeneratorUtils.CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER))
			return new Long((String) parameter.getObjectValue(String.class));
		else if (messageName.equals(CertGeneratorUtils.CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER))
		{
			byte [] certChainBytes = (byte []) parameter.getObjectValue(byte[].class);
			X509Certificate[] certChain = (X509Certificate [])DBSerializer.deserialize(certChainBytes);

			X509Certificate issuerCert = certChain[0];
			issuerCert.checkValidity(new Date());
			PublicKey pk = null;
			if (certChain.length == 1)
				pk = issuerCert.getPublicKey();
			else
			{
				X509Certificate parentCert = certChain[1];
				pk = parentCert.getPublicKey();
			}
			issuerCert.verify(pk);
			
			return certChain;
		}
		else if (messageName.equals(CertGeneratorUtils.CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER))
		{
			byte [] privateKeyBytes = (byte []) parameter.getObjectValue(byte[].class);
			PrivateKey privateKey = (PrivateKey) DBSerializer.deserialize(privateKeyBytes);
			return privateKey;
		}
		else
			return super.translateConstructionParameter(parameter);
	}
}
