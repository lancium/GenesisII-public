package edu.virginia.vcgr.genii.client.gui.bes;

import java.rmi.RemoteException;
import java.util.Map;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;

public class BESManager
{
	static public String createBES(String path, BESListModel model)
	{		
		EndpointReferenceType newBES = null;
		RNSPath target = null;
		
		try
		{
			Map<String, ContainerInformation> containers = 
				InstallationState.getRunningContainers();
			if (containers.size() == 0)
				return "No local container found.";
			else if (containers.size() > 1)
				return "Too many local containers found.";
			
			ContainerInformation cInfo = containers.get(
				containers.keySet().iterator().next());
			EndpointReferenceType service = EPRUtils.makeEPR(
				cInfo.getContainerURL().toString() 
				+ "/axis/services/GeniiBESPortType");
			
			target = RNSPath.getCurrent().lookup(path, RNSPathQueryFlags.MUST_NOT_EXIST);
			GeniiBESPortType servicePT = ClientUtils.createProxy(
				GeniiBESPortType.class, service);
			newBES = servicePT.vcgrCreate(new VcgrCreate()).getEndpoint();
			target.link(newBES);
			model.addBES(target.pwd(), newBES);
			target = null;
			newBES = null;
			return null;
		} catch (FileLockException e)
		{
			return "Unable to acquire file lock.";
		} catch (RNSPathDoesNotExistException e)
		{
			return "Path does not exist.";
		} catch (RNSPathAlreadyExistsException e)
		{
			return "Path already exists.";
		} catch (ResourceException e)
		{
			return "Unknown resource exception occurred.";
		} catch (GenesisIISecurityException e)
		{
			return "Permission denied";
		} catch (ResourceUnknownFaultType e)
		{
			return "BES Service not found.";
		} catch (ResourceCreationFaultType e)
		{
			return "Unable to create BES container.";
		} catch (RemoteException e)
		{
			return "Unknown grid exception.";
		} catch (RNSException e)
		{
			return "Unknown RNS exception.";
		}
		finally
		{
			if (target != null)
			{
				try { target.unlink(); } catch (Throwable cause) {}
			}
			
			if (newBES != null)
			{
				try
				{
					GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, newBES);
					common.destroy(new Destroy());
				}
				catch (Throwable cause)
				{
				}
			}
		}
	}
	
	static public String removeBES(String path, EndpointReferenceType bes, BESListModel model)
	{
		try
		{
			RNSPath target = RNSPath.getCurrent().lookup(path);
			target.unlink();
		}
		catch (Throwable cause)
		{
			// do nothing
		}
		
		try
		{
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, bes);
			common.destroy(new Destroy());
			model.removeBES(path);
		}
		catch (Throwable cause)
		{
			// do nothing
		}
		
		return null;
	}
}