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

import edu.virginia.vcgr.genii.client.bes.BESConstants;

public class WellKnownPortTypes
{
	static public QName GENII_NOTIFICATION_PRODUCER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiNotificiationProducerPortType");
	static public QName GENII_SUBSCRIPTION_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiSubscriptionPortType");
	static public QName GENII_NOTIFICATION_CONSUMER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/notification",
			"GeniiNotificationConsumerPortType");
	static public QName GENII_RESOURCE_LIFETIME_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-lifetime",
			"GeniiResourceLifetime");
	static public QName GENII_RESOURCE_FACTORY_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory",
			"GeniiResourceFactory");
	static public QName GENII_RESOURCE_ATTRS_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-attrs",
			"GeniiResourceAttrs");
	static public QName GENII_RESOURCE_AUTHZ_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-authz",
			"GeniiResourceAuthz");
	static public QName VCGR_COMMON_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/common",
			"VCGRCommon");
	static public QName COUNTER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/counter/2006/06/counter",
			"CounterPortType");
	static public QName RNS_SERVICE_PORT_TYPE =
		new QName("http://schemas.ggf.org/rns/2006/05/rns",
			"RNSPortType");
	static public QName APPDESC_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/application-description",
			"ApplicationDescriptionPortType");
	static public QName DEPLOYER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/application-deployer",
			"ApplicationDeployerPortType");
	static public QName QUEUE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/queue",
			"QueuePortType");
	
	static public QName SCHEDULER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2007/01/scheduler",
			"SchedulerPortType");
	static public QName BASIC_SCHEDULER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2007/01/scheduler/basic",
			"BasicSchedulerPortType");
	
	static public QName BES_SERVICE_PORT_TYPE =
		new QName(BESConstants.BES_NS, "BESPortType");
	static public QName BES_FACTORY_PORT_TYPE =
		new QName("http://schemas.ggf.org/bes/2006/08/bes-factory",
			"BESFactoryPortType");
	static public QName VCGR_BES_ACTIVITY_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/bes/2006/07/bes-activity");
	
	static public QName RBYTEIO_SERVICE_PORT_TYPE =
		new QName("http://schemas.ggf.org/byteio/2005/10/random-access",
			"RandomByteIOPortType");
	static public QName SBYTEIO_SERVICE_PORT_TYPE =
		new QName("http://schemas.ggf.org/byteio/2005/10/streamable-access",
			"StreamableByteIOPortType");
	static public QName SBYTEIO_FACTORY_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory",
			"StreamableByteIOFactory");

	static public QName EXPORTED_ROOT_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-root",
			"ExportedRootPortType");
	
	static public QName EXPORTED_FILE_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/byteio/2006/08/exported-file",
			"ExportedFilePortType");
	
	static public QName EXPORTED_DIR_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-dir",
			"ExportedDirPortType");
	
	static public QName VCGR_CONTAINER_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/container/2006/07/container",
			"VCGRContainerPortType");

	static public QName ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE =
		new QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl/EndpointIdentifierResolver",
			"EndpointIdentifierResolverPortType");
	static public QName REFERENCE_RESOLVER_SERVICE_PORT_TYPE =
		new QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl/ReferenceResolver",
			"ReferenceResolverPortType");
	static public QName GENII_SIMPLE_RESOLVER_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/container/2006/12/simple-resolver",
			"SimpleResolverPortType");
	static public QName GENII_SIMPLE_RESOLVER_FACTORY_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/container/2006/12/simple-resolver-factory",
			"SimpleResolverFactoryPortType");
	
	static public QName GENII_FACTORY_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/factory-pattern",
			"GeniiFactoryPortType");
	
	static public QName CERT_GENERATOR_SERVICE_PORT_TYPE =
		new QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator",
			"CertGeneratorPortType");
}
