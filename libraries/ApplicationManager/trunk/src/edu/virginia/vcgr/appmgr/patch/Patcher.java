package edu.virginia.vcgr.appmgr.patch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.security.VerifiableJarFile;

public class Patcher
{
	static private Collection<PatchOperation> parsePatchDescription(JarFile patchFile, ApplicationDescription appDescription)
		throws IOException, ParserConfigurationException, SAXException
	{
		InputStream in = null;
		JarEntry entry = patchFile.getJarEntry("META-INF/patch/patch-description.xml");
		if (entry == null)
			throw new IOException("Unable to locate patch-description.xml file in patch.");

		try {
			in = patchFile.getInputStream(entry);
			return PatchDescriptionParser.parse(patchFile, appDescription, in);
		} finally {
			in.close();
		}
	}

	static public void patch(PrintStream log, ApplicationDescription appDescription, File patchFile) throws IOException,
		ParserConfigurationException, SAXException
	{
		VerifiableJarFile patchJarFile = new VerifiableJarFile(appDescription.getPatchVerifier(), patchFile);

		log.println("Parsing patch description");
		log.flush();
		Collection<PatchOperation> operations = parsePatchDescription(patchJarFile, appDescription);

		Collection<PatchOperationTransaction> transactions = new LinkedList<PatchOperationTransaction>();

		log.println("Applying patch:");
		log.flush();
		try {
			for (PatchOperation operation : operations) {
				if (!operation.satisfies()) {
					log.format("    Skipping \"%s\".\n", operation);
					log.flush();
				} else
					transactions.add(operation.perform(log));
			}

			log.println("Committing patch.");
			log.flush();
			for (PatchOperationTransaction transaction : transactions)
				transaction.commit();
			transactions.clear();
		} finally {
			if (transactions.size() > 0) {
				log.println("Rolling patch back.");
				log.flush();
				for (PatchOperationTransaction transaction : transactions)
					transaction.rollback();
			}
		}

		log.println("Finished patching.");
		log.flush();
	}
}