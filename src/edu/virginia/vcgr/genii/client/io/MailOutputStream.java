/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.mail.smtp.SMTPMessage;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.Hostname;

class MailOutputStream extends OutputStream
{
	static private final String _FILENAME_X_HEADER = "X-AttachmentFilename";
	static private Log _logger = LogFactory.getLog(MailOutputStream.class);
	
	static private QName _SMTP_PROPERTIES = new QName(
		GenesisIIConstants.GENESISII_NS, "smtp-properties");
	static private final String _SMTP_PASSWORD_PROPERTY =
		"edu.virginia.vcgr.htc.smtp.password";
	
	private Session _session;
	private SMTPMessage _message;
	private MimeMultipart _multipart;
	private Properties _mailProps;
	private ByteArrayOutputStream _baos = new ByteArrayOutputStream();
	
	public MailOutputStream(String address, Properties headers) throws IOException
	{
		_mailProps = null;
		try
		{
			_mailProps = 
				(Properties)(ConfigurationManager.getCurrentConfiguration(
					).getClientConfiguration().retrieveSection(_SMTP_PROPERTIES));
		}
		catch (Throwable t)
		{
			_logger.error(t);
		}
		
		if (_mailProps == null)
		{
			_mailProps = new Properties();
			_mailProps.put("mail.smtp.host", "localhost");
			_mailProps.put("mail.smtp.auth", "false");
			_mailProps.put("mail.smtp.from", 
				"vcgr-grid@" + Hostname.getLocalHostname());
		}
		
		_session = Session.getInstance(_mailProps);
		_message = new SMTPMessage(_session);

		try
		{
			for (Object key : headers.keySet())
			{
				String header = (String)key;
				String value = headers.getProperty(header);
				_message.setHeader(header, value);
			}
			_message.addRecipient(RecipientType.TO,
				new InternetAddress(address));
			
			_multipart = new MimeMultipart();
			_message.setContent(_multipart);
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText("Output.");
			_multipart.addBodyPart(bodyPart);
		}
		catch (MessagingException me)
		{
			throw new IOException(me.getMessage());
		}
	}
	
	@Override
	public void close() throws IOException
	{
		_baos.close();
		Transport transport = null;
		try
		{
			ByteArrayDataSource source = new ByteArrayDataSource(
				_baos.toByteArray(), "application/octet-stream");
			MimeBodyPart part = new MimeBodyPart();
			part.setDataHandler(new DataHandler(source));
			
			String []results = _message.getHeader(_FILENAME_X_HEADER);
			if (results != null && results.length > 0)
				part.setFileName(results[0]);
			
			_multipart.addBodyPart(part);
			transport = _session.getTransport("smtp");
			String passwd = _mailProps.getProperty(_SMTP_PASSWORD_PROPERTY);
			
			transport.connect(null, null, passwd);
			_message.saveChanges();
			transport.sendMessage(_message, _message.getAllRecipients());
		}
		catch (MessagingException me)
		{
			throw new IOException(me.toString());
		}
		finally
		{
			try { if (transport != null) transport.close(); } catch (Throwable t) {}
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		_baos.write(b);
	}
	
	@Override 
	public void write(byte[] b, int off, int len) throws IOException
	{
		_baos.write(b, off, len);
	}

	@Override
	public void write(int arg0) throws IOException
	{
		_baos.write(arg0);
	}
}
