package edu.virginia.vcgr.genii.client.rns;

import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.ws.addressing.EndpointReferenceType;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;

import edu.virginia.vcgr.genii.client.byteio.RandomByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIOOutputStream;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/**
 * thread to copy files
 */
public class ConcurrentCopyMachine implements Runnable
{
	static private Log _logger = LogFactory.getLog(ConcurrentCopyMachine.class);

	Semaphore semaphore;
	Mutex mutex;
	String sourceIn;
	String targetIn;
	RNSPath logLocation;

	public ConcurrentCopyMachine(String sourceIn, String targetIn, RNSPath logLocation, Semaphore semaphore, Mutex mutex)
	{
		this.semaphore = semaphore;
		this.mutex = mutex;
		this.sourceIn = sourceIn;
		this.targetIn = targetIn;
		this.logLocation = logLocation;
	}

	@Override
	public void run()
	{
		int fileSize = 0;
		int maxTries = 7;
		RNSPath current = RNSPath.getCurrent();
		RNSPath rnsPath = null;
		RandomByteIOTransferer reader = null;
		RandomByteIOTransferer writer = null;
		String sanitizedKey = (sourceIn + targetIn).replace("/", "");
		boolean success = false;
		for (int tries = 1; success == false; tries++) {
			try {
				rnsPath = current.lookup(new GeniiPath(sourceIn).path(), RNSPathQueryFlags.DONT_CARE);
				fileSize = Integer.parseInt((new TypeInformation(rnsPath.getEndpoint())).getTypeDescription());
				EndpointReferenceType fileEPR = rnsPath.getEndpoint();
				RandomByteIOPortType clientStub = ClientUtils.createProxy(RandomByteIOPortType.class, fileEPR);
				RandomByteIOTransfererFactory factory = new RandomByteIOTransfererFactory(clientStub);
				reader = factory.createRandomByteIOTransferer();

				GeniiPath geniiPath = new GeniiPath(targetIn);
				rnsPath = current.lookup(geniiPath.path(), RNSPathQueryFlags.DONT_CARE);
				rnsPath.mkdirs();
				if (rnsPath.exists()) {
					rnsPath.delete();
				}
				rnsPath.createNewFile();
				fileEPR = rnsPath.getEndpoint();
				clientStub = ClientUtils.createProxy(RandomByteIOPortType.class, fileEPR);
				factory = new RandomByteIOTransfererFactory(clientStub);
				writer = factory.createRandomByteIOTransferer();
				success = true;
			} catch (Throwable cause) {
				if (tries == maxTries) {
					System.exit(1);
				}
			}
		}

		DocumentBuilder dBuilder = null;
		Document doc = null;

		NodeList nl = null;
		try {
			mutex.acquire();
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = dBuilder.parse(new RandomByteIOInputStream(logLocation.getEndpoint()));
			doc.getDocumentElement().normalize();
			nl = doc.getElementsByTagName(sanitizedKey);
		} catch (Throwable e) {
			_logger.error("caught exception while parsing log", e);
		}
		int transferred = 0;
		if (nl == null || nl.getLength() == 0) {
			Element record = doc.createElement(sanitizedKey);
			record.setAttribute("bytes", "0");
			// record.appendChild(doc.createTextNode("0"));
			doc.getDocumentElement().appendChild(record);

			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new RandomByteIOOutputStream(logLocation.getEndpoint()));
				transformer.transform(source, result);
			} catch (Exception e) {
				_logger.error("caught exception while writing log", e);
			}

			nl = doc.getElementsByTagName(sanitizedKey);
		}
		Element n = (Element) nl.item(0);
		transferred = Integer.parseInt(n.getAttribute("bytes"));
		mutex.release();

		int blockSize = 65536;// 64 KB
		while (transferred < fileSize) {
			success = false;
			byte[] data = null;
			for (int tries = 1; success == false; tries++) {
				data = null;
				try {
					data = reader.read(transferred, blockSize, 1, 0);
					success = true;
				} catch (Throwable cause) {
					if (tries == maxTries) {
						System.exit(1);
					}
				}
			}

			success = false;
			for (int tries = 1; success == false; tries++) {
				try {
					writer.write(transferred, data.length, 0, data);
					success = true;
				} catch (Throwable cause) {
					if (tries == maxTries) {
						System.exit(1);
					}
				}
			}

			transferred += data.length;
			try {
				mutex.acquire();
				doc = dBuilder.parse(new RandomByteIOInputStream(logLocation.getEndpoint()));
				nl = doc.getElementsByTagName(sanitizedKey);
			} catch (Throwable e) {
				_logger.error("caught exception while finding tag", e);
			}
			n = (Element) nl.item(0);
			n.setAttribute("bytes", "" + transferred);

			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new RandomByteIOOutputStream(logLocation.getEndpoint()));
				transformer.transform(source, result);
			} catch (Exception e) {
				_logger.error("caught exception while writing log", e);
			}

			mutex.release();
		}

		semaphore.release();
	}
}