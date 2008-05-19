package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.appdesc.ApplicationDescriptionPortType;
import edu.virginia.vcgr.genii.appdesc.CreateDeploymentDocumentRequestType;
import edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType;
import edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType;
import edu.virginia.vcgr.genii.appdesc.bin.BinDeploymentType;
import edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionCreator;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionUtils;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationVersion;
import edu.virginia.vcgr.genii.client.appdesc.IUploadProgressListener;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class ApplicationDescriptionTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Registers a legacy application with the system so that it can later be deployed.";
	
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/app-description-usage.txt";
	
	private boolean _createApplication = false;
	private boolean _register = false;
	private boolean _registerProgram = false;
	
	private String _descriptionService = null;
	private String _cpuArch = null;
	private String _osName = null;
	private String _osVersion = null;
	
	
	public ApplicationDescriptionTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}

	public void setCreate_application()
	{
		_createApplication = true;
	}
	
	public void setRegister()
	{
		_register = true;
	}
	
	public void setRegister_program()
	{
		_registerProgram = true;
	}
	
	public void setDescription_service(String descriptionServicePath)
	{
		_descriptionService = descriptionServicePath;
	}
	
	public void setCpu_arch(String cpuArch)
	{
		_cpuArch = cpuArch;
	}
	
	public void setOs_type(String osType)
	{
		_osName = osType;
	}
	
	public void setOs_version(String osVersion)
	{
		_osVersion = osVersion;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (_createApplication)
			return createApplication(
				_descriptionService,
				getArgument(0), getArgument(1));
		else if (_register)
			return registerApplication(
				getArgument(0), getArgument(1),
				getArgument(2));
		else if(_registerProgram)
		{
			return registerProgram(
				getArgument(0), getArgument(1));
		} else
		{
			throw new InvalidToolUsageException();
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		int numFlags = 0;
		numFlags += (_createApplication) ? 1 : 0;
		numFlags += (_register) ? 1 : 0;
		numFlags += (_registerProgram) ? 1 : 0;
		
		if (numFlags != 1)
			throw new InvalidToolUsageException(
				"Either the createApplication or the register or the register-program " +
				"flag must be given.");
		
		int numArguments = getArguments().size();
		if (_createApplication && 
			(numArguments < 1 || numArguments > 2))
			throw new InvalidToolUsageException();
		else if (_register &&
			(numArguments < 2 || numArguments > 3))
			throw new InvalidToolUsageException();
		else if (_registerProgram && (numArguments != 2))
			throw new InvalidToolUsageException();
		
		if (_osVersion != null && _osName == null)
			throw new InvalidToolUsageException(
				"Cannot give os-version without os-name option.");
	}
	

	public int createApplication(
		String descriptionServicePath, String newApplication, String version)
		throws RemoteException, RNSException, ConfigurationException,
			CreationException, FileNotFoundException
	{
		RNSPath path = RNSPath.getCurrent().lookup(newApplication, 
			RNSPathQueryFlags.MUST_NOT_EXIST);
		EndpointReferenceType epr;
		
		if (descriptionServicePath == null)
		{
			epr = ApplicationDescriptionCreator.createApplicationDescription(
					"ApplicationDescriptionPortType", newApplication,
					(version == null) ? null : new ApplicationVersion(version));	
		} else
		{
			RNSPath servicePath = RNSPath.getCurrent().lookup(
				descriptionServicePath, RNSPathQueryFlags.MUST_EXIST);
			epr = ApplicationDescriptionCreator.createApplicationDescription(
					servicePath.getEndpoint(), newApplication, 
					(version == null) ? null : new ApplicationVersion(version));
		}
		
		try
		{
			path.link(epr);
			epr = null;
		} finally
		{
			if (epr != null)
			{
				GeniiCommon common = ClientUtils.createProxy(
					GeniiCommon.class, epr);
				common.destroy(new Destroy());
			}
		}

		return 0;
	}
	
	public int registerApplication(
		String applicationName, String registrationName,
		String deploymentDocumentPath)
		throws FileNotFoundException, IOException, RNSException,
			ConfigurationException
	{
		File deployDirectory = null;
		DeploymentDocumentType deployDocument;
		
		if (deploymentDocumentPath != null)
		{
			deployDocument = readDeploymentDocument(deploymentDocumentPath);
			deployDirectory = new File(deploymentDocumentPath).getParentFile();
		} else
		{
			deployDocument = createDeploymentDocument();
			deployDirectory = new File(".");
		}

		return register(applicationName, registrationName,
			deployDirectory, deployDocument);
	}
	
	public int registerProgram(String applicationPath, String binaryPath)
		throws ToolException, FileNotFoundException, IOException, RNSException,
		ConfigurationException
	{
		PlatformDescriptionType []platforms;
		
		if (_cpuArch != null || _osName != null)
		{
			platforms = new PlatformDescriptionType[] {
				makePlatformDescriptionType(_cpuArch, _osName, _osVersion)
			};
		} else
		{
			platforms = new PlatformDescriptionType[] {
				determineLocalPlatform()
			};
		}
		
		File binaryFile = new File(binaryPath);
		if (!binaryFile.exists())
			throw new ToolException("Couldn't locate binary \"" + binaryPath + "\".");
		
		MessageElement source =
			new MessageElement(
				ApplicationDescriptionUtils.LOCAL_FILE_SOURCE_NAME,
				binaryFile.getAbsolutePath());
		BinDeploymentType bdt = new BinDeploymentType(".", 
			binaryFile.getName(), null, new NamedSourceType[] {
				new NamedSourceType(
					new MessageElement[] { source }, binaryFile.getName())
			}, null, null);
		MessageElement elem = new MessageElement(
			ObjectSerializer.toElement(bdt, 
				ApplicationDescriptionUtils.BINARY_DEPLOYMENT_ELEMENT_QNAME));
		DeploymentDocumentType deployDoc = new DeploymentDocumentType(
			platforms, new MessageElement [] {
				elem
			});
		
		return register(applicationPath, String.format("%s-binary",
			platforms[0].getCPUArchitecture()[0].getCPUArchitectureName().toString()),
			new File("."), deployDoc);
	}
	
	private int register(String applicationPath, String registrationName,
		File deployDirectory, DeploymentDocumentType deployDocument)
			throws FileNotFoundException, IOException, RNSException,
				ConfigurationException
	{
		deployDocument = ApplicationDescriptionUtils.uploadLocalSources(
			deployDirectory, deployDocument,
			new DeployPrinter(stdout));
		
		RNSPath path = RNSPath.getCurrent().lookup(
			applicationPath, RNSPathQueryFlags.MUST_EXIST);
		ApplicationDescriptionPortType appDesc =
			ClientUtils.createProxy(
				ApplicationDescriptionPortType.class, path.getEndpoint());
		appDesc.createDeploymentDocument(
			new CreateDeploymentDocumentRequestType(registrationName,
				deployDocument));
		
		return 0;
	}
	
	static private DeploymentDocumentType readDeploymentDocument(String path)
		throws FileNotFoundException, IOException
	{
		FileInputStream fin = null;
		fin = new FileInputStream(path);
		
		try
		{
			return (DeploymentDocumentType)ObjectDeserializer.deserialize(
				new InputSource(fin), DeploymentDocumentType.class);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static private DeploymentDocumentType createDeploymentDocument()
	{
		throw new RuntimeException("Interactive deployment not implemented yet.");
	}
	
	static private class DeployPrinter implements IUploadProgressListener
	{
		private PrintStream _out;
		
		public DeployPrinter(PrintStream out)
		{
			_out = out;
		}
		
		public void finishedUpload(File localFile)
		{
			_out.println("[finished]");
		}

		public void startingUpload(File localFile)
		{
			_out.print("Starting to upload deployment file \""
				+ localFile + "\"...");
		}
	}
	
	static private PlatformDescriptionType makePlatformDescriptionType(
		String cpuArch, String osName, String osVersion)
		throws ToolException
	{
		CPUArchitecture_Type []cpuArches = null;
		OperatingSystem_Type []oses = null;
		
		if (cpuArch != null)
		{
			try
			{
				cpuArches = new CPUArchitecture_Type[] {
					new CPUArchitecture_Type(
						ProcessorArchitectureEnumeration.fromString(cpuArch), null)
				};
			}
			catch (IllegalArgumentException iae)
			{
				throw new ToolException("\"" + cpuArch 
					+ "\" is not a valid CPU Architecture.", iae);
			}
		}
		
		if (osName != null)
		{
			try
			{
				oses = new OperatingSystem_Type[] {
					new OperatingSystem_Type(
						new OperatingSystemType_Type(
							OperatingSystemTypeEnumeration.fromString(osName), null),
						osVersion, null, null)
				};
			}
			catch (IllegalArgumentException iae)
			{
				throw new ToolException("\"" + osName
					+ "\" is not a valid Operating System Name.", iae);
			}
		}
		
		return new PlatformDescriptionType(cpuArches, oses, null);
	}
	
	static private PlatformDescriptionType determineLocalPlatform()
	{

		PlatformDescriptionType pdesc = new PlatformDescriptionType(
			new CPUArchitecture_Type[] {
				JSDLUtils.getLocalCPUArchitecture()
			},
			new OperatingSystem_Type[] {
					JSDLUtils.getLocalOperatingSystem()
			}, null);	
		
		pdesc.getOperatingSystem(0).setOperatingSystemVersion(null);
		return pdesc;
	}
}