package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNICpTool extends JNILibraryBase {
	static private final int _BLOCK_SIZE = 
		ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE;
	
	public static Boolean copy(String source, String destination, 
			Boolean srcLocal, Boolean dstLocal){		
		try{
			tryToInitialize();
			copy(source, srcLocal, destination, dstLocal);			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void copy(InputStream in, OutputStream out)
		throws IOException
	{
		byte []data = new byte[_BLOCK_SIZE];
		int r;
		
		while ( (r = in.read(data)) >= 0)
		{
			out.write(data, 0, r);
		}
	}
	
	public static void copy(String sourcePath, boolean isLocalSource,
		String targetPath, boolean isLocalTarget)
	throws ConfigurationException, FileNotFoundException, IOException,
		RNSException
	{
		String sourceName = null;
		OutputStream out = null;
		InputStream in = null;
		
		RNSPath current = RNSPath.getCurrent();
		
		try
		{
			if (isLocalSource)
			{
				in = new FileInputStream(sourcePath);
				File sourceFile = new File(sourcePath);
				sourceName = sourceFile.getName();
			} else
			{
				RNSPath path = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
				in = ByteIOStreamFactory.createInputStream(path);
				int index = sourcePath.lastIndexOf('/');
				if (index >= 0)
					sourceName = sourcePath.substring(index + 1);
				else
					sourceName = sourcePath;
			}
			
			if (isLocalTarget)
			{
				File targetFile = new File(targetPath);
				if (targetFile.exists() && targetFile.isDirectory())
					targetFile = new File(targetFile, sourceName);
				out = new FileOutputStream(targetFile);
			} else
			{
				RNSPath path = current.lookup(targetPath, RNSPathQueryFlags.DONT_CARE);
				if (path.exists() && path.isDirectory())
					path = path.lookup(sourceName, RNSPathQueryFlags.DONT_CARE);
				out = ByteIOStreamFactory.createOutputStream(path);
			}
			
			copy(in, out);
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
		catch (RNSMultiLookupResultException e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}
}
