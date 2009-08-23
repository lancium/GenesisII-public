package edu.virginia.vcgr.genii.container.dynpages;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

public class PageMap
{
	static final long serialVersionUID = 0L;
	
	static final private String PAGE_MAP_FILENAME = "page-map.dat";

	static final private Pattern LINE_PATTERN = Pattern.compile(
		"^([^=]+)=\\[([^\\]]+)\\](.*)$");
	
	private ClassLoader _loader;
	private String _baseResourcePath;
	private Map<String, PageDescription> _descriptions = 
		new HashMap<String, PageDescription>();
	
	public PageMap(ClassLoader loader, String baseResourcePath)
		throws IOException
	{
		_loader = loader;
		if (_loader == null)
			_loader = Thread.currentThread().getContextClassLoader();
		while (baseResourcePath.endsWith("/"))
			baseResourcePath = baseResourcePath.substring(0,
				baseResourcePath.length() - 1);
		_baseResourcePath = baseResourcePath;
		
		InputStream in = null;
		try
		{
			in = _loader.getResourceAsStream(String.format("%s/%s",
				_baseResourcePath, PAGE_MAP_FILENAME));
			if (in != null)
			{
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
				String line;
				
				while ( (line = reader.readLine()) != null)
				{
					int index = line.indexOf('#');
					if (index >= 0)
						line = line.substring(0, index);
					line = line.trim();
					if (line.length() == 0)
						continue;
					
					Matcher matcher = LINE_PATTERN.matcher(line);
					if (!matcher.matches())
						throw new IOException(String.format(
							"Unable to parse page map line \"%s\".", line));
					
					String pageName = matcher.group(1);
					String type = matcher.group(2);
					String resourceName = matcher.group(3);
					String resourcePath = String.format("%s/%s",
						_baseResourcePath, resourceName);
					if (type.equals("class"))
						_descriptions.put(pageName, 
							new ClassBasedPageDescription(_loader,
								resourcePath.replaceAll("/", ".")));
					else if (type.equals("static"))
						_descriptions.put(pageName,
							new StaticContentPageDescription(_loader,
								resourcePath));
				}
			}
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new IOException("Unable to find dynamic page class.", cnfe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	public DynamicPage loadPage(String pageName) throws IOException
	{
		if (pageName.equals(PAGE_MAP_FILENAME))
			throw new FileNotFoundException(
				"Not allowed to lookup page map file.");
		
		PageDescription desc = _descriptions.get(pageName);
		if (desc == null)
			desc = new StaticContentPageDescription(_loader,
				_baseResourcePath + "/" + pageName);
		
		return desc.loadPage();
	}
}