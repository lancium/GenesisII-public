package edu.virginia.vcgr.genii.client.logging;

public class DLogConstants
{

	static final public String DLOG_CONS_PARMS_NS = "http://vcgr.cs.virginia.edu/construction-parameters/dlog";
	// resource list sub-fields
	static final public String DLOG_RESOURCE_LIST_LIST_ATTR = "listType";
	static final public String DLOG_RESOURCE_LIST_ATTR_EXCLUSIVE = "exclusive";
	static final public String DLOG_RESOURCE_LIST_ATTR_INCLUSIVE = "inclusive";
	static final public String DLOG_RESOURCE_LIST_PORT_TYPES = "portTypes";
	static final public String DLOG_RESOURCE_LIST_PORT_TYPE_NAME = "portType";
	static final public String DLOG_RESOURCE_LIST_RESOURCE_KEYS = "resources";
	static final public String DLOG_RESOURCE_LIST_RESOURCE_KEY = "resourceKey";

	// Entry table fields
	static final public String DLOG_ENTRY_FIELD_DATE = "dated";
	static final public String DLOG_ENTRY_FIELD_LEVEL = "level";
	static final public String DLOG_ENTRY_FIELD_RPCID = "rpcid";
	static final public String DLOG_ENTRY_FIELD_LOGGER = "logger";
	static final public String DLOG_ENTRY_FIELD_MESSAGE = "message";
	static final public String DLOG_ENTRY_FIELD_STACK_TRACE = "stacktrace";

	// Hierarchy table fields
	static final public String DLOG_HIERARCHY_CHILD = "child";
	static final public String DLOG_HIERARCHY_PARENT = "parent";
	static final public String DLOG_HIERARCHY_DATE = "dated";

	// Metadata table fields
	static final public String DLOG_METADATA_EPR = "targetepr";
	static final public String DLOG_METADATA_RPCID = "rpcid";
	static final public String DLOG_METADATA_REQUEST = "request";
	static final public String DLOG_METADATA_RESPONSE = "response";
	static final public String DLOG_METADATA_DATE_SENT = "issuedate";
	static final public String DLOG_METADATA_DATE_RCVD = "returndate";
	static final public String DLOG_METADATA_OP_NAME = "operation";

}
