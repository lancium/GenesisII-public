package edu.virginia.vcgr.genii.container.rfork;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
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
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryDoesNotExistFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.sbyteio.SeekNotPermittedFaultType;
import org.ggf.sbyteio.SeekRead;
import org.ggf.sbyteio.SeekReadResponse;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.SeekWriteResponse;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rl_2.DestroyResponse;
import org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.naming.WSAddressingConstants;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.authz.rwx.*;
import edu.virginia.vcgr.genii.client.utils.IterableIterable;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.DefaultRandomByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.TransferAgent;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.invoker.timing.Timer;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingSink;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.cmd.CommandChannelManager;
import edu.virginia.vcgr.genii.container.rfork.sd.SimpleStateResourceFork;
import edu.virginia.vcgr.genii.container.rfork.sd.StateDescription;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.rfork.ResourceForkPortType;

public abstract class ResourceForkBaseService extends GenesisIIBase 
	implements ResourceForkPortType, ResourceForkService
{
	static private Log _logger = LogFactory.getLog(
		ResourceForkBaseService.class);
	
	private Object _forkLock = null;
	private ResourceKey _resourceKey = null;
	private EndpointReferenceType _exemplar = null;
	private ResourceForkInformation _forkInfo = null;
	private ResourceFork _fork = null;
	
	static public class DestroyForkMappingResolver implements MappingResolver
	{
		@Override
		public RWXCategory resolve(Class<?> serviceClass, Method operation)
		{
			try
			{
				ResourceKey rKey = ResourceManager.getCurrentResource();
				AddressingParameters ap = rKey.getAddressingParameters();
				Class<?> forkClass = null;
				if (ap != null)
				{
					ResourceForkInformation info = 
						(ResourceForkInformation)ap.getResourceForkInformation();
					ResourceFork fork = null;
					
					if (info != null)
						fork = info.instantiateFork(null);
					
					if (fork != null)
					{
						if (fork.getForkPath().equals("/"))
							return RWXCategory.EXECUTE;
						
						forkClass = fork.getClass();
					} else
						return RWXCategory.EXECUTE;
				}
				
				if (forkClass == null)
					return RWXCategory.EXECUTE;
				
				return RWXManager.lookup(forkClass, "destroy");
			} 
			catch (ResourceUnknownFaultType e)
			{
				throw new RWXMappingException("Unable to find RWXCategory.", 
					e);
			}
			catch (ResourceException e)
			{
				throw new RWXMappingException("Unable to find RWXCategory.", 
					e);
			}
		}	
	}
	
	static public class ForkMappingResolver implements MappingResolver
	{
		@Override
		public RWXCategory resolve(Class<?> serviceClass, Method operation)
		{
			MappingRedirect redirect = operation.getAnnotation(MappingRedirect.class);
			if (redirect == null)
				throw new RWXMappingException(
					"Unable to find mapping redirect annotation on method " 
					+ operation);
			
			try
			{
				ResourceKey rKey = ResourceManager.getCurrentResource();
				AddressingParameters ap = rKey.getAddressingParameters();
				Class<?> forkClass = null;
				if (ap != null)
				{
					ResourceForkInformation info = 
						(ResourceForkInformation)ap.getResourceForkInformation();
					if (info != null)
					{
						ResourceFork fork = info.instantiateFork(null);
						forkClass = fork.getClass();
					}
				}
				
				if (forkClass == null)
					forkClass = getFromRoot(serviceClass);
				
				return RWXManager.lookup(forkClass, redirect.methodName());
			} 
			catch (ResourceUnknownFaultType e)
			{
				throw new RWXMappingException("Unable to find RWXCategory.", 
					e);
			}
			catch (ResourceException e)
			{
				throw new RWXMappingException("Unable to find RWXCategory.", 
					e);
			}
		}
	}
	
	static private EndpointReferenceType cleanEPR(
		EndpointReferenceType epr)
	{
		AttributedURIType address = epr.getAddress();
		ReferenceParametersType refParams = epr.getReferenceParameters();
		MetadataType mdt = epr.getMetadata();
		
		if (refParams != null)
			refParams = new ReferenceParametersType(refParams.get_any());
		if (mdt != null)
		{
			MessageElement []anyArray = mdt.get_any();
			if (anyArray != null)
			{
				Collection<MessageElement> any = new ArrayList<MessageElement>(
					anyArray.length);
				for (MessageElement me : anyArray)
				{
					QName name = me.getQName();
					if ((!name.equals(
						OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME)) && (!name.equals(
								new QName(WSAddressingConstants.WSA_NS,"PortType"))) )
						any.add(me);
				}
				
				mdt = new MetadataType(any.toArray(
					new MessageElement[any.size()]));
			}
		}
		
		return new EndpointReferenceType(
			address, refParams, mdt, epr.get_any());
	}
	
	private EndpointReferenceType getExemplarEPR() 
		throws ResourceUnknownFaultType, ResourceException
	{
		synchronized(_forkLock)
		{
			if (_exemplar == null)
				_exemplar =	cleanEPR(getMyEPR(false));
			
			return _exemplar;
		}
	}
	
	static private Class<? extends ResourceFork> getFromRoot(Class<?> cl)
	{
		ForkRoot froot = null;
		Class<?> cl2 = cl;
		while (!cl2.getName().equals("java.lang.Object"))
		{
			froot = cl.getAnnotation(ForkRoot.class);
			if (froot != null)
				break;
			cl2 = cl2.getSuperclass();
		}
		
		if (froot == null)
			throw new ConfigurationException("Service class \"" +
				cl.getName() + 
				"\" does not have the required ForkRoot annotation.");
		
		return froot.value();
	}
	
	private void setFromRoot()
	{
		ForkRoot froot = null;
		Class<?> cl = getClass();
		while (!cl.getName().equals("java.lang.Object"))
		{
			froot = cl.getAnnotation(ForkRoot.class);
			if (froot != null)
				break;
			cl = cl.getSuperclass();
		}
		
		if (froot == null)
			throw new ConfigurationException("Service class \"" +
				getClass().getName() + 
				"\" does not have the required ForkRoot annotation.");
		
		try
		{
			Class<? extends RNSResourceFork> forkClass = froot.value();
			Constructor<? extends RNSResourceFork> cons = 
				forkClass.getConstructor(
					ResourceForkService.class, String.class);
			
			_fork = cons.newInstance(this, "/");
			_forkInfo = _fork.describe();
		}
		catch (Throwable cause)
		{
			throw new ConfigurationException("Unable to set up root fork.", 
				cause);
		}
	}
	
	private ResourceForkInformation getResourceForkInformation() 
		throws ResourceException, ResourceUnknownFaultType
	{
		synchronized(_forkLock)
		{
			if (_forkInfo == null)
			{
				EndpointReferenceType exemplar = getExemplarEPR();
				AddressingParameters ap = new AddressingParameters(
					exemplar.getReferenceParameters());
				_forkInfo = 
					(ResourceForkInformation)ap.getResourceForkInformation();
			}
			
			if (_forkInfo == null)
				setFromRoot();
			
			return _forkInfo;
		}
	}
	
	private ResourceFork getResourceFork()
		throws ResourceException, ResourceUnknownFaultType
	{
		synchronized(_forkLock)
		{
			if (_fork == null)
			{
				ResourceForkInformation forkInfo = 
					getResourceForkInformation();
				if (_fork == null)
					_fork = forkInfo.instantiateFork(this);
			}
			
			return _fork;
		}
	}
	
	@Override
	protected void registerNotificationHandlers(
		NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		try
		{
			ResourceFork fork = getResourceFork();
			if (fork != null)
				fork.registerNotificationHandlers(multiplexer);
		}
		catch (ResourceUnknownFaultType f)
		{
			_logger.warn("Unable to register notification handlers on fork.",
				f);
		}
		catch (ResourceException e)
		{
			_logger.warn("Unable to register notification handlers on fork.",
				e);
		}
	}

	protected ResourceForkBaseService(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		if (_forkLock == null)
			_forkLock = new Object();
		
		addImplementedPortType(WellKnownPortTypes.RESOURCE_FORK_PORT_TYPE);
		addImplementedPortType(RNSConstants.ENHANCED_RNS_PORT_TYPE);
	}
	
	@Override
	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		_forkLock = new Object();
		
		if (WorkingContext.getCurrentWorkingContext() != null)
		{
			ResourceFork fork = getResourceFork();
			if (fork instanceof RandomByteIOResourceFork)
				new RandomByteIOAttributeHandlers(
					(RandomByteIOResourceFork)fork,
					getAttributePackage());
			else if (fork instanceof StreamableByteIOResourceFork)
				new StreamableByteIOAttributeHandlers(
					(StreamableByteIOResourceFork)fork, getAttributePackage());
			else if (fork instanceof StreamableByteIOFactoryResourceFork)
				new StreamableByteIOFactoryAttributeHandlers(
					(StreamableByteIOFactoryResourceFork)fork, 
					getAttributePackage());
		}
	}
	
	private PortType[] getPortType(ResourceFork fork)
	{
		if (fork instanceof RNSResourceFork)
		{
			return new PortType []
			{
				RNSConstants.RNS_PORT_TYPE,
				RNSConstants.ENHANCED_RNS_PORT_TYPE
			};
		} 
		else if (fork instanceof RandomByteIOResourceFork)
			return new PortType [] { WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE };
		else if (fork instanceof StreamableByteIOFactoryResourceFork)
			return new PortType [] { WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE };
		else if (fork instanceof StreamableByteIOResourceFork)
			return new PortType [] { WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE };
		else
			throw new ConfigurationException(
				"Class \"" + fork.getClass() + "\" does not implement one " +
				"of the resource fork types.");
	}
	
	private PortType[] getPortType(ResourceForkInformation rif) 
		throws ResourceException
	{
		return getPortType(rif.instantiateFork(this));
	}
	
	@Override
	public PortType[] getImplementedPortTypes(ResourceKey rKey) 
		throws ResourceException, ResourceUnknownFaultType
	{
		PortType []portTypes = super.getImplementedPortTypes(rKey);
		PortType []additionalPT = getPortType(getResourceFork());
		
		PortType []ret = new PortType[portTypes.length + additionalPT.length];
		System.arraycopy(portTypes, 0, ret, 0, portTypes.length);
		System.arraycopy(additionalPT, 0, ret, portTypes.length,
			additionalPT.length);
		
		return ret;
	}
	
	@Override
	public String getMasterType(ResourceKey rKey)
	throws ResourceException, ResourceUnknownFaultType
	{
		if(getResourceForkInformation().forkPath().equalsIgnoreCase("/"))
			return null;	//corresponds to the root !
		
		return(getResourceFork().describe().forkClass().getSimpleName());
	}
	
	static final private Pattern EPI_PATTERN = Pattern.compile(
		"^(.+):fork-path:.+$");
	
	private EndpointReferenceType modifyEPI(EndpointReferenceType epr,
		String forkPath) throws ResourceException, ResourceUnknownFaultType
	{
		Collection<MessageElement> attributes;
		
		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			epr.setMetadata(mdt = new MetadataType(null));
		
		MessageElement []any = mdt.get_any();
		if (any == null)
			any = new MessageElement[0];
		
		attributes = new ArrayList<MessageElement>(any.length + 1);
		for (int lcv = 0; lcv < any.length; lcv++)
		{
			MessageElement element = any[lcv];
			QName name = element.getQName();
			if (name.equals(WSName.ENDPOINT_IDENTIFIER_QNAME))
			{
				String epi = element.getValue();
				Matcher matcher = EPI_PATTERN.matcher(epi);
				if (matcher.matches())
					epi = matcher.group(1);
				File forkFile = new File(forkPath);
				java.net.URI forkFileURI = forkFile.toURI();
				epi += ":fork-path:" + forkFileURI.getRawPath();
				any[lcv] = new MessageElement(
					WSName.ENDPOINT_IDENTIFIER_QNAME, epi);
				return epr;
			}
			
			attributes.add(element);
		}
		
		attributes.add(new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME,
			getResourceKey().dereference().getProperty(
					IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME)
					.toString() + ":fork-path:" + forkPath));
		mdt.set_any(attributes.toArray(new MessageElement[0]));
		return epr;
	}
	
	private EndpointReferenceType addPortTypes(EndpointReferenceType epr,
		PortType[] superPortTypes, PortType []myPortTypes, ResourceForkInformation rif)
	{
		Collection<PortType> portTypes = new ArrayList<PortType>(
			superPortTypes.length + 1);
		for (PortType superPT : superPortTypes)
			portTypes.add(superPT);
		for (PortType pt : myPortTypes)
			portTypes.add(pt);
		
		Collection<MessageElement> mdtList = new LinkedList<MessageElement>();
		
		MetadataType mdt = epr.getMetadata();
		if (mdt != null)
		{
			MessageElement []any = mdt.get_any();
			if (any != null)
			{
				for (MessageElement me : any)
					mdtList.add(me);
			}
		}
		
		mdtList.add(new MessageElement(
			OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME, 
			PortType.translate(portTypes)));
		
		mdtList.add(new MessageElement(new QName(WSAddressingConstants.WSA_NS,"PortType"), new QName(GenesisIIConstants.GENESISII_NS, rif.forkClass().getSimpleName())));
		
		return new EndpointReferenceType(
			epr.getAddress(), epr.getReferenceParameters(),
			new MetadataType(mdtList.toArray(new MessageElement[0])),
			epr.get_any());
	}
	
	@Override
	public EndpointReferenceType createForkEPR(String forkPath,
		ResourceForkInformation rif) 
			throws ResourceUnknownFaultType, ResourceException
	{
		EndpointReferenceType ret = getExemplarEPR();
		ret = addPortTypes(ret, super.getImplementedPortTypes(getResourceKey()),
			getPortType(rif), rif);
		ret = modifyEPI(ret, forkPath);
		AddressingParameters ap = new AddressingParameters(
			ret.getReferenceParameters());
		ap.setResourceForkInformation(rif);
		ret.setReferenceParameters(ap.toReferenceParameters());
		
		CommandChannelManager.appendMetadata(ret, rif.forkClass());
		
		return ret;
	}

	@Override
	public ResourceKey getResourceKey() 
		throws ResourceUnknownFaultType, ResourceException
	{
		synchronized(_forkLock)
		{
			if (_resourceKey == null)
				_resourceKey = ResourceManager.getCurrentResource();
			
			return _resourceKey;
		}
	}
	
	@Override
	@RWXMappingResolver(DestroyForkMappingResolver.class)
	public DestroyResponse destroy(Destroy destroyRequest)
			throws RemoteException, ResourceUnknownFaultType,
			ResourceNotDestroyedFaultType, ResourceUnavailableFaultType
	{
		ResourceFork fork = getResourceFork();
		fork.destroy();
		
		if (fork.getForkPath().equals("/"))
			return super.destroy(destroyRequest);
		
		return new DestroyResponse();
	}

	/* RNS Operations */
	protected RNSEntryResponseType add(RNSEntryType entryType)
		throws RemoteException
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RNSResourceFork))
			throw new RemoteException(
				"Target fork does not implement RNS interface.");
		
		RNSResourceFork fork = (RNSResourceFork)tFork;
		String entryName = entryType.getEntryName();
		EndpointReferenceType target = entryType.getEndpoint();
		try
		{
			if (target == null)
				return new RNSEntryResponseType(
					fork.mkdir(getExemplarEPR(), entryName),
					entryType.getMetadata(), null, entryName);
			else
				return new RNSEntryResponseType(
					fork.add(getExemplarEPR(), entryName, 
					target), entryType.getMetadata(), null, entryName);
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to add entry.", ioe);
		}
	}
	
	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "add")
	final public RNSEntryResponseType[] add(RNSEntryType[] addRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		RNSEntryResponseType []ret = new RNSEntryResponseType[addRequest.length];
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				ret[lcv] = add(addRequest[lcv]);
			}
			catch (BaseFaultType bft)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					bft, addRequest[lcv].getEntryName());
			}
			catch (Throwable cause)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(
						new BaseFaultType(null, null, null, null, 
							new BaseFaultTypeDescription[] { 
								new BaseFaultTypeDescription("Unable to add entry!") 
							}, null)), addRequest[lcv].getEntryName());
			}
		}
		
		return ret;
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "list")
	final public LookupResponseType lookup(String[] lookupRequest)
			throws RemoteException, org.ggf.rns.ReadNotPermittedFaultType
	{
		TimingSink tSink = TimingSink.sink();
		Timer timer = null;
		Iterable<InternalEntry> entries;
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RNSResourceFork))
			throw new RemoteException(
				"Target fork does not implement RNS interface.");
		
		RNSResourceFork fork = (RNSResourceFork)tFork;
		AttributesPreFetcherFactory factory = 
			new AttributesPreFetcherFactoryImpl();
		
		try
		{
			if (lookupRequest == null || lookupRequest.length == 0)
			{
				timer = tSink.getTimer("Retrieve Entries");
				entries = fork.list(getExemplarEPR(), null);
				timer.noteTime();
			} else
			{
				IterableIterable<InternalEntry> entryConglomerate =
					new IterableIterable<InternalEntry>();
				timer = tSink.getTimer("Retrieve Entries");
				for (String request : lookupRequest)
					entryConglomerate.add(
						fork.list(getExemplarEPR(), request));
				timer.noteTime();
				entries = entryConglomerate;
			}
			
			Collection<RNSEntryResponseType> resultEntries = 
				new LinkedList<RNSEntryResponseType>();
			timer = tSink.getTimer("Prepare Entries");
	    	for (InternalEntry internalEntry : entries)
	    	{
	    		EndpointReferenceType epr = internalEntry.getEntryReference();
	    		RNSEntryResponseType entry = new RNSEntryResponseType(
	    			epr, RNSUtilities.createMetadata(epr, 
	    				preFetch(epr, internalEntry.getAttributes(), factory)),
	    			null, internalEntry.getName());
	    		resultEntries.add(entry);
	    	}
	    	timer.noteTime();
			
	    	timer = tSink.getTimer("Create Iterator");
	    	return RNSContainerUtilities.translate(
	    		resultEntries, iteratorBuilder(
	    			RNSEntryResponseType.getTypeDesc().getXmlType()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to list contents.", ioe);
		}
		finally
		{
			if (timer != null)
				timer.noteTime();
		}
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	final public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException(
			"Rename not supported in Resource forks!");
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "createFile")
	final public CreateFileResponseType createFile(CreateFileRequestType request)
		throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RNSResourceFork))
			throw new RemoteException(
				"Target fork does not implement RNS interface.");
		
		RNSResourceFork fork = (RNSResourceFork)tFork;
		try
		{
			return new CreateFileResponseType(
				fork.createFile(getExemplarEPR(), request.getFilename()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to add entry.", ioe);
		}
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "remove")
	final public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		RNSEntryResponseType []ret = 
			new RNSEntryResponseType[removeRequest.length];
		
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RNSResourceFork))
			throw new RemoteException(
				"Target fork does not implement RNS interface.");
		
		RNSResourceFork fork = (RNSResourceFork)tFork;
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				if (fork.remove(removeRequest[lcv]))
					ret[lcv] = new RNSEntryResponseType(
						null, null, null, removeRequest[lcv]);
				else
					ret[lcv] = new RNSEntryResponseType(
						null, null, FaultManipulator.fillInFault(
							new RNSEntryDoesNotExistFaultType(
								null, null, null, null, new BaseFaultTypeDescription[] {
									new BaseFaultTypeDescription(
										String.format("Entry %s does not exist!", removeRequest[lcv]))
								}, null, removeRequest[lcv])), removeRequest[lcv]);
			}
			catch (BaseFaultType bft)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					bft, removeRequest[lcv]);
			}
			catch (Throwable cause)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(
						new BaseFaultType(null, null, null, null, 
							new BaseFaultTypeDescription[] { 
								new BaseFaultTypeDescription("Unable to remove entry!") 
							}, null)), removeRequest[lcv]);
			}
		}
		
		return ret;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	final public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException(
			"setMetadata operation not supported!");
	}

	/* Random ByteIO Operations */
	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "truncAppend")
	final public AppendResponse append(Append arg0) throws RemoteException,
			ResourceUnknownFaultType, WriteNotPermittedFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RandomByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement RByteIO interface.");
		
		RandomByteIOResourceFork fork = (RandomByteIOResourceFork)tFork;
		try
		{
			fork.truncAppend(fork.size(), ByteBuffer.wrap(
				TransferAgent.receiveData(arg0.getTransferInformation())));
			return new AppendResponse(new TransferInformationType(null,
				arg0.getTransferInformation().getTransferMechanism()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to append data.", ioe);
		}
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "read")
	final public ReadResponse read(Read arg0) throws RemoteException,
			ReadNotPermittedFaultType, ResourceUnknownFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RandomByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement RByteIO interface.");
		
		RandomByteIOResourceFork fork = (RandomByteIOResourceFork)tFork;
		try
		{
			long startOffset = arg0.getStartOffset();
			int bytesPerBlock = arg0.getBytesPerBlock();
			long stride = arg0.getStride();
			int numBlocks = arg0.getNumBlocks();
			TransferInformationType transferInformation =
				arg0.getTransferInformation();
			ByteBuffer dest = ByteBuffer.allocate(
				bytesPerBlock * numBlocks);
			
			while (dest.hasRemaining())
			{
				ByteBuffer tmpDest = dest.slice();
				tmpDest.limit(bytesPerBlock);
				fork.read(startOffset, tmpDest);
				int read = tmpDest.position();
				dest.position(dest.position() + read);
				if (read < bytesPerBlock)
					break;
				startOffset += stride;
			}
			
			dest.flip();
			byte []data = new byte[dest.remaining()];
			dest.get(data);
			TransferAgent.sendData(data,
				transferInformation);
			return new ReadResponse(transferInformation);
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to read data.", ioe);
		}
	}
	
	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "truncAppend")
	final public TruncAppendResponse truncAppend(TruncAppend arg0)
			throws RemoteException, ResourceUnknownFaultType,
			WriteNotPermittedFaultType, TruncateNotPermittedFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RandomByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement RByteIO interface.");
		
		RandomByteIOResourceFork fork = (RandomByteIOResourceFork)tFork;
		try
		{
			fork.truncAppend(arg0.getOffset(),
				ByteBuffer.wrap(
					TransferAgent.receiveData(arg0.getTransferInformation())));
			return new TruncAppendResponse(new TransferInformationType(null,
				arg0.getTransferInformation().getTransferMechanism()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to truncAppend data.", ioe);
		}
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "write")
	final public WriteResponse write(Write arg0) throws RemoteException,
			ResourceUnknownFaultType, WriteNotPermittedFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof RandomByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement RByteIO interface.");
		
		RandomByteIOResourceFork fork = (RandomByteIOResourceFork)tFork;
		try
		{
			long startOffset = arg0.getStartOffset();
			int bytesPerBlock = arg0.getBytesPerBlock();
			long stride = arg0.getStride();
			ByteBuffer source = ByteBuffer.wrap(TransferAgent.receiveData(
				arg0.getTransferInformation()));
			
			while (source.hasRemaining())
			{
				int toWrite = bytesPerBlock;
				if (toWrite > source.remaining())
					toWrite = source.remaining();
				
				ByteBuffer tmpSource = source.slice();
				tmpSource.limit(toWrite);
				fork.write(startOffset, tmpSource);
				source.position(source.position() + toWrite);
				startOffset += stride;
			}
			
			return new WriteResponse(new TransferInformationType(null,
				arg0.getTransferInformation().getTransferMechanism()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to write data.", ioe);
		}
	}

/* Streamable ByteIO Operations */
	private SeekOrigin translate(URI seekOriginURI) throws RemoteException
	{
		if (seekOriginURI.equals(ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI))
			return SeekOrigin.SEEK_BEGINNING;
		else if (seekOriginURI.equals(
			ByteIOConstants.SEEK_ORIGIN_CURRENT_URI))
				return SeekOrigin.SEEK_CURRENT;
		else if (seekOriginURI.equals(
			ByteIOConstants.SEEK_ORIGIN_END_URI))
				return SeekOrigin.SEEK_END;
		else
			throw new RemoteException("Invalid seek operation.");
	}
	
	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "seekRead")
	final public SeekReadResponse seekRead(SeekRead seekReadRequest) 
		throws RemoteException,
			ReadNotPermittedFaultType, SeekNotPermittedFaultType,
			ResourceUnknownFaultType, UnsupportedTransferFaultType,
			CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof StreamableByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement SByteIO interface.");
		
		int numBytes = seekReadRequest.getNumBytes().intValue();
		long seekOffset = seekReadRequest.getOffset();
		SeekOrigin seekOrigin = translate(seekReadRequest.getSeekOrigin());
		TransferInformationType transType = seekReadRequest.getTransferInformation();
		
		StreamableByteIOResourceFork fork = 
			(StreamableByteIOResourceFork)tFork;
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate(numBytes);
			fork.seekRead(seekOrigin, seekOffset, buffer);
			buffer.flip();
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			TransferAgent.sendData(data, transType);
			return new SeekReadResponse(transType);
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to read from stream.", ioe);
		}
	}

	@Override
	@RWXMappingResolver(ForkMappingResolver.class)
	@MappingRedirect(methodName = "seekWrite")
	final public SeekWriteResponse seekWrite(SeekWrite seekWriteRequest)
		throws RemoteException,
			SeekNotPermittedFaultType, ResourceUnknownFaultType,
			WriteNotPermittedFaultType, UnsupportedTransferFaultType,
			CustomFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof StreamableByteIOResourceFork))
			throw new RemoteException(
				"Target fork does not implement SByteIO interface.");
		
		long seekOffset = seekWriteRequest.getOffset();
		SeekOrigin seekOrigin = translate(seekWriteRequest.getSeekOrigin());
		TransferInformationType transType = 
			seekWriteRequest.getTransferInformation();
		ByteBuffer source = ByteBuffer.wrap(TransferAgent.receiveData(
			transType));
		
		StreamableByteIOResourceFork fork = 
			(StreamableByteIOResourceFork)tFork;
		try
		{
			fork.seekWrite(seekOrigin, seekOffset, source);
			return new SeekWriteResponse(new TransferInformationType(null,
				transType.getTransferMechanism()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to write to stream.", ioe);
		}
	}	
	
/* Streamable ByteIO Factory Operations */
	@Override
	@RWXMapping(RWXCategory.OPEN)
	final public OpenStreamResponse openStream(Object arg0) throws RemoteException,
			ResourceUnknownFaultType, ResourceCreationFaultType
	{
		ResourceFork tFork = getResourceFork();
		if (!(tFork instanceof StreamableByteIOFactoryResourceFork))
			throw new RemoteException(
				"Target fork does not implement SByteIO Factory interface.");
		
		try
		{
			StreamableByteIOFactoryResourceFork fork =
				(StreamableByteIOFactoryResourceFork)tFork;
			
			StreamableFactoryConfiguration configuration =
				fork.getClass().getAnnotation(
					StreamableFactoryConfiguration.class);
			boolean notifyOnDestroy =
				configuration == null ?
					true : configuration.notifyOnDestroy();
			
			if (configuration == null && 
				(fork instanceof SimpleStateResourceFork<?>))
			{
				StateDescription description =
					fork.getClass().getAnnotation(StateDescription.class);
				if (description != null)
					notifyOnDestroy = description.writable();
			}
			
			DefaultStreamableByteIOResourceFork newFork =
				new DefaultStreamableByteIOResourceFork(
					this, "/" + new GUID(), 
					(configuration == null) ? 
						true : configuration.destroyOnClose(),
					notifyOnDestroy, fork);
			
			return new OpenStreamResponse(
				createForkEPR(newFork.getForkPath(), newFork.describe()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to open stream.", ioe);
		}		
	}
	
	private ResourceFork getMyByteIOFork(EndpointReferenceType target)
	{
		try
		{
			AddressingParameters ap = new AddressingParameters(
				target.getReferenceParameters());
			
			ResourceForkInformation rfi = 
				(ResourceForkInformation)ap.getResourceForkInformation();
			
			if (rfi != null)
			{
				String targetKey = ap.getResourceKey();
				String myKey = getResourceKey().getResourceKey();
				if (targetKey != null && myKey != null && 
					(targetKey.equals(myKey)))
				{
					ResourceFork fork = rfi.instantiateFork(this);
					if (fork instanceof RandomByteIOResourceFork)
						return fork;
					if (fork instanceof StreamableByteIOResourceFork)
						return fork;
					if (fork instanceof StreamableByteIOFactoryResourceFork)
						return fork;
				}
			}
		}
		catch (Throwable cause)
		{
			// If anything goes wrong, we simply don't fill in the attributes.
			_logger.warn(
				"Unable to fill in the attributes for a resource fork.", 
				cause);
		}
		
		return null;
	}
	
	private class AttributesPreFetcherFactoryImpl
		implements AttributesPreFetcherFactory
	{
		@Override
		public AttributePreFetcher getPreFetcher(EndpointReferenceType epr)
				throws Throwable
		{
			ResourceFork fork = null;
			
			if (Container.getServiceURL("RandomByteIOPortType").equalsIgnoreCase(
				epr.getAddress().toString()))
			{
				return new DefaultRandomByteIOAttributePreFetcher(epr);
			} else if ( (fork = getMyByteIOFork(epr)) != null)
			{
				if (fork instanceof RandomByteIOResourceFork)
				{
					return new RandomByteIOForkAttributePreFetcher(
						getResourceKey().dereference(),
						(RandomByteIOResourceFork)fork);
				} else if (fork instanceof StreamableByteIOResourceFork)
				{
					return new StreamableByteIOForkAttributePreFetcher(
						getResourceKey().dereference(),
						(StreamableByteIOResourceFork)fork);
				} else if (fork instanceof StreamableByteIOFactoryResourceFork)
				{
					return new StreamableByteIOFactoryForkAttributePreFetcher(
						getResourceKey().dereference(),
						(StreamableByteIOFactoryResourceFork)fork);
				}
			} else if (Container.onThisServer(epr))
			{
				return new DefaultGenesisIIAttributesPreFetcher<IResource>(epr);
			}
			
			return null;
		}
	}
}