package edu.virginia.vcgr.genii.web.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.util.url.ApacheURLLister;
import org.morgan.util.io.StreamUtils;

/**
 * HttpFileGrabber: a handy utility for downloading files from web sites.
 * 
 * @author Chris Koeritz
 * 
 *         information about apache ivy thanks to the example at:
 *         http://www.java-forums.org/networking/15527-fetch-files-over-web-server.html
 */
public class HttpFileGrabber
{
	static private Log _logger = LogFactory.getLog(HttpFileGrabber.class);

	/**
	 * retrieves the set of files found at a URL, if any, and returns them as a list of URLs.
	 */
	static public Collection<URL> listRemoteFiles(String directoryURL)
	{
		URL url;
		try {
			url = new URL(directoryURL);
			ApacheURLLister lister = new ApacheURLLister();
			@SuppressWarnings("rawtypes")
			List files = lister.listFiles(url);
			ArrayList<URL> toReturn = new ArrayList<URL>();
			for (Object o : files) {
				toReturn.add((URL) o);
			}
			return toReturn;
		} catch (Exception e) {
			_logger.error("caught exception while retrieving file list: " + e.getMessage());
		}
		return null;
	}

	/**
	 * downloads the file specified by the URL and stores it in the destination folder.
	 */
	public static void downloadRemoteFile(URL url, File destination) throws Exception
	{
		File remote = new File(url.getFile());
		File target = new File(destination, remote.getName());
		target.getParentFile().mkdirs();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			URLConnection urlc = url.openConnection();
			bis = new BufferedInputStream(urlc.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(target.getPath()));
			StreamUtils.copyStream(bis, bos);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException ioe) {
					_logger.error("exception while closing input stream: " + ioe.getMessage());
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException ioe) {
					_logger.error("exception while closing output stream: " + ioe.getMessage());
				}
			}
		}
	}

	/* test app. */
	public static void main(String[] args)
	{
		String target = "http://gruntose.com/pics";
		Collection<URL> files = listRemoteFiles(target);
		_logger.info("found these files at " + target + ":" + files);

		File destination = new File("files-grabbed");
		destination.mkdir();

		Iterator<URL> iter = files.iterator();
		while (iter.hasNext()) {
			URL url = iter.next();
			try {
				downloadRemoteFile(url, destination);
			} catch (Exception e) {
				_logger.error("There was an exception while downloading files: " + e.getMessage());
			}
		}
		System.out.println("files were downloaded.");
	}
}
