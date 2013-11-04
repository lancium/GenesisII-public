package edu.virginia.vcgr.genii.web.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.ivy.util.url.ApacheURLLister;

public class FileGrabber
{

	static public Collection<URL> listRemoteFiles(String directoryURL)
	{
		URL url;
		try {
			url = new URL(directoryURL);
			ApacheURLLister lister = new ApacheURLLister();
			@SuppressWarnings("unchecked")
			List<URL> files = (List<URL>) lister.listAll(url);
			return files;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void httpFileDownload(URL url, File destFolder) throws Exception
	{
		File destination = new File(destFolder, url.getFile());
		destination.getParentFile().mkdirs();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			URLConnection urlc = url.openConnection();

			bis = new BufferedInputStream(urlc.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(destination.getPath()));

			int i;
			while ((i = bis.read()) != -1) {
				bos.write(i);
			}
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
		}
	}

	public static void main(String[] args)
	{
		String targetURL = "http://gruntose.com";
		Collection<URL> files = listRemoteFiles(targetURL);

		File destFolder = new File("test");

		System.out.println("list file is complete.." + files);
		for (Iterator<URL> iter = files.iterator(); iter.hasNext();) {
			URL fileUrl = (URL) iter.next();
			try {
				httpFileDownload(fileUrl, destFolder);
			} catch (Exception e) {
				// hmmm Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("download is complete..");
	}
}
