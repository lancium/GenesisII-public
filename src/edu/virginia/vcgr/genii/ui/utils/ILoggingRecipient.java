package edu.virginia.vcgr.genii.ui.utils;

// Author: Chris Koeritz

/**
 * An interface for any object that can accept diagnostic info.
 */
public interface ILoggingRecipient
{
  public boolean consumeLogging(String message, Throwable cause);
}

