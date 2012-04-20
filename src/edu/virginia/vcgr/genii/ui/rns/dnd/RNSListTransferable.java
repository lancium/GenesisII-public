package edu.virginia.vcgr.genii.ui.rns.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.tools.CopyTool;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.utils.Extemporizer;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.dragdrop.ListTransferable;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;

@SuppressWarnings("serial")
public class RNSListTransferable extends ListTransferable
{
    static private Log logger = LogFactory.getLog(RNSListTransferable.class);

    static final public DataFlavor RNS_PATH_LIST_FLAVOR;

    static final public DataFlavor[] SUPPORTED_FLAVORS;

    static {
        try {
            String FLAVOR_PATTERN = "%s;class=\"%s\"";
            RNS_PATH_LIST_FLAVOR = new DataFlavor(String.format(FLAVOR_PATTERN,
                    DataFlavor.javaJVMLocalObjectMimeType, RNSListTransferData.class.getName()));

            SUPPORTED_FLAVORS = new DataFlavor[] { RNS_PATH_LIST_FLAVOR };
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("Unable to create DnD transfer flavors.", cnfe);
        }
    };

    private RNSTree _tree;
    private UIContext _sourceContext;
    private Collection<Pair<RNSTreeNode, RNSPath>> _paths;

    RNSListTransferable(RNSTree tree, UIContext sourceContext,
            Collection<Pair<RNSTreeNode, RNSPath>> paths)
    {
        _sourceContext = sourceContext;
        _paths = new Vector<Pair<RNSTreeNode, RNSPath>>(paths);
        _tree = tree;
        FLAVORS.add(RNS_PATH_LIST_FLAVOR);
    }

    public UIContext getSourceContext()
    {
        return _sourceContext;
    }

    // this can take a while to complete.
    public File renderToFile(RNSPath path)
    {
        if (path == null) return null;
        File localDir = Extemporizer.createTempDirectory("temp-rns", "dir");
        logger.debug("trying to store " + path.toString() + " into temporary path: " + localDir);
        PathOutcome ret = CopyTool.copy(path.toString(), "local:" + localDir, true, true, null);
        if (ret.same(PathOutcome.OUTCOME_SUCCESS)) {
            logger.debug("made a copy of " + path.toString() + " in " + localDir);
            return localDir.listFiles()[0];
        } else {
            logger.debug("failed to make a copy of " + path.toString() + " in " + localDir
                    + " because " + PathOutcome.outcomeText(ret));
        }
        return null;
    }

    @Override
    public boolean loadDataJustInTime(DataFlavor flavor)
    {
        if (flavor == null) return false;
        // need to fill the underlying vector in the base class now.
        for (Pair<RNSTreeNode, RNSPath> pathPair : _paths) {
            super.add(renderToFile(pathPair.second()));
        }
        return (size() != 0);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (flavor == null) return null;
        logger.debug("got into rns list transferable, get data...");
        if (flavor.equals(RNS_PATH_LIST_FLAVOR)) {
            logger.debug("seeing rns path list flavor.");
            return new RNSListTransferData(_tree, _sourceContext, _paths);
        } else {
            logger.debug("into non-rns path case for data, has elems=" + super.size());
            return super.getTransferData(flavor);
        }
    }
}
