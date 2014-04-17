package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.File;
import java.io.IOException;

public class SudoExportedDirUtils
{

	public static boolean dirReadable(String path) throws IOException
	{
		if (path == null) {
			return false;
		}

		File target = new File(path);

		String uname = SudoExportUtils.getLocalUser();

		if (SudoDiskExportEntry.doesExist(target, uname) && SudoDiskExportEntry.isDir(target, uname)
			&& SudoDiskExportEntry.canRead(path, uname)) {
			return true;
		}

		return false;
	}

}
