package edu.virginia.vcgr.genii.client.utils.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;

import edu.virginia.vcgr.genii.client.utils.urls.URLUtilities;

public class URLUtilityTest
{
	@Test
	public void testFormURL() throws MalformedURLException
	{
		URL expected = new URL("http://www.tempuri.org/some-path.txt");
		URL testValue = URLUtilities.formURL("http://www.tempuri.org/some-path.txt", false);
		Assert.assertEquals(expected, testValue);

		expected = new URL("ftp://www.tempuri.org/some-path.txt");
		testValue = URLUtilities.formURL("ftp://www.tempuri.org/some-path.txt", false);
		Assert.assertEquals(expected, testValue);

	}

	/*
	 * We can't actually run these tests because they behavior is incorrect for paths that occur on
	 * platforms other than the one that the path is for -- in otherwords, a windows path only works
	 * on a windows machine and vice-versa
	 */

	@Ignore
	@Test
	public void testFormURLFromUnixPath() throws MalformedURLException
	{
		URL expected;
		URL testValue;

		expected = new URL("file:///home/morgan/some-path.txt");
		testValue = URLUtilities.formURL("/home/morgan/some-path.txt", false);
		Assert.assertEquals(expected, testValue);
	}

	@Ignore
	@Test
	public void testFormURLFromWindowsPath() throws MalformedURLException
	{
		URL expected;
		URL testValue;

		expected = new URL("file://C:/home/morgan/some-path.txt");
		testValue = URLUtilities.formURL("C:\\home\\morgan\\some-path.txt", true);
		Assert.assertEquals(expected, testValue);
	}
}