package edu.virginia.vcgr.genii.client.rns;

public interface TreeTraversalActionAlert<nodeType>
{
    /**
     * An interface for call-backs that occur when particular assets or locations are hit
     * during the tree traversal.
     */
    public abstract PathOutcome respond(nodeType path);
}
