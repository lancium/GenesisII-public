package edu.virginia.vcgr.genii.client.rns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for traversing a filesystem-like tree and travelling to all sub-nodes. This is generic
 * based on the nodeType involved, and requires creation of a helper class based on TreeTraversalPathQuery to
 * answer questions about the targeted filesystem.
 * Note that instances of this class are intended for a single thread to use at a time; they are not
 * thread-safe for use from different threads.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class TreeTraverser<nodeType>
{
    static private Log _logger = LogFactory.getLog(TreeTraverser.class);

    // the functions we will invoke when we see certain things in the tree.
    private TreeTraversalActionAlert<nodeType> _dirEnter;
    private TreeTraversalActionAlert<nodeType> _dirExit;
    private TreeTraversalActionAlert<nodeType> _fileAct;
    private TreeTraversalActionAlert<nodeType> _bounce;

    // an object that can answer important questions for us about the nodeType.
    private TreeTraversalPathQuery<nodeType> _querier;
    
    // we will not try to keep traversing if the directory depth goes beyond this limit. 
    private int _maximumRecursionDepth = 100;

    private int _depth;  // tracks how far we have delved into the tree.
    
    /**
     * Constructs an object for recursing a directory tree. This takes a path query object for
     * navigating the nodeType properly.  It also will accept 4 alert methods to be invoked when
     * (1) entering a directory, (2) leaving a directory, (3) hitting a file, and (4)
     * encountering a cycle in the tree hierarchy (due to links, for example).  Any of the alert
     * methods may be null.
     */
    public TreeTraverser(TreeTraversalPathQuery<nodeType> querierMethod,
            TreeTraversalActionAlert<nodeType> enterDirectoryAlert, TreeTraversalActionAlert<nodeType> exitDirectoryAlert,
            TreeTraversalActionAlert<nodeType> fileAlert, TreeTraversalActionAlert<nodeType> bounceAlert)
    {
        _querier = querierMethod;
        _dirEnter = enterDirectoryAlert;
        _dirExit = exitDirectoryAlert;
        _fileAct = fileAlert;
        _bounce = bounceAlert;
    }
    
    /**
     * enables users to set their preferred maximum depth of directory traversal.
     */
    public void setMaximumRecursionDepth(int newMaxDepth) {
        _maximumRecursionDepth = newMaxDepth;
    }

    /**
     * Traverses a directory tree structure and performs operations on the file and directory nodes
     * found. The directory and file ActionAlerts will be invoked when the traversal encounters
     * these items during the recursion. The bounceAlert will be called when a cycle in the
     * traversal graph has been detected. This will return zero if no errors were encountered during
     * tree traversal.
     */
    public PathOutcome recursePath(nodeType path)
    {
        if ((path == null) || (_querier == null)) return PathOutcome.OUTCOME_NOTHING;
        _logger.debug("into recursePath");
        _depth = 0;  // reset the depth now.
        try {
            PathOutcome ret = innerRecursePath(path);
            // normal returns are considered successes.
            if (ret.same(PathOutcome.OUTCOME_SUCCESS)) return PathOutcome.OUTCOME_SUCCESS;
            if (ret.same(PathOutcome.OUTCOME_CONTINUABLE)) return PathOutcome.OUTCOME_SUCCESS;
            // anything else is a failure return.
            return ret;
        } catch (Throwable cause) {
            _logger.error("recursePath saw unexpected exception; stifling it and returning error.", cause);
        }
        return PathOutcome.OUTCOME_ERROR;  // failure if got to here.
    }

    /**
     * Processes a path while avoiding cycles. The hash set should be empty to start with.
     */
    private PathOutcome innerRecursePath(nodeType path)
    {
        if ((path == null) || (_querier == null)) return PathOutcome.OUTCOME_NOTHING;
        _logger.debug("entered innerRecursePath on: " + path.toString());
        _depth++;  // increment depth because we are supposedly dipping down.
        if (_depth > _maximumRecursionDepth) {
            _depth--;
            return PathOutcome.OUTCOME_TOO_DEEP;
        }
        PathOutcome ret = _querier.checkPathSanity(path, _bounce); 
        if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
            _logger.info("checkPathSanity considers this a bad path: " + path.toString());
            _depth--;
            return ret;
        }

        // if we're just looking at a file, then we're all done recursing.
        if (_querier.isFile(path)) {
            ret = PathOutcome.OUTCOME_SUCCESS;
            if (_fileAct != null)
                ret = _fileAct.respond(path);
            _depth--;
            return ret;
        }
        if (!_querier.isDirectory(path)) {
            _logger.warn("abandoning recursion, since node is not a directory or file");
            _depth--;
            return PathOutcome.OUTCOME_ERROR;
        }

        if (_dirEnter != null) {
            ret = _dirEnter.respond(path);
            if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
                _logger.info("dirEnter alert told us to stop recursing.");
                _depth--;
                return ret;            
            }
        }

        for (nodeType contained : _querier.getContents(path)) {
            // logger.debug("now operating on " + contained.toString());
            if (_querier.isDirectory(contained)) {
                _logger.debug("found dir, diving into: " + contained.toString());
                try {
                    // dive into directories first, so we do depth first.
                    ret = innerRecursePath(contained);
                    _logger.debug("came back out of: " + contained.toString());
                    // continuable bouncing is fine, we just need to stop working on this path.
                    if (ret.differs(PathOutcome.OUTCOME_CONTINUABLE) && 
                            ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
                        _depth--;
                        return ret;  // stop running recursion.
                    }
                } catch (Throwable cause) {
                    _logger.debug("recursePath encountered unexpected exception while calling innerRecursePath", cause);
                    _depth--;
                    return PathOutcome.OUTCOME_ERROR;
                }
            } else if (_querier.isFile(contained)) {
                _logger.debug("found file at: " + contained.toString());
                if (_fileAct != null) {
                    ret = _fileAct.respond(contained);
                    if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
                        _logger.info("fileAct alert told us to stop recursing.");
                        _depth--;
                        return ret;
                    }                    
                }
            } else {
                _logger.warn("hit unknown node type (not file, not dir) during traversal: " + contained.toString());
            }
        }

        if (_dirExit != null) {
            ret = _dirExit.respond(path);
            if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
                _logger.info("dirExit alert told us to stop recursing.");
                _depth--;
                return ret;
            }                    
        }
        _depth--;
        return PathOutcome.OUTCOME_SUCCESS;
    }

}
