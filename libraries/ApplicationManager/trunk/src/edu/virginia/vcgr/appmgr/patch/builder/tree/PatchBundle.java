package edu.virginia.vcgr.appmgr.patch.builder.tree;

import edu.virginia.vcgr.appmgr.patch.PatchRestrictions;

public class PatchBundle
{
	private String _patchName;
	private PatchRestrictions _restrictions = new PatchRestrictions();

	public PatchBundle(String patchName)
	{
		_patchName = patchName;
	}

	public PatchRestrictions getRestrictions()
	{
		return _restrictions;
	}

	public void setRestrictions(PatchRestrictions restrictions)
	{
		_restrictions = restrictions;
	}

	@Override
	public String toString()
	{
		return _patchName;
	}
}