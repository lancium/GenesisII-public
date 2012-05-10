package edu.virginia.vcgr.genii.client.cache.unified;

import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

public class CacheUtils {
	
	public static String getContainerId(WSName wsName) {
		if (!wsName.isValidWSName()) return null;
		return getContainerId(wsName.getEndpoint());
	}

	public static String getContainerId(EndpointReferenceType epr) {
		try {
			return EPRUtils.getGeniiContainerID(epr).toString();
		} catch (Exception ex) {
			//not a GenesisII resource
			return null;
		}
	}
	
	public static String getNamespaceForByteIOAttributes(EndpointReferenceType byteIO) {
		TypeInformation info = new TypeInformation(byteIO);
		if (info.isRByteIO()) {
			 return ByteIOConstants.RANDOM_BYTEIO_NS;
		} else {
			return ByteIOConstants.STREAMABLE_BYTEIO_NS;
		}
	}
	
	public static URI getEPI(EndpointReferenceType endPoint) {
		WSName wsName = new WSName(endPoint);
		if (!wsName.isValidWSName()) return null;
		return wsName.getEndpointIdentifier(); 
	}
	
	public static String getEPIString(EndpointReferenceType endPoint) {
		WSName wsName = new WSName(endPoint);
		if (!wsName.isValidWSName()) return null;
		return wsName.getEndpointIdentifier().toString(); 
	}
	
	public static String getStackTraceString() {
		StringBuilder buffer = new StringBuilder();
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement traceElement : stackTrace) {
			String className = traceElement.getClassName();
			buffer.append(className).append(":");
			String methodName = traceElement.getMethodName();
			buffer.append(methodName).append(" at line ");
			buffer.append(traceElement.getLineNumber());
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
