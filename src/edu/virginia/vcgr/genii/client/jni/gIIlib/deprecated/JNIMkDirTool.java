package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import org.ggf.rns.RNSPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIMkDirTool extends JNILibraryBase {
	
	public static Boolean makeDirectory(String sPath){
		tryToInitialize();
		
		try{
		
			boolean createParents = false;
			EndpointReferenceType service = null;
			
			ICallingContext ctxt = ContextManager.getCurrentContext();
			
			RNSPath path = ctxt.getCurrentPath();
			
			RNSPath newDir = path.lookup(sPath, RNSPathQueryFlags.MUST_NOT_EXIST);
			
			path = newDir;
			
			if (service == null)
			{
				if (createParents)
					path.mkdirs();
				else
					path.mkdir();
			} 
			else
			{
				RNSPath parent = path.getParent();
				
				if (!parent.exists())
				{
					System.out.println("Can't create directory \"" + path.pwd() 
						+ "\".");
					return false;
				}
				
				if (!(new TypeInformation(parent.getEndpoint()).isRNS()))
				{
					System.out.println("\"" + parent.pwd() + 
						"\" is not a directory.");
					return false;
				}
				
				RNSPortType rpt = ClientUtils.createProxy(
					RNSPortType.class, service);
				path.link(rpt.add(null).getEntry_reference());
			}
			
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
