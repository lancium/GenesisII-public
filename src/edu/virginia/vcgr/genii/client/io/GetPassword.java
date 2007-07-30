package edu.virginia.vcgr.genii.client.io;

import edu.virginia.vcgr.genii.client.jni.JNIClientBaseClass;

public class GetPassword extends JNIClientBaseClass {

	static public native String getPassword(String prompt);
}
