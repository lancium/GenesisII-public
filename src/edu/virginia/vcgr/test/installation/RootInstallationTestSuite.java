package edu.virginia.vcgr.test.installation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.virginia.vcgr.test.installation.client.ClientPackageTestSuite;
import edu.virginia.vcgr.test.installation.container.ContainerPackageTestSuite;

@RunWith(Suite.class)
@SuiteClasses({ContainerPackageTestSuite.class, ClientPackageTestSuite.class})
public class RootInstallationTestSuite
{
}