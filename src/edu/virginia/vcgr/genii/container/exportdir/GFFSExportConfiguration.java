package edu.virginia.vcgr.genii.container.exportdir;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.morgan.util.io.StreamUtils;

public class GFFSExportConfiguration {

	public static final String EXPORT_TYPE = "export.type";
	public static final String EXPORT_TYPE_SUDO = "sudo";

	private static Properties p = null;

	public static ExportType getExportType()
	{
		if (p == null) {
			p = new Properties();
			InputStream in = GFFSExportConfiguration.class.getClassLoader().getResourceAsStream("export.properties");
			try {
				p.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				StreamUtils.close(in);
			}
		}
		
		String r = p.getProperty(EXPORT_TYPE);
		if (r == null) {
			return ExportType.DEFAULT;
		}
		if (r.equalsIgnoreCase("sudo"))
			return ExportType.SUDO;
	
		return ExportType.DEFAULT;
	}
		
}
