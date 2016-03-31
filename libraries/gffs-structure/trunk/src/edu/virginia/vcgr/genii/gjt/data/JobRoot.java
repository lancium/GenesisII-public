package edu.virginia.vcgr.genii.gjt.data;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

// import edu.virginia.vcgr.genii.client.jsdl.JSDLTransformer;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.recent.Recents;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.UnmarshallListener;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathIterableNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.jsdl.Application;
import edu.virginia.vcgr.jsdl.Common;
import edu.virginia.vcgr.jsdl.JSDLConstants;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.JobDescription;
import edu.virginia.vcgr.jsdl.JobIdentification;
import edu.virginia.vcgr.jsdl.Resources;
import edu.virginia.vcgr.jsdl.sweep.Sweep;
import edu.virginia.vcgr.jsdl.sweep.SweepAssignment;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

@XmlRootElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "master-job-project")
public class JobRoot
{
	final int _paramSweepDoc = 0;

	static Logger _logger = Logger.getLogger(JobRoot.class);

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "common")
	private JobDocument _commonBlock;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-project")
	private List<JobDocument> _jobDocs = new LinkedList<JobDocument>();

	final public List<JobDocument> jobDocument()
	{
		return _jobDocs;
	}

	static public JobRoot load(File source, ParameterizableBroker parameterBroker, ModificationBroker modificationBroker) throws IOException
	{
		JobRoot result;

		try {
			JAXBContext context = JAXBContext.newInstance(JobRoot.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setListener(new UnmarshallListener(parameterBroker, modificationBroker));
			result = (JobRoot) unmarshaller.unmarshal(source);

			Recents.instance.addRecent(source);
			return result;
		} catch (JAXBException e) {
			throw new IOException(String.format("Unable to load project from file \"%s\".", source), e);
		}
	}

	static public void store(JobRoot masterDocument, File target) throws IOException
	{
		try {
			JAXBContext context = JAXBContext.newInstance(JobRoot.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(masterDocument, target);
			Recents.instance.addRecent(target);
		} catch (JAXBException e) {
			throw new IOException(String.format("Unable to save project to file \"%s\".", target), e);
		}
	}

	private void generateCommonBlock(XPathBuilder builder, Map<String, List<SweepParameter>> variables, EnumSet<FilesystemType> filesystemSet,
		JobDefinition jobDef, JobDocument commonDoc)
	{
		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "Common"));
		JobIdentification jobIdent = commonDoc.generateJobIdentification(builder, variables);
		Application application = commonDoc.generateApplication(builder, variables, filesystemSet);
		Resources resources = commonDoc.generateResources(builder, variables, filesystemSet);

		Common commonBlock = new Common(jobIdent, application, resources);

		commonBlock.staging().addAll(commonDoc.generateDataStaging(builder, variables, filesystemSet));

		jobDef.common(commonBlock);

		builder.pop();

	}

	public JobDefinition generate()
	{
		Map<String, List<SweepParameter>> variables = new HashMap<String, List<SweepParameter>>();
		EnumSet<FilesystemType> filesystemSet = EnumSet.noneOf(FilesystemType.class);

		XPathBuilder builder = new XPathBuilder();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "JobDefinition"));
		JobDefinition jobDef = new JobDefinition();
		int num_of_jobDescriptions = 0;

		if (_jobDocs.size() > 2) {
			generateCommonBlock(builder, variables, filesystemSet, jobDef, _jobDocs.get(0));
			// System.out.println("Removing extra empty job description");
			// _jobDocs.remove(_jobDocs.size()-1);
			num_of_jobDescriptions = 1;
		} else {
			num_of_jobDescriptions = 0;
			// _jobDocs.remove(_jobDocs.size()-1);
		}
		// generateCommonBlock(builder, variables, filesystemSet, jobDef, _jobDocs.get(0));

		DefaultXPathIterableNode node = new DefaultXPathIterableNode(JSDLConstants.JSDL_NS, "JobDescription");
		builder.push(node);

		// System.out.println("The size of Job Docs " + _jobDocs.size());

		for (int i = num_of_jobDescriptions; i < _jobDocs.size(); i++) {
			JobIdentification jobIdent = _jobDocs.get(i).generateJobIdentification(builder, variables);
			Application application = _jobDocs.get(i).generateApplication(builder, variables, filesystemSet);
			Resources resources = _jobDocs.get(i).generateResources(builder, variables, filesystemSet);

			JobDescription jobDesc = new JobDescription(jobIdent, application, resources);

			jobDesc.staging().addAll(_jobDocs.get(i).generateDataStaging(builder, variables, filesystemSet));

			jobDef.jobDescription().add(jobDesc);

			builder.iterate();
		}

		builder.pop();

		Sweep root = null;
		if (!variables.isEmpty()) {
			Sweep sweep = null;
			for (Map.Entry<String, List<SweepParameter>> variable : variables.entrySet()) {
				Sweep newSweep = new Sweep();
				if (root == null)
					root = newSweep;
				if (sweep != null)
					sweep.addSubSweep(newSweep);
				sweep = newSweep;

				SweepFunction function = _jobDocs.get(_paramSweepDoc).variables().get(variable.getKey()).generateFunction();// Focus
																															// on
																															// this
																															// line
				SweepAssignment assignment = new SweepAssignment(function);
				for (SweepParameter parameter : variable.getValue())
					assignment.addParameter(parameter);
				sweep.addAssignment(assignment);
			}

			jobDef.parameterSweeps().add(root);

		}
		return jobDef;
	}
}
