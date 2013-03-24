package edu.virginia.vcgr.genii.network.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.ws.addressing.EndpointReferenceType;

public class RedirectFile
{
	private byte[] _content;

	public RedirectFile(EndpointReferenceType epr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream writer = new PrintStream(baos);

		writer.println("<HTML>");
		writer.println("<HEAD>");
		writer.println("<META HTTP-EQUIV=\"Refresh\"");
		writer.println("CONTENT=\"0; URL=" + epr.getAddress().get_value() + "\">");
		writer.println("</HEAD>");
		writer.println("<BODY>");
		writer.println("If you are not automatically redirected, please click ");
		writer.println("<A HREF=\"" + epr.getAddress().get_value() + "\"> here</A>");
		writer.println("</BODY>");
		writer.println("</HTML>");

		writer.flush();
		writer.close();

		_content = baos.toByteArray();
	}

	public long getSize()
	{
		return _content.length;
	}

	public InputStream getStream()
	{
		return new ByteArrayInputStream(_content);
	}
}