package edu.virginia.vcgr.genii.wsdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WsdlSourcePath
{
	static private Pattern _URL_PATTERN = Pattern.compile(
		"^https?://.*$", Pattern.CASE_INSENSITIVE);
	
	private File _filePath = null;
	private URL _urlPath = null;
	
	public WsdlSourcePath(File filePath)
	{
		_filePath = filePath;
	}
	
	public WsdlSourcePath(URL urlPath)
	{
		_urlPath = urlPath;
	}
	
	public WsdlSourcePath(String path) throws WsdlException
	{
		try
		{
			Matcher matcher = _URL_PATTERN.matcher(path);
			if (matcher.matches())
				_urlPath = new URL(path);
			else
				_filePath = new File(path);
		}
		catch (MalformedURLException mue)
		{
			throw new WsdlException(mue.getLocalizedMessage(), mue);
		}
	}
	
	public WsdlSourcePath()
	{
		_filePath = new File(".");
	}
	
	public InputStream open() throws IOException
	{
		if (_filePath != null)
			return new FileInputStream(_filePath);
		else if (_urlPath != null)
			return _urlPath.openStream();
		else
			throw new IOException("WsdlSourcePath not specified.");
	}
	
	public WsdlSourcePath createRelative(String relativePath)
		throws WsdlException
	{
		try
		{
			Matcher matcher = _URL_PATTERN.matcher(relativePath);
			if (matcher.matches())
				return new WsdlSourcePath(new URL(relativePath));
			else
			{
				if (_urlPath != null)
					return new WsdlSourcePath(new URL(_urlPath, relativePath));
				
				return new WsdlSourcePath(
					new File(_filePath.getParentFile(), relativePath));
			}
		}
		catch (MalformedURLException mue)
		{
			throw new WsdlException(mue.getLocalizedMessage(), mue);
		}
	}
	
	public String getPath()
	{
		if (_filePath != null)
			return "./" + _filePath.getName();
		else
			return _urlPath.toString();
	}
}