package edu.virginia.vcgr.genii.client.gui;

import edu.virginia.vcgr.genii.client.ClientProperties;

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

	public static String get_help_url(String helpPointer)
	{
		String r = ClientProperties.getClientProperties().getHelpFileProperty(helpPointer);
		if (r == null) {
			throw new RuntimeException("Could not find help link for: " + helpPointer);
		}
		return r;
	}

}
