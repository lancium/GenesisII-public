package edu.virginia.vcgr.appmgr.patch.par;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.patch.PatchRunnable;

public class ParFile
{
	static final public String CLASSES_SUBDIR_NAME = "classes";
	static final public String LIB_SUBDIR_NAME = "lib";

	private ClassLoader _loader = null;

	static private void addJarFiles(File targetDir, Collection<URL> urls) throws MalformedURLException
	{
		for (File entry : targetDir.listFiles()) {
			if (entry.isDirectory())
				addJarFiles(entry, urls);
			else if (entry.getName().endsWith(".jar"))
				urls.add(entry.toURI().toURL());
		}
	}

	static private ClassLoader createClassLoader(File targetDir) throws MalformedURLException
	{
		Collection<URL> urls = new LinkedList<URL>();
		urls.add(new File(targetDir, CLASSES_SUBDIR_NAME).toURI().toURL());

		File libDir = new File(targetDir, LIB_SUBDIR_NAME);
		if (libDir.exists() && libDir.isDirectory())
			addJarFiles(libDir, urls);

		return new URLClassLoader(urls.toArray(new URL[0]));
	}

	public ParFile(ApplicationDescription appDescription, InputStream parStream) throws IOException
	{
		File target = appDescription.getScratchSpaceManager().newDirectory();

		JarInputStream jis = null;
		try {
			jis = new JarInputStream(parStream);
			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null) {
				File entryTarget = new File(target, entry.getName());
				if (entry.isDirectory()) {
					if (!entryTarget.exists()) {
						if (!entryTarget.mkdirs())
							throw new IOException(String.format("Unable to make directory %s.\n", entryTarget));
					}
				} else {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(entryTarget);
						IOUtils.copy(jis, fos);
					} finally {
						IOUtils.close(fos);
					}
				}
			}

			_loader = createClassLoader(target);
		} finally {
			IOUtils.close(jis);
		}
	}

	@SuppressWarnings("unchecked")
	public void run(String className, Properties runProperties) throws Throwable
	{
		Class<?> cl = _loader.loadClass(className);
		if (!PatchRunnable.class.isAssignableFrom(cl))
			throw new RuntimeException(String.format("Class \"%s\" does not implement PatchRunnable interface.", className));

		Class<PatchRunnable> pCl = (Class<PatchRunnable>) cl;
		PatchRunnable pr = pCl.newInstance();
		pr.run(runProperties);
	}
}