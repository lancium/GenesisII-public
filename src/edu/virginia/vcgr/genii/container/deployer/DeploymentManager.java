package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class DeploymentManager
{
	static private Log _logger = LogFactory.getLog(DeploymentManager.class);

	// 1 week
	static final private long _UNUSED_TIMEOUT_DAYS = 7;
	
	static private File _baseDirectory;
	
	static
	{
		_baseDirectory = 
			ConfigurationManager.getCurrentConfiguration().getUserDirectory();
		if (!_baseDirectory.exists())
			_baseDirectory.mkdirs();
		if (!_baseDirectory.exists())
			throw new RuntimeException(
				"Unable to create deploy directory " + _baseDirectory);
		if (!_baseDirectory.isDirectory())
			throw new RuntimeException("Deploy path " + _baseDirectory 
				+ " does not seem to be a directory.");
		
		DeployDatabase database = null;
		try
		{
			database = new DeployDatabase();
			cleanupStaleDeployments(database);
			_knownDeployments = database.getKnownDeployments();
			database.commit();
		}
		catch (Throwable t)
		{
			_logger.error("Unable to create deploy database.", t);
		}
		finally
		{
			StreamUtils.close(database);
		}
	}
	
	static private HashMap<DeploySnapshot, String> _knownDeployments;
	
	static public IDeployment createDeployment(
		String deploymentID, IDeployerProvider provider,
		DeploySnapshot snapshot) throws DeploymentException
	{
		String instanceID;
		
		while (true)
		{
			synchronized(_knownDeployments)
			{
				instanceID = _knownDeployments.get(snapshot);
				if (instanceID == null)
				{
					// My responsibility to download
					instanceID = "";
					_knownDeployments.put(snapshot, instanceID);
					break;
				} else
				{
					if (instanceID.equals(""))
					{
						// Someone else is working this one.
						try { _knownDeployments.wait(); } catch (Throwable t) {}
					} else
					{
						// It's all good and ready to go
						break;
					}
				}
			}
		}
		
		File targetDirectory;
		
		DeployDatabase database = null;
		try
		{
			database = new DeployDatabase();
			
			try
			{
				if (instanceID.equals(""))
				{
					targetDirectory = createNewDirectory();
					instanceID = database.createDeployment(
						deploymentID, targetDirectory.getName(), snapshot);
					database.commit();
					
					provider.deployApplication(targetDirectory);
					database.setState(instanceID, DeploymentState.DEPLOYED);
					database.commit();
					
					synchronized(_knownDeployments)
					{
						_knownDeployments.put(snapshot, instanceID);
						_knownDeployments.notifyAll();
					}
				} else
				{
					targetDirectory = new File(_baseDirectory,
						database.getDirectory(instanceID));
				}
			}
			catch (SQLException sqe)
			{
				throw new DeploymentException(
					"Unable to deploy application.", sqe);
			}
			
			// Now that we have a deployment instance ID, we need to create the
			// deployment structures
			try
			{
				// There is a small chance here that we could leak this
				// deployment.  Essentially, if the VM crashes (or the
				// DeploymentBundle we are about to create is lost) after
				// the following commit, but before the DeploymentBundle itself
				// is stored with the resource, then we'll never decrement the
				// count on this deployment (hence, it will never go to zero and
				// get cleaned up).  For now, we're going to live with 
				database.updateCount(instanceID, 1);
				database.commit();
				return new DeploymentBundle(instanceID, targetDirectory, 
					provider.getReifier());
			}
			catch (SQLException sqe)
			{
				throw new DeploymentException(
					"Unable to deploy application", sqe);
			}
		}
		catch (SQLException sqe)
		{
			throw new DeploymentException(
				"Problem with the deployment database.", sqe);
		}
		finally
		{
			StreamUtils.close(database);
		}
	}
	
	static private void cleanupStaleDeployments(
		DeployDatabase database) throws SQLException
	{
		Collection<DeploymentInformation> staleInfo =
			database.retrieveStaleDeployments(_UNUSED_TIMEOUT_DAYS);
		
		for (DeploymentInformation info : staleInfo)
		{
			database.setState(info.getInstanceID(), 
				DeploymentState.PARTIAL);
		}
		database.commit();
		
		for (DeploymentInformation info : staleInfo)
		{
			removeDirectory(
				new File(_baseDirectory, info.getDirectoryName()));
			database.deleteDeployment(info.getInstanceID());
		}
		database.commit();
	}
	
	static private void removeDirectory(File dir)
	{
		for (File child : dir.listFiles())
		{
			if (child.isDirectory())
				removeDirectory(child);
			else
				child.delete();
		}
		
		dir.delete();
	}
	
	static private Random _generator = new Random();
	static private File createNewDirectory()
	{
		while (true)
		{
			int next = _generator.nextInt();
			String name = String.format("dep-%d", next);
			File dir = new File(_baseDirectory, name);
			
			synchronized(DeploymentManager.class)
			{
				if (dir.exists())
					continue;
				if (!dir.mkdir())
					continue;
			}
			
			return dir;
		}
	}
	
	static void decrementCount(String instanceID) throws DeploymentException
	{
		DeployDatabase database = null;
		
		try
		{
			database = new DeployDatabase();
			database.updateCount(instanceID, -1);
			database.commit();
		}
		catch (SQLException sqe)
		{
			throw new DeploymentException(
				"Unable to undeploy application.", sqe);
		}
		finally
		{
			StreamUtils.close(database);
		}
	}
}