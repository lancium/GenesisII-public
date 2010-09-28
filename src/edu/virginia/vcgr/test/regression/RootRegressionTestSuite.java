package edu.virginia.vcgr.test.regression;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.virginia.vcgr.test.regression.client.macro.MacroPackageTestSuite;
import edu.virginia.vcgr.test.regression.client.units.UnitsPackageTestSuite;

@RunWith(Suite.class)
@SuiteClasses({MacroPackageTestSuite.class, UnitsPackageTestSuite.class})
public class RootRegressionTestSuite
{
}