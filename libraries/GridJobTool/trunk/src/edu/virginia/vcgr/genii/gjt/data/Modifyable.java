package edu.virginia.vcgr.genii.gjt.data;

public interface Modifyable {
	public void addModificationListener(ModificationListener listener);

	public void removeModificationListener(ModificationListener listener);
}