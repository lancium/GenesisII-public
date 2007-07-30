/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package edu.virginia.vcgr.genii.client.ser;

import java.io.IOException;
import java.io.Reader;

/**
 * StringBufferReader based on StringReader (Sun's code) code in Java.
 */
public class StringBufferReader extends Reader {
    
    private StringBuffer str;
    private int length;
    private int next = 0;
    private int mark = 0;
    
    /**
     * Create a new string buffer reader.
     *
     * @param s  StringBuffer providing the character stream.
     */
    public StringBufferReader(StringBuffer s) {
	this.str = s;
	this.length = s.length();
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
	if (str == null) {
	    throw new IOException("Stream closed");
        }
    }

    /**
     * Read a single character.
     *
     * @return     The character read, or -1 if the end of the stream has been
     *             reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read() throws IOException {
	synchronized (lock) {
	    ensureOpen();
	    if (next >= length) {
		return -1;
            }
	    return str.charAt(next++);
	}
    }

    /**
     * Read characters into a portion of an array.
     *
     * @param      cbuf  Destination buffer
     * @param      off   Offset at which to start writing characters
     * @param      len   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
	synchronized (lock) {
	    ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
	    if (next >= length) {
		return -1;
            }
	    int n = Math.min(length - next, len);
	    str.getChars(next, next + n, cbuf, off);
	    next += n;
	    return n;
	}
    }

    /**
     * Skip characters.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public long skip(long ns) throws IOException {
	synchronized (lock) {
	    ensureOpen();
	    if (next >= length) {
		return 0;
            }
	    long n = Math.min(length - next, ns);
	    next += n;
	    return n;
	}
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     *
     * @exception  IOException  If the stream is closed
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return true;
        }
    }

    /**
     * Tell whether this stream supports the mark() operation, which it does.
     */
    public boolean markSupported() {
	return true;
    }

    /**
     * Mark the present position in the stream.  Subsequent calls to reset()
     * will reposition the stream to this point.
     *
     * @param  readAheadLimit  Limit on the number of characters that may be
     *                         read while still preserving the mark.  Because
     *                         the stream's input comes from a string, there
     *                         is no actual limit, so this argument must not
     *                         be negative, but is otherwise ignored.
     *
     * @exception  IllegalArgumentException  If readAheadLimit is < 0
     * @exception  IOException  If an I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
	if (readAheadLimit < 0){
	    throw new IllegalArgumentException("Read-ahead limit < 0");
	}
	synchronized (lock) {
	    ensureOpen();
	    mark = next;
	}
    }

    /**
     * Reset the stream to the most recent mark, or to the beginning of the
     * string if it has never been marked.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void reset() throws IOException {
	synchronized (lock) {
	    ensureOpen();
	    next = mark;
	}
    }

    /**
     * Close the stream.
     */
    public void close() {
	str = null;
    }
    
}
