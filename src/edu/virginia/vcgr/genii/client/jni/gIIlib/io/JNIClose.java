package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSDirectory;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSFile;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSResource;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;


public class JNIClose {
	public static Boolean close(Integer handle, Boolean deleteOnClose){		
		IFSResource resource = DataTracker.getInstance().getResource(handle);
		
		if(resource == null){
			System.out.println("Invalid handle");			
			return false;
		}
		
		if(resource.isDirectory()){
			IFSDirectory directory = (IFSDirectory)resource;
			try{
				directory.close();
				if(deleteOnClose){
					System.out.println("Removing directory " + resource.getRNSPath());
					resource.getRNSPath().delete();						
				}				
			}catch(RNSPathDoesNotExistException e){
				System.err.println("No such path exists in Genesis");
			} catch (Exception e){
				e.printStackTrace(new PrintStream(System.err));
			}
		}		
		else{
			IFSFile file = (IFSFile)resource;
			try{
				file.close();
				if(deleteOnClose){
					System.out.println("Deleting file " + resource.getRNSPath());
					resource.getRNSPath().delete();
				}
			}catch(RNSPathDoesNotExistException e){
				System.err.println("No such path exists in Genesis");
			} catch (Exception e){
				e.printStackTrace(new PrintStream(System.err));
			}					
		}	
		DataTracker.getInstance().removeResource(handle);			
		return true;
	}
}
