package edu.virginia.vcgr.genii.client.cmd.tools;

/*Enum that has all categories tools can be grouped in
 * this data should never be persistently stored or replied upon
 * to allow easy configuration/grouping changes in the future
 * mts5x
 */
public enum ToolCategory {
	
	DATA (false, "Data"),
	HELP (false, "Help"),
	SECURITY (false, "Security"),
	MISC (false, "Misc"),
	INTERNAL (false, "Internal Use"),
	EXECUTION(false, "Job/Execution"),
	ANTIQUATED(false, "Antiquated"),
	GENERAL(false, "General"),
	ADMINISTRATION(false, "Administration");
	
	
	private boolean _hidden;
	private String _desc;
	
	ToolCategory(boolean hiddenGroup, String description){
		_hidden = hiddenGroup;
		_desc = description;
	}
	
	public boolean isHidden(){
		return _hidden;
	}
	
	public String getDescription(){
		return _desc;
	}
	
}
