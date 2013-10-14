package edu.virginia.vcgr.appmgr.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class VerifiableJarFile extends JarFile
{
	private Verifier _verifier;

	private void initialize(Verifier verifier)
	{
		if (verifier == null)
			throw new IllegalArgumentException("Verifier parameter cannot be null.");

		_verifier = verifier;
	}

	private void verify(JarEntry entry)
	{
		_verifier.verify(entry.getName(), (Object[]) entry.getCertificates());
	}

	public VerifiableJarFile(Verifier verifier, File file, int mode) throws IOException
	{
		super(file, true, mode);

		initialize(verifier);
	}

	public VerifiableJarFile(Verifier verifier, File file) throws IOException
	{
		super(file, true);

		initialize(verifier);
	}

	public VerifiableJarFile(Verifier verifier, String name) throws IOException
	{
		super(name, true);

		initialize(verifier);
	}

	@Override
	public InputStream getInputStream(ZipEntry ze) throws IOException
	{
		InputStream ret = super.getInputStream(ze);

		if (ze instanceof JarEntry)
			ret = new VerifiableInputStream(ret, (JarEntry) ze);

		return ret;
	}

	private class VerifiableInputStream extends InputStream
	{
		private boolean _verificationChecked = false;
		private JarEntry _entry;
		private InputStream _in;

		synchronized final private void verify()
		{
			if (!_verificationChecked) {
				_verificationChecked = true;
				VerifiableJarFile.this.verify(_entry);
			}
		}

		private VerifiableInputStream(InputStream in, JarEntry entry)
		{
			_in = in;
			_entry = entry;
		}

		@Override
		public int available() throws IOException
		{
			return _in.available();
		}

		@Override
		public void close() throws IOException
		{
			try {
				verify();
			} finally {
				_in.close();
			}
		}

		@Override
		public void mark(int readlimit)
		{
			_in.mark(readlimit);
		}

		@Override
		public boolean markSupported()
		{
			return _in.markSupported();
		}

		@Override
		public int read() throws IOException
		{
			int result = _in.read();
			if (result < 0)
				verify();
			return result;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			int result = _in.read(b, off, len);
			if (result < 0)
				verify();
			return result;
		}

		@Override
		public int read(byte[] b) throws IOException
		{
			int result = _in.read(b);
			if (result < 0)
				verify();
			return result;
		}

		@Override
		public void reset() throws IOException
		{
			_in.reset();
		}

		@Override
		public long skip(long n) throws IOException
		{
			return _in.skip(n);
		}
	}
}