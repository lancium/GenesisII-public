package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.Date;

public class PollingTimeSettings {
	
	private String containerId;
	private long pollingRPCInterval;
	private Date nextPollingTime;

	// This represent the last time a polling has been done on the container.
	private Date mostRecentPollingTime;
	
	public PollingTimeSettings(String containerId) {
		this.containerId = containerId;
		pollingRPCInterval = PollingIntervalDirectory.DEFAULT_POLLING_INTERVAL;
		nextPollingTime = new Date();
	}

	public String getContainerId() {
		return containerId;
	}
	
	public long getPollingRPCInterval() {
		return pollingRPCInterval;
	}
	
	public void setPollingRPCInterval(long pollingRPCInterval) {
		this.pollingRPCInterval = pollingRPCInterval;
	}
	
	public Date getNextPollingTime() {
		return nextPollingTime;
	}

	public void setNextPollingTime(Date nextPollingTime) {
		this.nextPollingTime = nextPollingTime;
	}
	
	public void updateNextPollingTime() {
		
		long nextPollingTimeInMillis;
		
		if (mostRecentPollingTime == null) {
			nextPollingTimeInMillis = System.currentTimeMillis() + pollingRPCInterval;
		} else {
			nextPollingTimeInMillis = mostRecentPollingTime.getTime() + pollingRPCInterval;
		}
		nextPollingTime = (nextPollingTimeInMillis < System.currentTimeMillis()) ? 
				new Date() : new Date(nextPollingTimeInMillis);
	}
	
	public void notifyAboutPolling() {
		nextPollingTime = new Date(System.currentTimeMillis() + pollingRPCInterval);
		mostRecentPollingTime = new Date();
	}

	public Date getMostRecentPollingTime() {
		return mostRecentPollingTime;
	}

	public void setMostRecentPollingTime(Date mostRecentPollingTime) {
		this.mostRecentPollingTime = mostRecentPollingTime;
	}
}
