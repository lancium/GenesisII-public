package edu.virginia.vcgr.genii.client.rns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/**
 * Helper class for directory tree traversals on RNS paths.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class RNSPathHierarchyHelper implements TreeTraversalPathQuery<RNSPath>
{
    static private Log _logger = LogFactory.getLog(RNSPathHierarchyHelper.class);

    // tracks cycles during the tree traversal.
    // idea of using hash set for catching cycles from Mike Saravo.
    private HashSet<URI> epis = new HashSet<URI>();

    public RNSPathHierarchyHelper()
    {
    }

    @Override
    public boolean exists(RNSPath path)
    {
        if (path == null) return false;
        return path.exists();
    }

    /**
     * some basic tests on the path provided.  true is returned if it looks like a normal
     * file or directory at the location.
     */
    public static PathOutcome checkPathIsNormal(RNSPath path)
    {
        if (path == null) return PathOutcome.OUTCOME_NOTHING;
        // check some characteristics of this node.
        if (!path.exists()) {
            _logger.warn("path no longer exists: " + path.pwd());
            return PathOutcome.OUTCOME_NONEXISTENT;
        }
        try {
            TypeInformation info = new TypeInformation(path.getEndpoint());
            boolean bounce_it = false;
            if (!info.isRNS() && !info.isEnhancedRNS() && !info.isRByteIO()) {
                _logger.warn("RNS checkPath bouncing non-RNS & non-RByteIO type at: " + path.pwd());
                bounce_it = true;
            }
/* always bouncing on resource forks cannot be considered reasonable.  exports
   are resource forks.  jobs that we submit to queue submission points get
   considered to be resource forks afterwards.  we need to be able to copy and
   delete into these unfettered, and just rely on the authorization to keep
   people from messing with more than they should.
*/
//hmmm: flag needed to enable this to just dip into the resource fork.
            if (!bounce_it && info.isResourceFork()) {
                _logger.warn("RNS checkPath bouncing resource fork at: " + path.pwd());
                bounce_it = true;
            }
            if (bounce_it) {
                PathOutcome ret = PathOutcome.OUTCOME_BOUNCED;
                return ret;
            }
        } catch (Throwable cause) {
            _logger.debug("caught exception in RNS checkPathIsNormal", cause);
            return PathOutcome.OUTCOME_ERROR;
        }
        return PathOutcome.OUTCOME_SUCCESS;
    }

    @Override
    public PathOutcome checkPathSanity(RNSPath path, TreeTraversalActionAlert<RNSPath> bounce)
    {
        PathOutcome ret = checkPathIsNormal(path);
        if (ret.differs(PathOutcome.OUTCOME_BOUNCED) && ret.differs(PathOutcome.OUTCOME_SUCCESS))
            return ret;
        if (ret.same(PathOutcome.OUTCOME_BOUNCED)) {
            if (bounce != null)
                ret = bounce.respond(path);
            return ret;
        }
        // phase 2: check for whether we have seen this path before based on its EPI.
        WSName epname;
        try {
            epname = new WSName(path.getEndpoint());
        } catch (RNSPathDoesNotExistException e) {
            _logger.error("RNS checkPath problem checking path: " + path.toString());
            return PathOutcome.OUTCOME_NONEXISTENT;
        }
        if ( (epname == null) || (epname.getEndpointIdentifier() == null) ) {
            _logger.error("RNS checkPath problem getting EPI for: " + path.toString());
            return PathOutcome.OUTCOME_UNSUPPORTED;
        }
        if (epis.contains(epname.getEndpointIdentifier())) {
            // we have already been here.
            String msg = "RNS checkPath detected cycle at path: " + path.pwd();
            _logger.debug(msg);
            ret = PathOutcome.OUTCOME_BOUNCED;
            if (bounce != null)
                ret = bounce.respond(path);
            return ret;
        } else {
            epis.add(epname.getEndpointIdentifier());
            return PathOutcome.OUTCOME_SUCCESS;
        }
    }

    @Override
    public boolean isDirectory(RNSPath path)
    {
        if (path == null) return false;
        return (new GeniiPath(path.pwd())).isDirectory();
    }

    @Override
    public boolean isFile(RNSPath path)
    {
        if (path == null) return false;
        return (new GeniiPath(path.pwd())).isFile();
    }

    @Override
    public Collection<RNSPath> getContents(RNSPath path)
    {
        if (path == null) return new ArrayList<RNSPath>();
        try {
            return path.listContents();
        } catch (Throwable cause) {
            _logger.error("failed to list path contents for " + path.toString(), cause);
            return new ArrayList<RNSPath>();
        }
    }
}
