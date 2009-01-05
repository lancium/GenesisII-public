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
package edu.virginia.vcgr.genii.client;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public class WellKnownPortTypes
{
	static public PortType GENII_NOTIFICATION_PRODUCER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiNotificiationProducerPortType"));
	static public PortType GENII_SUBSCRIPTION_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiSubscriptionPortType"));
	static public PortType GENII_NOTIFICATION_CONSUMER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiNotificationConsumerPortType"));
	static public PortType GENII_RESOURCE_FACTORY_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory",
			"GeniiResourceFactory"));
	static public PortType GENII_RESOURCE_ATTRS_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-attrs",
			"GeniiResourceAttrs"));
	static public PortType GENII_RESOURCE_AUTHZ_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-authz",
			"GeniiResourceAuthz"));
	static public PortType VCGR_COMMON_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/common",
			"VCGRCommon"));
	static public PortType COUNTER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/counter/2006/06/counter",
			"CounterPortType"));
	
	static public PortType RESOURCE_FORK_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/rfork",
			"ResourceForkPortType"));
	
	static public PortType APPDESC_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/application-description",
			"ApplicationDescriptionPortType"));
	static public PortType DEPLOYER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/application-deployer",
			"ApplicationDeployerPortType"));
	
	static public PortType SCHEDULER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2007/01/scheduler",
			"SchedulerPortType"));
	static public PortType BASIC_SCHEDULER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2007/01/scheduler/basic",
			"BasicSchedulerPortType"));
	
	static public PortType RBYTEIO_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://schemas.ggf.org/byteio/2005/10/random-access",
			"RandomByteIOPortType"));
	static public PortType SBYTEIO_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://schemas.ggf.org/byteio/2005/10/streamable-access",
			"StreamableByteIOPortType"));
	static public PortType SBYTEIO_FACTORY_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory",
			"StreamableByteIOFactory"));

	static public PortType EXPORTED_ROOT_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-root",
			"ExportedRootPortType"));
	
	static public PortType EXPORTED_FILE_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/byteio/2006/08/exported-file",
			"ExportedFilePortType"));
	
	static public PortType EXPORTED_DIR_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-dir",
			"ExportedDirPortType"));
	
	static public PortType VCGR_CONTAINER_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/container/2006/07/container",
			"VCGRContainerPortType"));

	static public PortType STS_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
			"SecurityTokenService"));
	static public PortType X509_AUTHN_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/container/2007/11/x509-authn",
			"X509AuthnPortType"));
	static public PortType JNDI_AUTHN_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/container/2007/11/jndi-authn",
			"JNDIAuthnPortType"));

	static public PortType ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl/EndpointIdentifierResolver",
			"EndpointIdentifierResolverPortType"));
	static public PortType REFERENCE_RESOLVER_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl/ReferenceResolver",
			"ReferenceResolverPortType"));
	static public PortType GENII_SIMPLE_RESOLVER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/container/2006/12/simple-resolver",
			"SimpleResolverPortType"));
	static public PortType GENII_SIMPLE_RESOLVER_FACTORY_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/container/2006/12/simple-resolver-factory",
			"SimpleResolverFactoryPortType"));
	
	static public PortType REXPORT_RESOLVER_FACTORY_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory",
			"RExportResolverFactoryPortType"));
	static public PortType REXPORT_RESOLVER_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver",
			"RExportResolverPortType"));
	static public PortType REXPORT_DIR_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/rns/2007/08/rexport-dir",
			"RExportDirPortType"));
	
	
	static public PortType GENII_FACTORY_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2006/12/factory-pattern",
			"GeniiFactoryPortType"));
	
	static public PortType CERT_GENERATOR_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator",
			"CertGeneratorPortType"));
	
	static public PortType INFORMATION_SERVICE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/informationService",
			"InformationServicePortType"));
	
	static public PortType GENESIS_DAI_CORE_DATA_ACCESS_COMBINED_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/genesis_dai",
			"CoreDataAccessCombinedPortType"));
	
	static public PortType GENESIS_DAI_WSRF_DATA_RESOURCE_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/genesis_dai",
			"WSRFDataResourcePortType"));
	
	static public PortType GENESIS_DAIR_SQL_ACCESS_COMBINED_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/genesis_dair",
			"SQLAccessCombinedPortType"));
	
	static public PortType GENESIS_DAIR_SQL_RESPONSE_COMBINED_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/genesis_dair",
			"SQLResponseCombinedPortType"));
	
	static public PortType GENESIS_DAIR_SQL_ROWSET_ACCESS_PORT_TYPE =
		PortType.get(new QName("http://vcgr.cs.virginia.edu/genii/genesis_dair",
			"SQLRowsetAccessPortType"));
}
