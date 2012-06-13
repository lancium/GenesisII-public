package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

/*
 * To avoid having a notifications from the container when we are not interested in the 
 * resource anymore, we should delete the subscriptions from the database. Since we are
 * doing all subscriptions through a notification broker, we can just hold onto the EPIs
 * of the subscriptions and ask the broker delete them using the EPIs.
 * */
public class SubscriptionReferenceList {

	private String rnsContentChangeReference;
	private String byteIOAttributesUpdateReference;
	private String permissionsBitsChangeReference;
	
	public String getRnsContentChangeReference() {
		return rnsContentChangeReference;
	}
	
	public void setRnsContentChangeReference(String rnsContentChangeReference) {
		this.rnsContentChangeReference = rnsContentChangeReference;
	}
	
	public String getByteIOAttributesUpdateReference() {
		return byteIOAttributesUpdateReference;
	}
	
	public void setByteIOAttributesUpdateReference(String byteIOAttributesUpdateReference) {
		this.byteIOAttributesUpdateReference = byteIOAttributesUpdateReference;
	}
	
	public String getPermissionsBitsChangeReference() {
		return permissionsBitsChangeReference;
	}

	public void setPermissionsBitsChangeReference(String permissionsBitsChangeReference) {
		this.permissionsBitsChangeReference = permissionsBitsChangeReference;
	}
}