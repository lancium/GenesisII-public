package edu.virginia.vcgr.genii.client.informationService;



public class GUIInternalStruct {
	private String OSTypeValue = "";
	private String OSVersionValue = "";
	private String CPUArchitectureNameValue = "";
	private String CPUCountValue = "";
	private String CPUSpeedValue ="";
	private String physicalMemoryValue= "";
	private String virtualMemoryValue= "";
	private String commonNameValue= "";
	private String totalNumberOfActivitiesValue= "";
	private String localResourceManagerValue= "";
	private String namingProfileValue= "";
	private boolean isAcceptingNewActivitiesValue =false;
	
	public GUIInternalStruct(){
	}
	
	public GUIInternalStruct (String OSTypeValue, String OSVersionValue,
			String CPUArchitectureNameValue, String CPUCountValue, String CPUSpeedValue,
			String physicalMemoryValue, String virtualMemoryValue, String commonNameValue,
			String totalNumberOfActivitiesValue, String localResourceManagerValue,
			String namingProfileValue, boolean isAcceptingNewActivitiesvalue){
		this.OSTypeValue = OSTypeValue;
		this.OSVersionValue = OSVersionValue;
		this.CPUArchitectureNameValue = CPUArchitectureNameValue;
		this.CPUCountValue = CPUCountValue;
		this.CPUSpeedValue = CPUSpeedValue;
		this.physicalMemoryValue = physicalMemoryValue;
		this.virtualMemoryValue = virtualMemoryValue;
		this.commonNameValue = commonNameValue;
		this.totalNumberOfActivitiesValue = totalNumberOfActivitiesValue;
		this.localResourceManagerValue = localResourceManagerValue;
		this.namingProfileValue = namingProfileValue;
		this.isAcceptingNewActivitiesValue = isAcceptingNewActivitiesvalue;	
	}
	
	public String getOSTypeValue (){
		return OSTypeValue;
	}
	
	public String getOSVersionValue (){
		return OSVersionValue;
	}
	
	public String getCPUArchitectureNameValue(){
		return CPUArchitectureNameValue;
	}
	
	public String getCPUCountValue(){
		return CPUCountValue;
	}
	
	public String getCPUSpeedValue() {
		return CPUSpeedValue;
	}
	
	public String getPhysicalMemoryValue() {
		return physicalMemoryValue;
	}
	
	public String getVirtualMemoryValue(){
		return virtualMemoryValue;
	}

	public String getCommonNameValue(){
		return commonNameValue;
	}
	
	public String getTotalNumberOfActivitiesValue(){
		return totalNumberOfActivitiesValue;
	}
	
	public String getLocalResourcemanagerValue(){
		return localResourceManagerValue;
	}

	public String getNamingProfilevalue(){
		return namingProfileValue;
	}
	
	public boolean getIsAcceptingNewActivitiesValue(){
		return this.isAcceptingNewActivitiesValue;
	}
	
	public void setOSTypevalue (String OSTypeValue){
		this.OSTypeValue = OSTypeValue;
	}
	
	public void setOSVersionValue (String OSVersionValue){
		this.OSVersionValue = OSVersionValue;
	}
	
	public void setCPUArchitectureNameValue (String CPUArchitectureNameValue){
		this.CPUArchitectureNameValue = CPUArchitectureNameValue;
	}
	
	public void setCPUCountValue (String CPUCountValue){
		this.CPUCountValue = CPUCountValue;
	}
	
	public void setCPUSpeedValue (String CPUSpeedvalue){
		this.CPUSpeedValue = CPUSpeedvalue;
	}
	
	public void setPhysicalMemoryValue (String physicalMemoryValue){
		this.physicalMemoryValue = physicalMemoryValue;
	}
	
	public void setVirtualMemoryValue (String virtualMemoryValue){
		this.virtualMemoryValue = virtualMemoryValue;
	}
	
	public void setCommonNameValue (String commonNameValue){
		this.commonNameValue = commonNameValue;
	}
	
	public void setTotalNumberOfActivitiesValue (String totalNumberOfActivities){
		this.totalNumberOfActivitiesValue = totalNumberOfActivities;
	}
	
	public void setLocalResourceManagervalue (String localResourceManager){
		this.localResourceManagerValue = localResourceManager;
	}
	
	public void setNamingProfileValue (String namingProfileValue){
		this.namingProfileValue = namingProfileValue;
	}
	
	public void setIsAcceptingNewActivitiesValue (boolean isAcceptingNewActivities){
		this.isAcceptingNewActivitiesValue = isAcceptingNewActivities;
	}
}
