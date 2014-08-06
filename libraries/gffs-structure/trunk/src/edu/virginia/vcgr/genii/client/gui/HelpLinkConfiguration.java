package edu.virginia.vcgr.genii.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.morgan.util.io.StreamUtils;

public class HelpLinkConfiguration
{
	public static final String MAIN_HELP = "main.help";
	public static final String GENERAL_EXPORT_HELP = "general.export.help";
	public static final String EXPORT_CREATION_HELP = "export.creation.help";
	public static final String GENERAL_DIRECTORY_HELP = "general.directory.help";
	public static final String GENERAL_FILE_HELP = "general.file.help";
	public static final String GENERAL_SECURITY_HELP = "general.security.help";
	public static final String CREATE_DIRECTORY_HELP = "general.directory.help";
	public static final String GENERAL_STORAGE_HELP = "general.storage.help";
	public static final String JOB_CREATE_HELP = "job.create.help";

	private static Properties p = null;

	public static String get_help_url(String help_ptr)
	{
		if (p == null) {
			p = new Properties();
			InputStream in = HelpLinkConfiguration.class.getClassLoader().getResourceAsStream("config/help-links.properties");
			try {
				p.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				StreamUtils.close(in);
			}
		}
		String r = p.getProperty(help_ptr);
		if (r == null) {
			throw new RuntimeException("Could not find help link " + help_ptr);
		}
		return r;
	}

}
