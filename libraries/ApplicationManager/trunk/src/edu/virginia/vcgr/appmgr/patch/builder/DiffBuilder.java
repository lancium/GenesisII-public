package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.appmgr.patch.builder.diffengine.DifferenceHandler;
import edu.virginia.vcgr.appmgr.patch.builder.diffengine.DirCompContext;
import edu.virginia.vcgr.appmgr.patch.builder.diffengine.FileComparator;

public class DiffBuilder
{
	static private class DifferenceHandlerImpl implements DifferenceHandler
	{
		private Map<String, PatchAtom> _atoms;

		private DifferenceHandlerImpl(File newVersionDirectory, Map<String, PatchAtom> atoms)
		{
			_atoms = atoms;
		}

		private String sanitizePath(String path)
		{
			if (path.startsWith("./"))
				return path.substring(2);
			return path;
		}

		public void analyzing(DirCompContext context, String relativePath)
		{
			/*
			 * File target = new File(_newVersionDirectory, relativePath); if (target.isDirectory())
			 * PatchBuilder.addSplashScreenText(String.format( "Analyzing %s.",
			 * sanitizePath(relativePath)));
			 */
		}

		@Override
		public void directoryAdded(DirCompContext context, String relativePath)
		{
			// We can't handle this situation out-right
		}

		@Override
		public void directoryChangedToFile(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new DeleteWriteAtom(relativePath));
		}

		@Override
		public void directoryRemoved(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new DeleteAtom(relativePath));
		}

		@Override
		public void fileAdded(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new WriteAtom(relativePath));
		}

		@Override
		public void fileChangedToDirectory(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new DeleteAtom(relativePath));
		}

		@Override
		public void fileDeleted(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new DeleteAtom(relativePath));
		}

		@Override
		public void fileModified(DirCompContext context, String relativePath)
		{
			relativePath = sanitizePath(relativePath);
			_atoms.put(relativePath, new WriteAtom(relativePath));
		}
	}

	static public Map<String, PatchAtom> buildDiffs(PatchRC rc, File oldTarget, File newTarget) throws IOException
	{
		Map<String, PatchAtom> ret = new HashMap<String, PatchAtom>();

		DifferenceHandler diffHandler = new DifferenceHandlerImpl(newTarget, ret);
		DirCompContext context = new DirCompContext(diffHandler, oldTarget, newTarget);
		FileComparator.compareEntries(rc, context, ".");
		return ret;
	}
}