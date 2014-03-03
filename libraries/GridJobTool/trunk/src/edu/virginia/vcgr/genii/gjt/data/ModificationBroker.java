package edu.virginia.vcgr.genii.gjt.data;

public class ModificationBroker extends BasicModifyable implements
		ModificationListener, Modifyable {
	@Override
	public void jobDescriptionModified() {
		fireJobDescriptionModified();
	}
}