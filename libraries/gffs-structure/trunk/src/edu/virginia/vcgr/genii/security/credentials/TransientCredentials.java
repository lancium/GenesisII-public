package edu.virginia.vcgr.genii.security.credentials;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * compatibility object that throws out old credentials. do not use this!
 */
public class TransientCredentials implements Serializable
{
	static final long serialVersionUID = 0L;

	static public Log _logger = LogFactory.getLog(TransientCredentials.class);
}
