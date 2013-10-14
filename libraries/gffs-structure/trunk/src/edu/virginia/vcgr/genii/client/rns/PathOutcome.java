package edu.virginia.vcgr.genii.client.rns;

/**
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * These values are standard returns for recursePath. There can also be other values returned by an
 * ActionAlert that are specific to higher-level types.
 */
public class PathOutcome
{
	// total success with no unfixable issues.
	public static final PathOutcome OUTCOME_SUCCESS = new PathOutcome(0);
	// there was an error during the operation that stopped it.
	public static final PathOutcome OUTCOME_ERROR = new PathOutcome(1);
	// tree traversal saw cycle and bounced; not necessarily an error.
	public static final PathOutcome OUTCOME_BOUNCED = new PathOutcome(2);
	// an asset was found to be non-existent.
	public static final PathOutcome OUTCOME_NONEXISTENT = new PathOutcome(3);
	// the action could not be completed due to lack of permission.
	public static final PathOutcome OUTCOME_NO_ACCESS = new PathOutcome(4);
	// the item in question is not empty yet and needs to be.
	public static final PathOutcome OUTCOME_NON_EMPTY = new PathOutcome(5);
	// the item in question is empty but must not be.
	public static final PathOutcome OUTCOME_EMPTY = new PathOutcome(6);
	// the specified object is not the appropriate type.
	public static final PathOutcome OUTCOME_WRONG_TYPE = new PathOutcome(7);
	// the requested operation did not seem to do anything, although we saw no
	// other error conditions.
	public static final PathOutcome OUTCOME_NOTHING = new PathOutcome(8);
	// there is already an existing asset in the location specified.
	public static final PathOutcome OUTCOME_EXISTENT = new PathOutcome(9);
	// the tree traversal had to bail due to too large of a recursive depth.
	public static final PathOutcome OUTCOME_TOO_DEEP = new PathOutcome(10);
	// this is an intermediate outcome that will not be returned to the caller.
	// it indicates that a bounce occurred, but it should not be construed as a failure
	// that should stop the tree traversal.
	public static final PathOutcome OUTCOME_CONTINUABLE = new PathOutcome(11);
	// this outcome indicates that an RNS path is not properly supporting EPIs, which we
	// are currently requiring for our traversals to succeed.
	public static final PathOutcome OUTCOME_UNSUPPORTED = new PathOutcome(12);

	private int _outcome;

	// some comparisons that still work if people construct their own PathOutcomes.
	public boolean same(int outcome)
	{
		return this.getOutcome() == outcome;
	}

	public boolean same(PathOutcome outcome)
	{
		return (outcome != null) && (this.getOutcome() == outcome.getOutcome());
	}

	public boolean differs(int outcome)
	{
		return this.getOutcome() != outcome;
	}

	public boolean differs(PathOutcome outcome)
	{
		return (outcome != null) && (this.getOutcome() != outcome.getOutcome());
	}

	/**
	 * returns a textual representation of the outcomes that are known about by this class.
	 */
	public static String outcomeText(PathOutcome toText)
	{
		if (toText.same(OUTCOME_SUCCESS))
			return "completely successful";
		if (toText.same(OUTCOME_ERROR))
			return "an error occurred during the operation";
		if (toText.same(OUTCOME_BOUNCED))
			return "operation halted due to inappropriate or cyclic path";
		if (toText.same(OUTCOME_NONEXISTENT))
			return "the path does not exist";
		if (toText.same(OUTCOME_NO_ACCESS))
			return "there were insufficient permissions";
		if (toText.same(OUTCOME_NON_EMPTY))
			return "the directory object was non-empty";
		if (toText.same(OUTCOME_EMPTY))
			return "the directory object was empty";
		if (toText.same(OUTCOME_WRONG_TYPE))
			return "the filesystem object was the wrong type";
		if (toText.same(OUTCOME_NOTHING))
			return "the operation seemed to have no effect";
		if (toText.same(OUTCOME_EXISTENT))
			return "there was an existing object in that location";
		if (toText.same(OUTCOME_TOO_DEEP))
			return "tree traversal went too deep, possibly due to cyclic path";
		if (toText.same(OUTCOME_CONTINUABLE))
			return "tree traversal saw a bounce condition but can continue";
		if (toText.same(OUTCOME_UNSUPPORTED))
			return "cannot traverse RNS space when lacking EPIs for path";
		return "Unknown Error";
	}

	public PathOutcome(int outcome)
	{
		this._outcome = outcome;
	}

	public int getOutcome()
	{
		return _outcome;
	}
}
