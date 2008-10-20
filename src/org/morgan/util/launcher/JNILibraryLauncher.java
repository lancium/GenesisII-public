/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.launcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class JNILibraryLauncher
{
	private static boolean isLoaded = false;
	private static ClassLoader loader;
	/*
	private final static String JNI_PACKAGE = 
		"edu.virginia.vcgr.genii.client.jni.gIIlib";
	private final static String JNI_DEPRECATED_PACKAGE = 
		"edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated";
	*/
	private final static String JNI_IO_PACKAGE = 
		"edu.virginia.vcgr.genii.client.jni.gIIlib.io";
	private final static String JNI_MISCELLANEOUS_PACKAGE = 
		"edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous";
	private static final String BASE_DIR_SYSTEM_PROPERTY = 
		"edu.virginia.vcgr.genii.install-base-dir";
	
	public static void initialize()
	{
		try{
			if(!isLoaded)
			{			
				String basedir = System.getProperty(BASE_DIR_SYSTEM_PROPERTY);
				JarDescription description = new JarDescription(basedir + "/jar-desc.xml");			
				loader = description.createClassLoader();				
				isLoaded = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	public static boolean changeDirectory(String targetDirectory){				
		String myClass = JNI_MISCELLANEOUS_PACKAGE + ".JNICdTool";
		String myMethod = "changeDirectory";
		Class<?>[] argTypes = new Class[] {String.class };
		Object[] args = new Object[] { targetDirectory};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	
	public static String getCurrentDirectory(){
		String myClass = JNI_MISCELLANEOUS_PACKAGE + ".JNIPwdTool";
		String myMethod = "getCurrentDirectory";
		Class<?>[] argTypes = null;
		Object[] args = null;
		
		return (String)invoke(myClass, myMethod, argTypes, args);				
	}
	
	public static boolean login(String keystorePath, String password, String certPath){
		String myClass = JNI_MISCELLANEOUS_PACKAGE + ".JNILoginTool";
		String myMethod = "login";
		Class<?>[] argTypes = new Class[] {String.class, String.class , String.class };
		Object[] args = new Object[] { keystorePath , password , certPath };
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);			
	}
	
	public static void logout(){
		String myClass = JNI_MISCELLANEOUS_PACKAGE + ".JNILogoutTool";
		String myMethod = "logout";
		Class<?>[] argTypes = null;
		Object[] args = null;
		
		invoke(myClass, myMethod, argTypes, args);				
	}
	
	/* ************************* IO Functions**************************** */
	
	@SuppressWarnings("unchecked")
	public static Object[] open(String fileName, int requestedDeposition,
			int DesiredAccess, boolean isDirectory){
		String myClass = JNI_IO_PACKAGE + ".JNIOpen";
		String myMethod = "open";
		Class[] argTypes = new Class[] {String.class, Integer.class, 
				Integer.class, Boolean.class};
		Object[] args = new Object[] {fileName, new Integer(requestedDeposition), 
				new Integer(DesiredAccess), new Boolean(isDirectory)};
		
		ArrayList<String> toReturn = (ArrayList<String>)
			invoke(myClass, myMethod, argTypes, args);
		return ((toReturn != null) ? toReturn.toArray() : null);
	}
	
	
	public static byte[] read(int fileHandle, long offset, int length){
		String myClass = JNI_IO_PACKAGE + ".JNIRead";
		String myMethod = "read";
		Class<?>[] argTypes = new Class[] {Integer.class, Long.class, Integer.class};
		Object[] args = new Object[] {new Integer(fileHandle), 
				new Long(Math.abs(offset)), new Integer(Math.abs(length))};
		
		return (byte[])invoke(myClass, myMethod, argTypes, args);				
	}
	
	public static int write(int fileHandle, byte[] data, long offset, int validLength){
		String myClass = JNI_IO_PACKAGE + ".JNIWrite";
		String myMethod = "write";
		Class<?>[] argTypes = new Class[] {Integer.class, byte[].class, Long.class};				

		/*	
			Only not use default if not the same size (i.e. pool buffers may be larger than
			valid length 
		*/
		
		//Bytes to actually use
		byte[] toUse = data;		
		
		if(data.length != validLength){
			toUse = Arrays.copyOf(data, validLength);  //truncates to valid length
		}
		
		Object[] args = new Object[] {new Integer(fileHandle),toUse, new Long(offset)};		
		return (Integer)invoke(myClass, myMethod, argTypes, args);				
	}
	
	public static int truncateAppend(int fileHandle, byte[] data, long offset, int validLength){
		String myClass = JNI_IO_PACKAGE + ".JNIWrite";
		String myMethod = "truncateAppend";
		Class<?>[] argTypes = new Class[] {Integer.class, byte[].class, Long.class};
		
		/*	
			Only not use default if not the same size (i.e. pool buffers may be larger than
			valid length 
		 */
	
		//Bytes to actually use
		byte[] toUse = data;		
	
		if(data.length != validLength){
			toUse = Arrays.copyOf(data, validLength);  //truncates to valid length
		}
		
		Object[] args = new Object[] {new Integer(fileHandle), 
				toUse, new Long(offset)};
		
		return (Integer)invoke(myClass, myMethod, argTypes, args);				
	}
	
	public static boolean close(int fileHandle, boolean deleteOnClose){
		String myClass = JNI_IO_PACKAGE + ".JNIClose";
		String myMethod = "close";
		Class<?>[] argTypes = new Class[] {Integer.class, Boolean.class};
		Object[] args = new Object[] {new Integer(fileHandle), new Boolean(deleteOnClose)};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	
	@SuppressWarnings("unchecked")
	public static Object[] getDirectoryListing(int handle, String target){		
		String myClass = JNI_IO_PACKAGE + ".JNIDirectoryListing";
		String myMethod = "getDirectoryListing";
		Class[] argTypes = new Class[] {Integer.class, String.class};
		Object[] args = new Object[] {new Integer(handle), target};
						
		ArrayList<String> toReturn = (ArrayList<String>)
			invoke(myClass, myMethod, argTypes, args);
		return ((toReturn != null) ? toReturn.toArray() : null);
	}
	
	public static boolean rename(int handle, String destination){
		String myClass = JNI_IO_PACKAGE + ".JNIRename";
		String myMethod = "rename";
		Class<?>[] argTypes = new Class[] {Integer.class, String.class};
		Object[] args = new Object[] {handle, destination};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	
	/* ************************* END IO Functions**************************** */
	
	private static Object invoke(String cls, String mtd, Class<?>[] argTypes, Object[] args){
		if(!isLoaded){
			initialize();
		}		
		Thread.currentThread().setContextClassLoader(loader);			
		
		try
		{				
			Class<?> cl = loader.loadClass(cls);		
			Method method = ((argTypes != null)? cl.getMethod(mtd, argTypes) :
				cl.getMethod(mtd));	
			return ((args != null) ? method.invoke(null, args) :
				method.invoke(null));			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}			
	}
	
	/*  NO LONGER SUPPORTED */
	
	/*
	@SuppressWarnings("unchecked")
	public static Object[] getInformation(String path){
		String myClass = JNI_PACKAGE + ".JNIGetInformationTool";										 
		String myMethod = "getInformation";
		Class[] argTypes = new Class[] {String.class};
		Object[] args = new Object[] {path};
				
		ArrayList<String> toReturn = (ArrayList<String>)
			invoke(myClass, myMethod, argTypes, args);
		return ((toReturn != null) ? toReturn.toArray() : null);
	}
	*/
	
	/*
	@SuppressWarnings("unchecked")
	public static Object[] getDirectoryListing(String directory, String target){		
		String myClass = JNI_IO_PACKAGE + ".JNIDirectoryListing";
		String myMethod = "getDirectoryListingOld";
		Class[] argTypes = new Class[] {String.class, String.class};
		Object[] args = new Object[] { directory, target };
						
		ArrayList<String> toReturn = (ArrayList<String>)
			invoke(myClass, myMethod, argTypes, args);
		return ((toReturn != null) ? toReturn.toArray() : null);
	}
	*/
	
	/*
	public static boolean makeDirectory(String newDirectory){
		String myClass = JNI_PACKAGE + ".JNIMkDirTool";
		String myMethod = "makeDirectory";
		Class<?>[] argTypes = new Class[] {String.class };
		Object[] args = new Object[] { newDirectory};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);					
	}
	*/
	
	/*
	public static boolean remove(String target, boolean recursive, boolean force){
		String myClass = JNI_PACKAGE + ".JNIRmTool";
		String myMethod = "remove";
		Class<?>[] argTypes = new Class[] {String.class, 
				Boolean.class, Boolean.class};
		Object[] args = new Object[] {target, 
				new Boolean(recursive), new Boolean(force)};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	*/
	
	/*
	public static boolean copy(String source, String destination,boolean srcLocal, boolean dstLocal){
		String myClass = JNI_PACKAGE + ".JNICpTool";
		String myMethod = "copy";
		Class<?>[] argTypes = new Class[] {String.class, String.class, 
					Boolean.class, Boolean.class};
		Object[] args = new Object[] {source, destination, 
				new Boolean(srcLocal), new Boolean(dstLocal)};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	*/
	
	/*
	public static boolean move(String source, String destination){
		String myClass = JNI_PACKAGE + ".JNIMvTool";
		String myMethod = "move";
		Class<?>[] argTypes = new Class[] {String.class, String.class};
		Object[] args = new Object[] {source, destination};
		
		return (Boolean)invoke(myClass, myMethod, argTypes, args);				
	}
	*/
}
