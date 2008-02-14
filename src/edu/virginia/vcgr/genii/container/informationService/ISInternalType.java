package edu.virginia.vcgr.genii.container.informationService;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;


public class ISInternalType {
	private String _resourceKey;
	private EndpointReferenceType _resourceEndpoint;
	private ICallingContext _callingContext;

	
	public ISInternalType (){
	}
	
	public ISInternalType(String resourceKey, EndpointReferenceType resourceEndpoint, ICallingContext callingContext){
		this._resourceKey = resourceKey;
		this._resourceEndpoint = resourceEndpoint;
		this._callingContext = callingContext;
		
	}

	/**
     * Gets the resourceKey value for this ISInternalType.
     * 
     * @return resourceEndpoint
     */
    public String getResourceKey() {
        return _resourceKey;
    }
	 /**
     * Gets the resourceEndpoint value for this ISInternalType.
     * 
     * @return resourceEndpoint
     */
    public EndpointReferenceType getResourceEndpoint() {
        return _resourceEndpoint;
    }
    
    /**
     * Gets the callingContext value for this ISInternalType.
     * 
     * @return resourceEndpoint
     */
    public ICallingContext getCallingContext() {
        return _callingContext;
    }
    
    /**
     * Sets the resourceKey value for this ISInternalType.
     * 
     * @param resourceKey
     */
    public void String (String resourceKey) {
        this._resourceKey = resourceKey;
    }
    
    /**
     * Sets the resourceEndpoint value for this ISInternalType.
     * 
     * @param resourceEndpoint
     */
    public void setResourceEndpoint (EndpointReferenceType resourceEndpoint) {
        this._resourceEndpoint = resourceEndpoint;
    }
    
    /**
     * Sets the callingContext value for this ISInternalType.
     * 
     * @param callingContext
     */
    public void setCallingContext (ICallingContext callingContext) {
        this._callingContext = callingContext;
    }
}
