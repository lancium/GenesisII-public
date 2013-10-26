package edu.virginia.vcgr.appmgr.patch.builder.diffengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.patch.builder.PatchRC;

public class FileComparator
{
	static private Log _logger = LogFactory.getLog(FileComparator.class);

	static private Set<String> _IGNORE_SET = new HashSet<String>();

	static {
		_IGNORE_SET.add(".svn");
		_IGNORE_SET.add("updates");
	}

	static private void compareDirectories(PatchRC rc, File one, File two, DirCompContext context, String relativePath)
		throws IOException
	{
		Set<String> entrySet = new HashSet<String>();

		for (String entry : one.list()) {
			entrySet.add(entry);
		}

		for (String entry : two.list()) {
			entrySet.add(entry);
		}

		for (String entry : entrySet) {
			if (_IGNORE_SET.contains(entry))
				continue;

			String newRelativePath = String.format("%s%s%s", relativePath, File.separator, entry);
			compareEntries(rc, context, newRelativePath);
		}
	}

	static private boolean compare(byte[] data, int length, FileInputStream stream) throws IOException
	{
		int offset = 0;
		int read;
		byte[] otherData = new byte[length];

		while (offset < length) {
			read = stream.read(otherData, 0, (length - offset));
			if (read <= 0)
				throw new IOException("Couldn't completely read file.");

			for (int lcv = 0; lcv < read; lcv++) {
				if (data[offset + lcv] != otherData[lcv])
					return false;
			}

			offset += read;
		}

		return true;
	}

	static private void compareFiles(File one, File two, DirCompContext context, String relativePath) throws IOException
	{
		if (one.length() != two.length()) {
			context.handler().fileModified(context, relativePath);
			return;
		}

		int read;
		byte[] data = new byte[1024];

		FileInputStream fin1 = null;
		FileInputStream fin2 = null;

		try {
			fin1 = new FileInputStream(one);
			fin2 = new FileInputStream(two);

			while ((read = fin1.read(data)) > 0) {
				if (!compare(data, read, fin2)) {
					context.handler().fileModified(context, relativePath);
					break;
				}
			}
		} finally {
			if (fin1 != null)
				try {
					fin1.close();
				} catch (Throwable cause) {
				}
			if (fin2 != null)
				try {
					fin2.close();
				} catch (Throwable cause) {
				}
		}
	}

	static private void compareExistingEntries(PatchRC rc, File one, File two, DirCompContext context, String relativePath)
		throws IOException
	{
		if (one.isDirectory()) {
			if (two.isDirectory()) {
				compareDirectories(rc, one, two, context, relativePath);
			} else {
				context.handler().directoryChangedToFile(context, relativePath);
			}
		} else {
			if (two.isDirectory()) {
				context.handler().fileChangedToDirectory(context, relativePath);
			} else {
				compareFiles(one, two, context, relativePath);
			}
		}
	}

	static public void compareEntries(PatchRC rc, DirCompContext context, String relativePath) throws IOException
	{
		context.handler().analyzing(context, relativePath);

		if (rc.isIgnore(relativePath))
			return;

		FileTuple tuple = context.getRelativeFile(relativePath);
		File one = tuple.one();
		File two = tuple.two();

		if (one.exists()) {
			if (two.exists()) {
				compareExistingEntries(rc, one, two, context, relativePath);
			} else {
				if (one.isDirectory()) {
					context.handler().directoryRemoved(context, relativePath);
				} else {
					context.handler().fileDeleted(context, relativePath);
				}
			}
		} else {
			if (two.exists()) {
				if (two.isDirectory()) {
					context.handler().directoryAdded(context, relativePath);
					for (String entry : two.list())
						compareEntries(rc, context, String.format("%s/%s", relativePath, entry));
				} else {
					context.handler().fileAdded(context, relativePath);
				}
			} else {
				// This shouldn't have happened
				_logger.error(String.format("Unknown error!  Found an entry which actually does "
					+ "not exist in either target.  Entry was \"%s\".\n", relativePath));
				System.exit(1);
			}
		}
	}
}