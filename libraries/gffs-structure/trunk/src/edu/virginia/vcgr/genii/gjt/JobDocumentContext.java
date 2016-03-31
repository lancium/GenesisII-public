package edu.virginia.vcgr.genii.gjt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.JobRoot;
import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.ModificationListener;
import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableManager;
import edu.virginia.vcgr.genii.gjt.gui.GridJobToolFrame;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreference;
import edu.virginia.vcgr.jsdl.JSDLUtility;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.Sweep;

public class JobDocumentContext
{
	static private Logger _logger = Logger.getLogger(JobDocumentContext.class);
	private JobApplicationContext _applicationContext;

	private ParameterizableBroker _pBroker;
	private ModificationBroker _mBroker;

	private boolean _initial;
	private JobDocument _document;
	private VariableManager _variableManager;
	private File _file;
	private boolean _modified = false;

	private GridJobToolFrame _frame;

	private JobRoot _masterDocument;

	JobDocumentContext(JobApplicationContext applicationContext, File initialFile) throws IOException
	{
		_applicationContext = applicationContext;
		_variableManager = new VariableManager();
		_file = initialFile;

		ParameterizableBroker pBroker = new ParameterizableBroker();
		ModificationBroker mBroker = new ModificationBroker();

		_pBroker = pBroker;
		_mBroker = mBroker;

		_pBroker.addParameterizableListener(_variableManager);

		if (_file != null && _file.length() > 0) {
			try {
				// Get the name of the root node in the .gjp XML file
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(_file.getAbsolutePath());
				Node root = doc.getFirstChild();
				String rootName = root.getNodeName();

				// This is used for backwards compatibility. The second case refers to the old
				// root node and the first one refers to the new root node
				if (rootName.equals("grid-job:master-job-project")) {
					_masterDocument = JobRoot.load(_file, _pBroker, _mBroker);
					// if(_masterDocument.jobDocument().size() > 1){
					_document = _masterDocument.jobDocument().get(0);
					// }

				} else if (rootName.equals("grid-job:job-project")) {
					_masterDocument = new JobRoot();

					// Create empty Job Document for Common Block
					JobDocument commonDocument = JobDocument.load(_file, _pBroker, _mBroker);
					commonDocument.postUnmarshall(_pBroker, _mBroker);
					_masterDocument.jobDocument().add(commonDocument);

					// Load Job Document for first job description

					// _masterDocument.jobDocument().add(_document);
					System.out.println("MasterDocument size =" + _masterDocument.jobDocument().size());
					// System.out.println("JobDocument Feature =" + _document.;
					_document = _masterDocument.jobDocument().get(0);
				}
				// System.out.println("****************************************************");
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (SAXException sae) {
				sae.printStackTrace();
			}

		} else {
			_masterDocument = new JobRoot();

			// Create empty Job Document for common block
			JobDocument commonDocument = new JobDocument();
			commonDocument.postUnmarshall(_pBroker, _mBroker);
			_masterDocument.jobDocument().add(commonDocument);

			// Create empty Job Document for first job description
			// _document = new JobDocument();
			_document = _masterDocument.jobDocument().get(0);
			// _document.postUnmarshall(_pBroker, _mBroker);
			// _masterDocument.jobDocument().add(_document);

		}
		_document.stageIns().setStageIn();
		if (_file != null && _file.length() == 0)
			_modified = true;

		_frame = GridJobToolFrame.createNewFrame(this);
		// if (listener != null)
		// _frame.addWindowListener(new JobDocumentClosedWindowAdapter(listener));

		setFrameTitle();

		_mBroker.addModificationListener(new ModificationListenerImpl());
	}

	public GridJobToolFrame getFrame()
	{
		return _frame;
	}

	public void setFrameTitle()
	{
		String filename = (_file == null) ? "New Job Document" : _file.getAbsolutePath();
		_frame.setTitle(String.format("Grid Job Tool - %s%s", filename, _modified ? "*" : ""));
	}

	void setInitial()
	{
		_initial = true;
	}

	boolean isInitial()
	{
		return _initial;
	}

	void start()
	{
		_frame.pack();
		GuiUtils.centerComponent(_frame);
		_frame.setVisible(true);
	}

	final public ParameterizableBroker getParameterizableBroker()
	{
		return _pBroker;
	}

	final public JobApplicationContext applicationContext()
	{
		return _applicationContext;
	}

	final public ModificationBroker getModificationBroker()
	{
		return _mBroker;
	}

	final public JobDocument jobDocument()
	{
		return _document;
	}

	final public VariableManager variableManager()
	{
		return _variableManager;
	}

	final public boolean isModified()
	{
		return _modified;
	}

	final public JobRoot jobRoot()
	{
		return _masterDocument;
	}

	final public void saveAs()
	{
		if (!_frame.hasFocus()) {
			_frame.requestFocusInWindow();
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					saveAs();
				}
			});
			return;
		}

		while (true) {
			File target = _applicationContext.getDesiredFile(_frame, false, false);
			if (target != null) {
				String name = target.getName();
				int index = name.lastIndexOf('.');
				if (index < 0)
					target = new File(target.getParentFile(), name + ".gjp");

				if (target.exists()) {
					int result =
						JOptionPane.showConfirmDialog(_frame, "Overwrite existing file?", "File Exists", JOptionPane.YES_NO_CANCEL_OPTION);
					if (result == JOptionPane.CANCEL_OPTION)
						return;
					else if (result == JOptionPane.NO_OPTION)
						continue;
				}

				try {
					// JobDocument.store(_document, target);
					if (_masterDocument.jobDocument().size() > 1) {
						JobRoot.store(_masterDocument, target);
					} else {
						JobDocument.store(_document, target);
					}
					_file = target;
					_modified = false;
					setFrameTitle();
					return;
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(_frame, "Error saving project file.", "Error Saving Project File",
						JOptionPane.ERROR_MESSAGE);
					_logger.error("Unable to save project file.", ioe);
					return;
				}
			} else
				break;
		}
	}

	final public void save()
	{
		if (_file == null) {
			saveAs();
			return;
		}

		try {
			if (!_frame.hasFocus()) {
				_frame.requestFocusInWindow();
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						save();
					}
				});
				return;
			}
			// JobDocument.store(_document, _file);
			if (_masterDocument.jobDocument().size() > 1) {
				JobRoot.store(_masterDocument, _file);
			} else {
				JobDocument.store(_document, _file);
			}
			_modified = false;
			setFrameTitle();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(_frame, "Error saving project file.", "Error Saving Project File", JOptionPane.ERROR_MESSAGE);
			_logger.error("Unable to save project file.", ioe);
		}
	}

	final public void close()
	{
		_frame.dispose();
	}

	final public void generateJSDL(final boolean willLaunch)
	{
		if (!_frame.hasFocus()) {
			_frame.requestFocusInWindow();
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					generateJSDL(willLaunch);
				}
			});
			return;
		}

		if (((Boolean) (_applicationContext.preferences().preference(ToolPreference.PopupForWarnings))).booleanValue()) {
			Analysis analysis;
			if (jobRoot().jobDocument().size() > 1) {
				analysis = jobRoot().jobDocument().get(1).analyze();
			} else {
				analysis = jobRoot().jobDocument().get(0).analyze();
			}
			if (analysis.hasWarnings()) {
				int result = JOptionPane.showConfirmDialog(_frame, "There are still warnings!  Do you still want to generate JSDL?",
					"JSDL Generation Confirmation", JOptionPane.YES_NO_OPTION);

				if (result != JOptionPane.YES_OPTION)
					return;
			}
		}

		JobDefinition jobDef = _masterDocument.generate();

		// Observation: Data structures like JobAnnotations and JobProjects in JobIdentification are
		// not null but are of size 0.
		// Data staging is also the same if we put nothing in it but Application and Resources will
		// be null if we put nothing in them.
		// Solution: Put code in JSDLTransformer.java to check for size 0 data structures.

		List<Sweep> sweeps = jobDef.parameterSweeps();
		int count = 0;
		for (Sweep sweep : sweeps)
			count += sweep.size();

		int limit = (((Integer) (_applicationContext.preferences().preference(ToolPreference.ParameterSweepPopupLimit))).intValue());

		if (count >= limit) {
			int result =
				JOptionPane.showConfirmDialog(_frame, String.format("This JSDL document contains %d jobs!  Do you want to continue?", count),
					"JSDL Generation Confirmation", JOptionPane.YES_NO_OPTION);

			if (result != JOptionPane.YES_OPTION)
				return;
		}

		JobDefinitionListener generationListener = _applicationContext.getGenerationListener();
		if (willLaunch && (generationListener != null)) {
			generationListener.jobDefinitionGenerated(jobDef);
		} else {
			while (true) {
				File target = _applicationContext.getDesiredFile(_frame, false, true);
				if (target != null) {
					String name = target.getName();
					int index = name.lastIndexOf('.');
					if (index < 0)
						target = new File(target.getParentFile(), name + ".jsdl");

					if (target.exists()) {
						int result = JOptionPane.showConfirmDialog(_frame, "Overwrite existing file?", "File Exists",
							JOptionPane.YES_NO_CANCEL_OPTION);
						if (result == JOptionPane.CANCEL_OPTION)
							return;
						else if (result == JOptionPane.NO_OPTION)
							continue;
					}

					try {
						Marshaller m = JSDLUtility.JSDLContext.createMarshaller();
						m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
						m.marshal(jobDef, target);
						return;
					} catch (JAXBException e) {
						JOptionPane.showMessageDialog(_frame, "Error generating JSDL file.", "Error Generating JSDL File",
							JOptionPane.ERROR_MESSAGE);
						_logger.error("Unable to generate JSDL file.", e);
						return;
					}
				} else
					break;
			}
		}
	}

	private class ModificationListenerImpl implements ModificationListener
	{
		@Override
		public void jobDescriptionModified()
		{
			_modified = true;
			_initial = false;
			setFrameTitle();
		}
	}

	// static private class JobDocumentClosedWindowAdapter extends WindowAdapter
	// {
	// private JobToolListener _listener;
	//
	// private JobDocumentClosedWindowAdapter(JobToolListener listener)
	// {
	// _listener = listener;
	// }
	//
	// @Override
	// final public void windowClosed(WindowEvent e)
	// {
	// _listener.jobWindowClosed();
	// }
	// }

}