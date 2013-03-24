package edu.virginia.vcgr.externalapp;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public abstract class EditableFile implements Closeable
{
	private boolean _closed = false;

	protected void doClose() throws IOException
	{
		// By default, nothing to do.
	}

	final protected void finalize() throws Exception
	{
		close();
	}

	protected abstract boolean wasModified();

	public abstract File file();

	@Override
	final public void close() throws IOException
	{
		boolean doClose = false;

		synchronized (this) {
			if (!_closed) {
				_closed = true;
				doClose = true;
			}
		}

		if (doClose)
			doClose();
	}

	static private class LocalEditableFile extends EditableFile
	{
		private File _file;
		private long _originalModificationDate;

		private LocalEditableFile(File file)
		{
			if (!file.exists())
				_originalModificationDate = -1L;
			else
				_originalModificationDate = file.lastModified();

			_file = file;
		}

		@Override
		final protected boolean wasModified()
		{
			return _file.exists() && _file.lastModified() > _originalModificationDate;
		}

		@Override
		final public File file()
		{
			return _file;
		}
	}

	static private class GridEditableFile extends LocalEditableFile
	{
		private RNSPath _originalPath;

		static private File downloadFile(RNSPath path) throws IOException
		{
			String filename = path.getName();
			String extension;

			int index = filename.lastIndexOf('.');
			if (index >= 0)
				extension = filename.substring(index);
			else
				extension = null;

			File ret = File.createTempFile("gedit", extension);
			ret.deleteOnExit();

			File tmp = ret;
			InputStream in = null;
			OutputStream out = null;

			try {
				if (path.exists()) {
					in = ByteIOStreamFactory.createInputStream(path);
					out = new FileOutputStream(tmp);

					StreamUtils.copyStream(in, out);

					in.close();
					in = null;
					out.close();
					out = null;
				}

				tmp = null;
				return ret;
			} catch (RNSException rne) {
				throw new IOException(String.format("Unable to copy grid file %s to temporary file.", path));
			} finally {
				StreamUtils.close(in);
				StreamUtils.close(out);

				if (tmp != null)
					tmp.delete();
			}
		}

		static private void uploadFile(File source, RNSPath target) throws IOException
		{
			InputStream in = null;
			OutputStream out = null;

			try {
				in = new FileInputStream(source);
				out = ByteIOStreamFactory.createOutputStream(target);

				StreamUtils.copyStream(in, out);
			} catch (RNSException e) {
				throw new IOException(String.format("Unable to upload temporary file to %s.", target));
			} finally {
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}

		private GridEditableFile(RNSPath originalPath) throws FileNotFoundException, IOException
		{
			super(downloadFile(originalPath));

			_originalPath = originalPath;
		}

		@Override
		protected void doClose() throws IOException
		{
			try {
				if (wasModified())
					uploadFile(file(), _originalPath);
			} finally {
				super.doClose();
			}
		}
	}

	static public EditableFile createEditableFile(String path) throws FileNotFoundException, IOException
	{
		return createEditableFile(new GeniiPath(path));
	}

	static public EditableFile createEditableFile(GeniiPath path) throws FileNotFoundException, IOException
	{
		if (path.pathType() == GeniiPathType.Grid) {
			RNSPath rnsPath = RNSPath.getCurrent().lookup(path.path());
			return createEditableFile(rnsPath);
		} else
			return new LocalEditableFile(new File(path.path()));
	}

	static public EditableFile createEditableFile(RNSPath file) throws FileNotFoundException, IOException
	{
		return new GridEditableFile(file);
	}
}