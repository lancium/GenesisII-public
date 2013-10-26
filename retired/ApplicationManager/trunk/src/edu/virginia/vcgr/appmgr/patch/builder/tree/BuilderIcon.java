package edu.virginia.vcgr.appmgr.patch.builder.tree;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public class BuilderIcon extends ImageIcon
{
	static final long serialVersionUID = 0L;

	static private byte[] loadImageData(String name)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream in = null;

		try {
			in = BuilderIcon.class.getResourceAsStream(String.format("icons/%s", name));
			IOUtils.copy(in, baos);
			baos.flush();
			return baos.toByteArray();
		} catch (Throwable cause) {
			return new byte[0];
		} finally {
			IOUtils.close(in);
		}
	}

	BuilderIcon(String iconName)
	{
		super(loadImageData(iconName));
	}
}