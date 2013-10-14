package edu.virginia.vcgr.genii.client.rns;

import java.util.Collection;

/**
 * A helper interface that lets the recurser know whether the path is considered to be a file or a
 * directory. It's possible neither could be true also. This also supports a sanity test that is
 * important for deciding whether the node can be entered or not. This will govern whether we
 * descend into the path.
 */
public interface TreeTraversalPathQuery<nodeType>
{
	// returns true if the path seems to exist at all.
	boolean exists(nodeType path);

	// returns true if the node is a directory type.
	boolean isDirectory(nodeType path);

	// reports true if the node represents a file.
	boolean isFile(nodeType path);

	// returns OUTCOME_SUCCESS if the path looks acceptable for recursing into.
	// if the path indicates a detected graph cycle, then "bounce" must be invoked
	// and OUTCOME_BOUNCE should be returned if the condition was not resolved. if
	// the bounce has been dealt with properly, then OUTCOME_CONTINUABLE should be
	// returned instead (and tree traversal and so forth may continue).
	PathOutcome checkPathSanity(nodeType path, TreeTraversalActionAlert<nodeType> bounce);

	// acquires the list of all contents in a directory type node.
	Collection<nodeType> getContents(nodeType path);
}
