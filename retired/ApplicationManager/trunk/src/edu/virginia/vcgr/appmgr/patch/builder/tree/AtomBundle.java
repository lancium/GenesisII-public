package edu.virginia.vcgr.appmgr.patch.builder.tree;

import edu.virginia.vcgr.appmgr.patch.builder.PatchAtom;

public class AtomBundle
{
	private String _relativePath;
	private PatchAtom _atom;

	public AtomBundle(String relativePath, PatchAtom atom)
	{
		_relativePath = relativePath;
		_atom = atom;
	}

	public PatchAtom getAtom()
	{
		return _atom;
	}

	@Override
	public String toString()
	{
		return _relativePath;
	}
}