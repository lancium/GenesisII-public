package edu.virginia.vcgr.genii.client.filesystems.email;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.filesystems.Filesystem;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemManager;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemWatchHandler;

public class EmailWatchHandler implements FilesystemWatchHandler
{
	static private Log _logger = LogFactory.getLog(EmailWatchHandler.class);
	
	private EmailWatchConfiguration _config;
	
	private EmailWatchHandler(InputStream configStream) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(
			EmailWatchConfiguration.class);
		Unmarshaller u = context.createUnmarshaller();
		_config = u.unmarshal(new StreamSource(configStream),
			EmailWatchConfiguration.class).getValue();	
	}
	
	public EmailWatchHandler(Element element) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(
			EmailWatchConfiguration.class);
		Unmarshaller u = context.createUnmarshaller();
		_config = u.unmarshal(element, EmailWatchConfiguration.class).getValue();	
	}
	
	final public void sendMessage(String subject, String message)
	{
		final AddressInfoConfiguration addr = _config.addr();
		final ConnectConfig connect = _config.connection();
		
		final LinkedList<String> TO = addr.to();
		final String FROM = addr.from();
		
		final String HOST = connect.smtpServer();
		final boolean USE_SSL = connect.isSSL();
		final boolean DEBUG_ON = connect.debugOn();
		final int PORT = connect.port();
		final String USERNAME = connect.username();
		final String PASSWORD = connect.password();
		
		Properties props = new Properties();

        Session session = Session.getInstance(props);
        session.setDebug(DEBUG_ON);

        try
        {
            // Instantiate a message
            Message msg = new MimeMessage(session);

            //Set message attributes
            msg.setFrom(new InternetAddress(FROM));
            InternetAddress[] address = new InternetAddress[TO.size()];
            ListIterator<String> itr = TO.listIterator();
            for (int i = 0; i < TO.size(); i++)
            	address[i] = new InternetAddress(itr.next());
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // Set message content
            msg.setText(message);

            Transport transport = session.getTransport(USE_SSL ? "smtps" : "smtp");
        	transport.connect(HOST, PORT, USERNAME, PASSWORD);
            transport.sendMessage(msg, address);
        }
        catch (MessagingException mex)
        {
        	_logger.error(
        		"Error trying to send email for file system notification.", 
        		mex);
        }
	}
	
	@Override
	public void notifyFilesystemEvent(FilesystemManager manager,
			String filesystemName, Filesystem filesystem,
			FilesystemUsageInformation usageInformation,
			boolean matched)
	{			
		final String SUBJECT = _config.subject();
		final String MESSAGE = _config.format(filesystemName,
				usageInformation.percentUsed());
		
		sendMessage(SUBJECT, MESSAGE);
	}
	
	static public void main(String []args) throws Throwable
	{
		InputStream in = null;
		
		try
		{
			if (args.length == 1)
			{
				in = new FileInputStream(args[0]);
			} else
			{
				in = EmailWatchHandler.class.getResourceAsStream(
					"test-config.xml");
			}
			
			EmailWatchHandler handler = new EmailWatchHandler(in);
			handler.sendMessage("Test Message",
				"This is a test message send from Java.");
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}