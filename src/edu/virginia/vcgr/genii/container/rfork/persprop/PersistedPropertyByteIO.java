package edu.virginia.vcgr.genii.container.rfork.persprop;

import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sd.SimpleStateResourceFork;
import edu.virginia.vcgr.genii.container.rfork.sd.StateDescription;
import edu.virginia.vcgr.genii.container.rfork.sd.TextStateTranslator;

@StateDescription(value = TextStateTranslator.class,
	readable = true, writable = true)
public class PersistedPropertyByteIO extends SimpleStateResourceFork<String>
{
	static private final Pattern EXTRACTOR = Pattern.compile(
		"^.*/([^/]+)/([^/]+)$");
	
	private void extract(StringBuilder category, StringBuilder property)
		throws FileNotFoundException
	{
		Matcher matcher = EXTRACTOR.matcher(getForkPath());
		if (!matcher.matches())
			throw new FileNotFoundException(String.format(
				"Unable to understand persistent property path \"%s\".",
				getForkPath()));
		
		category.append(matcher.group(1));
		property.append(matcher.group(2));
	}
	
	public PersistedPropertyByteIO(ResourceForkService service,
		String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	protected String get() throws Throwable
	{
		StringBuilder category = new StringBuilder();
		StringBuilder property = new StringBuilder();
		
		extract(category, property);
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		Properties props = resource.getPersistedProperties(
			category.toString());
		if (props == null)
			throw new FileNotFoundException(String.format(
				"Unable to find persistent property category \"%s\".", 
				category));
		String ret = props.getProperty(property.toString());
		if (ret == null)
			throw new FileNotFoundException(String.format(
				"Unable to find persistent property %s in category %s.",
				property, category));
		
		return ret;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	protected void set(String state) throws Throwable
	{
		StringBuilder category = new StringBuilder();
		StringBuilder property = new StringBuilder();
		
		extract(category, property);
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.replacePersistedProperty(category.toString(),
			property.toString(), state);
	}
}