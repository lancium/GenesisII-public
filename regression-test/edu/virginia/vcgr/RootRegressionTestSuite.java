package edu.virginia.vcgr;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.virginia.vcgr.client.macro.MacroPackageTestSuite;
import edu.virginia.vcgr.client.units.UnitsPackageTestSuite;

@RunWith(Suite.class)
@SuiteClasses({MacroPackageTestSuite.class, UnitsPackageTestSuite.class})
public class RootRegressionTestSuite
{
}