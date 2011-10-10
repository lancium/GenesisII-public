package edu.virginia.vcgr.genii.container.resource;

import java.io.Closeable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.common.MatchingParameter;

/**
 * A resource is the persistent state representation of a web service endpoint.
 * This interface represents the minimum amount of functionallity that a resource
 * MUST implement, but it is expected that web service implementators will expand
 * on this list to better suit the web services that they write.
 * 
 * @author Mark Morgan (mmm2a@cs.virginia.edu)
 */
public interface IResource extends Closeable
{
	static public final String STORED_CALLING_CONTEXT_PROPERTY_NAME =
		"genesisII.resource.property.stored-calling-context";
	static public final String CERTIFICATE_CHAIN_PROPERTY_NAME =
		"genesisII.resource.property.certificate-chain";
	static public final String ENDPOINT_IDENTIFIER_PROPERTY_NAME =
		"genesisII.resource.property.endpoint-identifier";
	static public final String SCHEDULED_TERMINATION_TIME_PROPERTY_NAME =
		"genesisII.resource.property.sched-term-time";
	static public final String TERM_TIME_ALARM =
		"genesisII.resource.term-time-alarm";
	static public final String CACHE_COHERENCE_WINDOW_PROPERTY =
		"genesisII.resource.cache-coherence-window";

	
	static public final QName CERTIFICATE_CHAIN_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "certificate-chain");
	static public final QName SERVICE_CERTIFICATE_CHAIN_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "service-certificate-chain");
	static public QName CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "certificate-creation-spec");
	static public QName ADDITIONAL_CNS_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "additional-CNs-creation-spec");
	static public QName ADDITIONAL_ORGS_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "additional-Os-creation-spec");
	static public QName ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "endpoint-identifier");
	static public QName IS_SERVICE_CONSTRUCTION_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "is-service");
	
	/**
	 * Initialize the resource (construct a new one) using the given construction
	 * parameters.
	 * 
	 * @param constructionParams Parameters that represent initial state for this
	 * resource.
	 * @throws ResourceException If anything goes wrong.
	 */
	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException;
	
	/**
	 * Load a resources state from persistent storage (or do nothing if no
	 * loading needs to take place).
	 * 
	 * @param refParams The refParams that identifies this resource.  The type can be of any
	 * type as long as it matches the IResourceKeyTranslater being used.
	 * @throws ResourceException If anything goes wrong.
	 */
	public void load(String resourceKey) 
		throws ResourceUnknownFaultType, ResourceException;
	
	/**
	 * Retrieve the internal key that represents this resource.  This method is
	 * generally to be used by the provider and ResourceKey and is not of
	 * interest to users in general.
	 * 
	 * @return The internal key representation for this resource.
	 */
	public String getKey();
	
	/**
	 * This method returns a key that can be used for locking the resource.  It can be,
	 * but doesn't have to be the same as the resource key.  As long as it is unique
	 * within a given virtual machine.
	 * 
	 * @return The lock key object.
	 */
	public Object getLockKey();
	
	public ResourceKey getParentResourceKey();
	
	/**
	 * Retrieve the value of a stored property.
	 * 
	 * @param propertyName The name of the property to retrieve.
	 * @return The value of the named property.
	 * @throws ResourceException If anything goes wrong.
	 */
	public Object getProperty(String propertyName) 
		throws ResourceException;
	
	/**
	 * Set the value of a stored property.
	 * 
	 * @param propertyName The name of the value to set.
	 * @param value The value for the property (which must be java.io.Serializable).
	 * @throws ResourceException If anything goes wrong.
	 */
	public void setProperty(String propertyName, Object value) 
		throws ResourceException;
	
	public ConstructionParameters constructionParameters(Class<?> serviceClass) throws ResourceException;
	public void constructionParameters(ConstructionParameters parameters) throws ResourceException;
	
	/**
	 * Destroy all state associated with this resource.
	 * 
	 * @throws ResourceException If anything goes wrong.
	 */
	public void destroy() throws ResourceException;
	
	/**
	 * Commits all changes made, so far, to this resource.
	 * 
	 * @throws ResourceException If anything goes wrong and the changes can't be committed.
	 */
	public void commit() throws ResourceException;
	
	/**
	 * Rolls back all changes made so far to this resource.
	 */
	public void rollback();
	
	/**
	 * Return whether or not the resource is a service resource
	 */
	public boolean isServiceResource();
	
	public Collection<MatchingParameter> getMatchingParameters()
		throws ResourceException;
	public void addMatchingParameter(MatchingParameter...parameters)
		throws ResourceException;
	public void removeMatchingParameter(MatchingParameter...parameters)
		throws ResourceException;
	
	public Calendar createTime() throws ResourceException;
	
	public Collection<MessageElement> getUnknownAttributes()
		throws ResourceException;
	public void setUnknownAttributes(
		Map<QName, Collection<MessageElement>> newAttrs)
			throws ResourceException;
	public void deleteUnknownAttributes(Set<QName> names) 
		throws ResourceException;
}