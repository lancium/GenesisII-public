package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.WSAddressingConstants;
import edu.virginia.vcgr.genii.client.naming.eprbuild.GenesisIIEPRBuilder;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

final public class MintEprTool extends BaseGridTool
{
	static final private String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dmint-epr";
	static final private FileResource USAGE = new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/usage/umint-epr");

	static private EndpointReferenceType readEPRFromFile(GeniiPath path) throws IOException
	{
		InputStream in = null;

		try {
			in = path.openInputStream();
			return (EndpointReferenceType) ObjectDeserializer.deserialize(new InputSource(in), EndpointReferenceType.class);
		} finally {
			StreamUtils.close(in);
		}
	}

	static private X509Certificate[] readCertChainFromFile(GeniiPath path) throws CertificateException, IOException
	{
		InputStream in = null;

		try {
			in = path.openInputStream();
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(in);
			return new X509Certificate[] { cert };
		} finally {
			StreamUtils.close(in);
		}
	}

	private GeniiPath _outputFile = null;
	private GeniiPath _linkPath = null;
	private Set<GeniiPath> _referenceParameterFiles = new HashSet<GeniiPath>();
	private Set<PortType> _portTypes = new HashSet<PortType>();
	private String _epi = null;
	private Set<GeniiPath> _epiResolvers = new HashSet<GeniiPath>();
	private Set<GeniiPath> _refResolvers = new HashSet<GeniiPath>();
	private GUID _containerID = null;
	private GeniiPath _certificateChain = null;
	private Boolean _usernamePasswordPolicy = null;
	private boolean _requireEncryption = false;
	private boolean _includeServerTls = false;
	private boolean _requireMessageSigning = false;

	private Collection<Element> getReferenceParameters() throws IOException, ParserConfigurationException, SAXException
	{
		Collection<Element> refParams = new LinkedList<Element>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		for (GeniiPath path : _referenceParameterFiles) {
			InputStream in = null;

			try {
				in = path.openInputStream();
				Document doc = builder.parse(in);
				refParams.add(doc.getDocumentElement());
			} finally {
				StreamUtils.close(in);
			}
		}

		return refParams;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_linkPath != null) {
			if (_linkPath.pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException("Link Path is not a grid path!");
			if (_outputFile != null)
				throw new InvalidToolUsageException("Cannot have both a link path and an output file specified!");
		}

		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GenesisIIEPRBuilder eprFactory = new GenesisIIEPRBuilder(new URI(getArgument(0)));

		for (Element e : getReferenceParameters())
			eprFactory.addReferenceParameters(e);

		eprFactory.addPortTypes(_portTypes.toArray(new PortType[_portTypes.size()]));

		if (_epi != null)
			eprFactory.epi(new URI(_epi));

		if (!_epiResolvers.isEmpty()) {
			for (GeniiPath path : _epiResolvers)
				eprFactory.addEndpointIdentifierResolvers(readEPRFromFile(path));
		}

		if (!_refResolvers.isEmpty()) {
			for (GeniiPath path : _refResolvers)
				eprFactory.addReferenceResolvers(readEPRFromFile(path));
		}

		if (_containerID != null)
			eprFactory.containerID(_containerID);

		if (_certificateChain != null) {
			X509Certificate[] certChain = readCertChainFromFile(_certificateChain);
			eprFactory.certificateChain(certChain);
		}

		if (_usernamePasswordPolicy != null)
			eprFactory.addUsernamePasswordTokenPolicy(_usernamePasswordPolicy);

		if (_requireEncryption)
			eprFactory.requireEncryption(true);

		if (_includeServerTls)
			eprFactory.includeServerTls(true);

		if (_requireMessageSigning)
			eprFactory.requireMessageSigning(true);

		OutputStream out = null;
		PrintWriter writer;
		try {
			if (_outputFile != null) {
				out = _outputFile.openOutputStream();
				writer = new PrintWriter(out);
			} else if (_linkPath != null) {
				RNSPath path = RNSPath.getCurrent().lookup(_linkPath.path(), RNSPathQueryFlags.MUST_NOT_EXIST);
				path.link(eprFactory.mint());
				return 0;
			} else
				writer = stdout;

			ObjectSerializer.serialize(writer, eprFactory.mint(), new QName(WSAddressingConstants.WSA_NS, "epr", "wsa"));
			writer.flush();

			return 0;
		} finally {
			StreamUtils.close(out);
		}
	}

	public MintEprTool()
	{
		super(new FileResource(DESCRIPTION), USAGE, false, ToolCategory.ADMINISTRATION);
	}

	@Option("output")
	final public void setOutputFile(String path)
	{
		_outputFile = new GeniiPath(path);
	}

	@Option(maxOccurances = "unbounded", value = "reference-parameters")
	final public void setReferenceParameterFile(String path)
	{
		_referenceParameterFiles.add(new GeniiPath(path));
	}

	@Option(maxOccurances = "unbounded", value = "port-type")
	final public void setPortType(String portType)
	{
		QName portTypeName = QName.valueOf(portType);
		if (PortType.isKnown(portTypeName))
			_portTypes.add(PortType.get(portTypeName));
	}

	@Option("epi")
	final public void setEPI(String epi)
	{
		_epi = epi;
	}

	@Option(maxOccurances = "unbounded", value = "epi-resolver")
	final public void setEPIResolver(String path)
	{
		_epiResolvers.add(new GeniiPath(path));
	}

	@Option(maxOccurances = "unbounded", value = "reference-resolver")
	final public void setReferenceResolver(String path)
	{
		_refResolvers.add(new GeniiPath(path));
	}

	@Option("container-id")
	final public void setContainerID(String containerID)
	{
		_containerID = GUID.fromString(containerID);
	}

	@Option("certificate-chain")
	final public void setCertificateChain(String path)
	{
		_certificateChain = new GeniiPath(path);
	}

	@Option("add-username-password-policy")
	final public void setUsernamePasswordPolicy(String isOptional)
	{
		_usernamePasswordPolicy = Boolean.valueOf(isOptional);
	}

	@Option("require-encryption")
	final public void setRequireEncryption()
	{
		_requireEncryption = true;
	}

	@Option("include-server-tls")
	final public void setIncludeServerTls()
	{
		_includeServerTls = true;
	}

	@Option("require-message-signing")
	final public void setRequireMessageSigning()
	{
		_requireMessageSigning = true;
	}

	@Option("link")
	final public void setLink(String path)
	{
		_linkPath = new GeniiPath(path);
	}
}