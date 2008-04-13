package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import java.io.IOException;
import java.util.ArrayList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.RandomByteIOFileDescriptor;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.StreamableByteIOFileDescriptor;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIOpen extends JNILibraryBase{
	
	/* Requested deposition */
	final static int SUPERSEDE = 0; //Delete + Create
	final static int OPEN = 1;
	final static int CREATE = 2;
	final static int OPEN_IF = 3;
	final static int OVERWRITE = 4; // Truncate
	final static int OVERWRITE_IF = 5;
	
	/* Desired Access */
	final static int FILE_READ_DATA = 1;
	final static int FILE_WRITE_DATA = 2;
	final static int FILE_APPEND_DATA = 4;
	final static int FILE_EXECUTE = 8; //Don't handle
	final static int DELETE = 16;	//Don't handle
	
	public static ArrayList<String> open(String fileName, Integer requestedDeposition,
			Integer DesiredAccess, Boolean isDirectory){
		DataTracker tracker = DataTracker.getInstance();
		IFSResource resource;		
		ArrayList<String> toReturn = new ArrayList<String>();		
		int fileHandle = -1;
		
		boolean isAppend = (DesiredAccess & FILE_APPEND_DATA) > 0;
		boolean isRead = (DesiredAccess & FILE_READ_DATA) > 0;
		boolean isWrite = (DesiredAccess & FILE_WRITE_DATA) > 0;
		boolean isTruncate = (requestedDeposition & OVERWRITE) > 0 ;
		
		String fileType = isDirectory ? "directory " : "file ";
		
		//Check if path is valid
		if(!JNIGetInformationTool.checkIfValidPath(fileName)){
			return null;
		}			
			
		try{			
			tryToInitialize();					
			
			//Alright time to do File stuff				
			RNSPath current = RNSPath.getCurrent();
			RNSPath filePath;	
			
			if(JNILibraryBase.DEBUG){
				System.out.print("GenesisII:  Attempting to open " + fileType + " " + fileName + 
						" with deposition ");
			}
			
			//Check the deposition and attempt RNS stuff accordingly
			switch(requestedDeposition){
				case SUPERSEDE:
					System.out.println("supersede");
					filePath =  current.lookup(fileName, RNSPathQueryFlags.DONT_CARE);
					if(filePath != null && filePath.exists()){
						filePath.delete();						
					}
					//Should just go straight into the CREATE (no break)
				case CREATE:
					System.out.println("create");
					filePath = current.lookup(fileName, RNSPathQueryFlags.MUST_NOT_EXIST);
					if(isDirectory){
						filePath.mkdir();
					}
					else{
						filePath.createFile();
					}					
					break;					
				case OPEN:
				case OVERWRITE:
					if(requestedDeposition == OPEN)
						System.out.println("open");					
					else
						System.out.println("overwrite");					
					
					filePath = current.lookup(fileName, RNSPathQueryFlags.MUST_EXIST);
					break;
				case OPEN_IF:
				case OVERWRITE_IF:
					if(requestedDeposition == OPEN)
						System.out.println("conditional open");					
					else
						System.out.println("conditional overwrite");
					
					filePath = current.lookup(fileName, RNSPathQueryFlags.DONT_CARE);
					if(!filePath.exists()){
						if(isDirectory){
							filePath.mkdir();
						}
						else{
							filePath.createFile();
						}	
					}
					break;
				default:
					System.err.println("not supported");
					return null;					
			}										
			
			fileHandle = tracker.atomicGetAndIncrementHandle();
			
			//If Directory, all we need is local copy, otherwise open file
			if(filePath.isDirectory()){		
				resource = new IFSDirectory(filePath);
			}
			else{
				//Check the type of ByteIO and create it according to the options specified
				EndpointReferenceType epr = filePath.getEndpoint();
				TypeInformation typeInfo = new TypeInformation(epr);
				if (typeInfo.isRByteIO()){	
					resource = new RandomByteIOFileDescriptor(filePath, epr, isRead, isWrite, isAppend, isTruncate);
																												
				} else if (typeInfo.isSByteIO()){
					resource = new StreamableByteIOFileDescriptor(filePath,
							epr, isRead, isWrite, isAppend);
					if(isTruncate){
						System.out.println("GenesisII:  Truncate not supported on SByteIO");
					}
				} else if (typeInfo.isSByteIOFactory())	{
					throw new IOException("SByteIO is unimplemented.");
				} else
				{
					throw new IOException("The path \"" + fileName + 
						"\" refers to an object that isn't a file.");
				}							
			}				
			
			tracker.putResource(fileHandle, (IFSResource)resource);
			
			//Add information to return
			toReturn.add(String.valueOf(fileHandle));
			toReturn.addAll(JNIGetInformationTool.getInformationFromRNS(filePath));			
			return toReturn;	
		}
		catch(Exception e){
			e.printStackTrace();
			tracker.removeResource(fileHandle);
			return null;						
		}				
	}
}
