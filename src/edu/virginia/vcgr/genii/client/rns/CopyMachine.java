package edu.virginia.vcgr.genii.client.rns;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.tools.MkdirTool;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.DataTransferStatistics;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

/**
 * A collection of useful functions for copying to and from GeniiPath locations.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class CopyMachine
{
    static private Log _logger = LogFactory.getLog(CopyMachine.class);
    private String _source;
    private String _target;
    private String _currentTarget; // the directory we are currently targeting.
    private PrintWriter _stderr; // gets error reports during the copy process.
    TaskProgressListener _updates;
    boolean _force; // true if we should overwrite things that get in the way.

    final static int MAX_COPY_DEPTH = 16; // we do not try to copy hierarchies deeper than this.

    /**
     * Constructs a copy machine for making a copy of the source to the target. If the sink is
     * non-null, we will use it to record updates as files and directories are seen.
     */
    public CopyMachine(String sourceIn, String targetIn, TaskProgressListener updateSink,
            boolean forceIn, PrintWriter stderrIn)
    {
        _source = sourceIn;
        _target = targetIn;
        _currentTarget = targetIn;
        _updates = updateSink;
        _force = forceIn;
        _stderr = stderrIn;
        _logger.debug("built a CopyMachine: src=" + _source + " dest=" + _target + " force="
                + _force);
    }

    public PrintWriter getStderr()
    {
        return _stderr;
    }

    /**
     * traverses a directory tree at the source (even if just a file) and replicates the source into
     * the target path, which must be a directory.
     */
    public PathOutcome copyTree()
    {
        if ((_source == null) || (_target == null))
            return PathOutcome.OUTCOME_NOTHING;
        _logger.debug("into copyTree on " + _source);
        _currentTarget = _target;  // reset for this copy.
        // we create paths in target on directory entry, and we copy files when
        // we hit them.
        GeniiPathRecurser cloner = new GeniiPathRecurser(new EnterDirectoryGeniiPath(this),
                new ExitDirectoryGeniiPath(this), new CopyFileGeniiPath(this), null);
        cloner.setMaximumRecursionDepth(MAX_COPY_DEPTH);
        GeniiPath targetCheck = new GeniiPath(_target);
        if (targetCheck.exists()) {
            // copying to an existing destination is simpler.
            return cloner.recursePath(new GeniiPath(_source));
        }
        // we do not have a target, so we need to create one.
        GeniiPath sourceCheck = new GeniiPath(_source);
        if (sourceCheck.isDirectory()) {
            // our source is a directory, so we'll stuff things into it.
            try {
                ArrayList<String> dirs = new ArrayList<String>();
                dirs.add(_target);
                int worked = MkdirTool.makeDirectory(false, null, dirs, _stderr);
                if (worked != 0) {
                    _logger.error("failed to make directory in target of " + targetCheck);
                    return PathOutcome.OUTCOME_NO_ACCESS;
                }
            } catch (Throwable cause) {
                _logger.debug("failed to make missing directory, got exception", cause);
                return PathOutcome.OUTCOME_NO_ACCESS;
            }
            // copy all of the contents in the source instead.
            GeniiPathHierarchyHelper gph = new GeniiPathHierarchyHelper();
            for (GeniiPath path : gph.getContents(sourceCheck)) {
                _logger.debug("copying from " + path + " to target " + _target);
                PathOutcome outcome = cloner.recursePath(path);
                // bail if we see a problem.
                if (outcome.differs(PathOutcome.OUTCOME_SUCCESS)) {
                    _logger.error("failed to copy " + path + " to target " + _target);
                    return outcome;
                }
            }
            return PathOutcome.OUTCOME_SUCCESS;
        }
        // this is just a file being copied to a new name.
        return copyOneFile(_source, _target);
    }

    /**
     * copies a single file from the source location to the target. if the target is a directory,
     * the file will be created inside of it. if the target is a file, then it is overwritten.
     */
    public static PathOutcome copyOneFile(String sourceIn, String targetIn)
    {
        if ((sourceIn == null) || (targetIn == null))
            return PathOutcome.OUTCOME_NOTHING;
        String sourceName = null;
        OutputStream out = null;
        InputStream in = null;
        PathOutcome toReturn = PathOutcome.OUTCOME_ERROR;
        _logger.debug("copyOneFile from " + sourceIn + " to " + targetIn);
        GeniiPath source = new GeniiPath(sourceIn);
        if (!source.exists()) {
            _logger.error(String.format("Unable to find source file %s!", source));
            return PathOutcome.OUTCOME_NONEXISTENT;
        }
        if (!source.isFile()) {
            _logger.error(String.format("Source path %s is not a file!", source));
            return PathOutcome.OUTCOME_WRONG_TYPE;
        }
        sourceName = source.getName();
        GeniiPath target = new GeniiPath(targetIn);

        // put the file into the target directory, if it is a directory. if target is a file, we
        // just wipe it out with the file we were asked to copy.
        if (target.exists() && target.isDirectory()) {
            target = new GeniiPath(String.format("%s/%s", target, sourceName));
        } else if (target.exists() && !target.isFile()) {
            _logger.error("trying to copy onto a non-file object for target: " + target);
            return PathOutcome.OUTCOME_WRONG_TYPE;
        }
        try {
            in = source.openInputStream();
        } catch (Throwable cause) {
            _logger.error("failed to open input stream for copying", cause);
            return PathOutcome.OUTCOME_NO_ACCESS;
        }
        try {
            out = target.openOutputStream();
        } catch (Throwable cause) {
            _logger.error("failed to open output stream for copying", cause);
            try {
                in.close();
            } catch (Throwable c) {
            } // ignore while bailing out.
            return PathOutcome.OUTCOME_NO_ACCESS;
        }
        toReturn = copy(in, out);
        try {
            out.flush();
        } catch (Throwable cause) {
            _logger.error("failed to flush I/O stream", cause);
            toReturn = PathOutcome.OUTCOME_ERROR;
        }
        try {
            StreamUtils.close(in);
            StreamUtils.close(out);
        } catch (Throwable cause) {
            _logger.error("failed to close I/O streams", cause);
            toReturn = PathOutcome.OUTCOME_ERROR;
        }
        return toReturn;
    }

    /**
     * copies bytes from the input stream to the output stream.
     */
    public static PathOutcome copy(InputStream in, OutputStream out)
    {
        if ((in == null) || (out == null))
            return PathOutcome.OUTCOME_NOTHING;
        try {
            DataTransferStatistics stat = StreamUtils.copyStream(in, out);
            // we have to take the result from above as good enough (that there was no exception
            // from running the copy), because even if there were zero bytes copied, that might
            // be right, for a zero length file.
            _logger.debug("Transferred " + stat.bytesTransferred() + " from input stream to output.");
            return PathOutcome.OUTCOME_SUCCESS;
        } catch (Throwable cause) {
            _logger.error("failed to copy the input stream to the output stream", cause);
            return PathOutcome.OUTCOME_ERROR;
        }
    }

    private static class CopyFileGeniiPath implements TreeTraversalActionAlert<GeniiPath>
    {
        private CopyMachine parent;

        public CopyFileGeniiPath(CopyMachine cm)
        {
            parent = cm;
        }

        @Override
        public PathOutcome respond(GeniiPath path)
        {
            if (path == null)
                return PathOutcome.OUTCOME_NOTHING;
            _logger.debug("CopyFileGeniiPath hit the action for " + path.toString());
            GeniiPath targetFile = new GeniiPath(parent._currentTarget + "/" + path.getName());
            if (!parent._force && targetFile.exists()) {
                _logger.error("force is false, and the target file already exists at path: "
                        + targetFile.toString());
                return PathOutcome.OUTCOME_EXISTENT;
            }
            if (parent._updates != null) {
                String partialDir = (new GeniiPath(path.getParent())).getName();
                parent._updates.updateSubTitle(partialDir + "/" + path.getName());
            }
            return copyOneFile(path.toString(), targetFile.toString());
        }
    }

    private static class EnterDirectoryGeniiPath implements TreeTraversalActionAlert<GeniiPath>
    {
        private CopyMachine parent;

        public EnterDirectoryGeniiPath(CopyMachine cm)
        {
            parent = cm;
        }

        @Override
        public PathOutcome respond(GeniiPath path)
        {
            if (path == null)
                return PathOutcome.OUTCOME_NOTHING;
            _logger.debug("EnterDirectoryGeniiPath hit the action for " + path.toString());
            parent._currentTarget = parent._currentTarget.concat("/" + path.getName());
            _logger.debug("added to current target, now: " + parent._currentTarget);
            GeniiPath target = new GeniiPath(parent._currentTarget);
            if (target.exists()) {
                if (!parent._force || target.isFile())
                    return PathOutcome.OUTCOME_EXISTENT;
                // a directory that's in the way is not a problem in force mode.
                return PathOutcome.OUTCOME_SUCCESS;
            }
            ArrayList<String> files = new ArrayList<String>(0);
            files.add(parent._currentTarget);
            try {
                int worked = MkdirTool.makeDirectory(false, null, files, parent._stderr);
                if (worked != 0) {
                    _logger.error("failed to make directory in target of " + parent._currentTarget);
                    return PathOutcome.OUTCOME_NO_ACCESS;
                }
            } catch (Throwable cause) {
                _logger.error("entering directory creation attempt raised exception for: "
                        + parent._currentTarget, cause);
                return PathOutcome.OUTCOME_ERROR;
            }
            return PathOutcome.OUTCOME_SUCCESS;
        }
    }

    private static class ExitDirectoryGeniiPath implements TreeTraversalActionAlert<GeniiPath>
    {
        private CopyMachine parent;

        public ExitDirectoryGeniiPath(CopyMachine cm)
        {
            parent = cm;
        }

        @Override
        public PathOutcome respond(GeniiPath path)
        {
            if (path == null)
                return PathOutcome.OUTCOME_NOTHING;
            _logger.debug("ExitDirectoryGeniiPath hit the action for " + path.toString());
            // peel off a directory from the current target.
            GeniiPath tempPath = new GeniiPath(parent._currentTarget);
            String just_dir = tempPath.getParent();
            parent._currentTarget = tempPath.pathType().toString() + ":" + just_dir;
            _logger.debug("trimmed current target down to " + parent._currentTarget);
            return PathOutcome.OUTCOME_SUCCESS;
        }
    }

}
