package edu.virginia.vcgr.genii.client.incommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CILogonClient
{

	static private Log _logger = LogFactory.getLog(CILogonClient.class);

	// Fields for holding some of the intermediate values that need to be compared and/or
	// passed around during the protocol
	private Node _relayState = null;
	private String _responseURL, _serviceURL;

	// The parameters provided by the calling client, such as credentials and target IDP
	private CILogonParameters _params;

	// HttpClient objects used during the communication
	private CloseableHttpClient _client = null;
	private HttpClientContext _defaultContext = null;
	private CookieStore _cookieJar = new BasicCookieStore();

	// TODO probably needs to be moved out to a config file somewhere
	// URL of the CILogon service
	private final String CILogonURL = "https://ecp.cilogon.org/secure/getcert/";

	// Basic constructor. Just copies in the parameters. Might skip this and make it
	// a static call, since there's only the one entry point anyway.
	public CILogonClient(CILogonParameters params)
	{
		_params = params;
	}

	// A couple of debugging utility functions.
	private void debug(String o)
	{
		_logger.debug(o);
		if (!_params.silent) {
			_params.stdout.println(o);
			_params.stdout.println("-------------------------------------------");
		}
	}

	private void trace(String o)
	{
		_logger.trace(o);
		if (!_params.silent && _params.verbose) {
			_params.stdout.println(o);
			_params.stdout.println("-------------------------------------------");
		}
	}

	// This is the only entry point into the protocol. None of the other functions
	// are meant for external consumption.
	// Final return value is a string containing the certificate, assuming success
	public String call() throws IOException, URISyntaxException
	{
		trace("");
		String res1 = call1();
		trace("-----First Call Response \n" + res1);
		String query2 = processCall1(res1);
		trace("-----Second Call Request \n" + query2);
		String res2 = call2(query2);
		trace("-----Second Call Response \n" + res2);
		String query3 = processCall2(res2);
		trace("-----Third Call Request \n" + query3);
		call3(query3);
		String ret = call4();

		return ret;
	}

	// //////// HTTP Calls /////////////////////////////

	private String call1() throws IOException, URISyntaxException
	{
		HttpGet get = defaultGet(CILogonURL);
		return makeCall(get);
	}

	private String call2(String query2) throws IOException, URISyntaxException
	{
		HttpPost post = defaultPost(_params.IDPUrl, query2);
		return makeSecureCall(post, _params.username, _params.password);
	}

	private String call3(String query3) throws IOException, URISyntaxException
	{
		HttpPost post = defaultPost(_serviceURL, query3);
		post.addHeader("Content-Type", "application/vnd.paos+xml");
		return makeCall(post);
	}

	private String call4() throws IOException, URISyntaxException
	{
		HttpPost post = defaultPost(CILogonURL, null);

		String randStr = UUID.randomUUID().toString();

		ArrayList<NameValuePair> formParams = new ArrayList<NameValuePair>();

		formParams.add(new BasicNameValuePair("submit", "certreq"));
		formParams.add(new BasicNameValuePair("CSRF", randStr));
		formParams.add(new BasicNameValuePair("certlifetime", "" + _params.lifetime));
		formParams.add(new BasicNameValuePair("certreq", _params.csr));

		post.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));

		BasicClientCookie cookie = new BasicClientCookie("CSRF", randStr);
		for (Cookie c : _cookieJar.getCookies()) {
			// I could hard code the cookie domain, but this is slightly
			// less likely to break if the service addresses change
			if (c.getDomain().contains("cilogon")) {
				cookie.setDomain(c.getDomain());
				cookie.setPath(c.getPath());
				break;
			}
		}
		_cookieJar.addCookie(cookie);

		return makeCall(post);
	}

	// //// Post call processing ////////////////

	private String processCall1(String res1) throws IOException
	{
		try {
			DocumentBuilderFactory bFactory = DocumentBuilderFactory.newInstance();
			bFactory.setNamespaceAware(true);
			DocumentBuilder builder = bFactory.newDocumentBuilder();

			// Use String reader
			Document doc = builder.parse(new InputSource(new StringReader(res1)));
			Node header = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Header").item(0);

			Node relayStateNode =
				doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp", "RelayState").item(0);
			if (relayStateNode != null) {
				trace("Capturing relayState data");
				_relayState = relayStateNode;
			} else {
				trace("No relayState node in document");
			}

			if (res1.contains("responseConsumerURL")) {
				int valueLocation = res1.indexOf("responseConsumerURL=\"") + "responseConsumerURL=\"".length();
				_responseURL = res1.substring(valueLocation, res1.indexOf("\"", valueLocation));
				trace("ResponseConsumerURL = " + _responseURL);
			}

			if (header != null) {
				trace("Found the header element(s)");
				doc.getFirstChild().removeChild(header);
			}
			NodeList queryResult = doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest");
			if (queryResult != null && queryResult.getLength() > 0) {
				trace("Found the " + queryResult.getLength() + " Authn element(s)");

				StringWriter writer = new StringWriter();
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));

				// returns the document minus the headers
				return writer.toString();
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		return null;
	}

	private String processCall2(String res2) throws IOException
	{
		try {
			DocumentBuilderFactory bFactory = DocumentBuilderFactory.newInstance();
			bFactory.setNamespaceAware(true);
			DocumentBuilder builder = bFactory.newDocumentBuilder();

			// Use String reader
			Document doc = builder.parse(new InputSource(new StringReader(res2)));
			Node header = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Header").item(0);

			if (header != null) {
				trace("Found the header element(s)");
			}
			if (res2.contains("AssertionConsumerServiceURL")) {
				int valueLocation = res2.indexOf("AssertionConsumerServiceURL=\"") + "AssertionConsumerServiceURL=\"".length();
				_serviceURL = res2.substring(valueLocation, res2.indexOf("\"", valueLocation));
				trace("AssertionConsumerServiceURL = " + _serviceURL);
				if (!_responseURL.equals(_serviceURL)) {
					debug("Error, service URL and response URL do not match. Bailing out!");
					throw new IOException("service URL and response URL do not match");
				}
			}

			NodeList queryResult = doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp", "Response");
			if (queryResult != null && queryResult.getLength() > 0) {
				trace("Found the " + queryResult.getLength() + " Response element(s)");

				if (_relayState != null) {
					Node localRelayState = doc.importNode(_relayState, true);
					header.replaceChild(localRelayState, queryResult.item(0));
					trace("Swapping out RelayState for Response in the header");
				}

				StringWriter writer = new StringWriter();
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));

				return writer.toString();
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		return null;
	}

	// ///// HttpClient abstraction methods ////////////

	private HttpGet defaultGet(String site)
	{
		HttpGet get = new HttpGet(site);
		get.addHeader("Accept", "text/html; application/vnd.paos+xml");
		get.addHeader("PAOS", "ver=\"urn:liberty:paos:2003-08\";\"urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp");
		return get;
	}

	private HttpPost defaultPost(String site, String content) throws UnsupportedEncodingException
	{
		HttpPost post = new HttpPost(site);
		if (content != null) {
			HttpEntity entity = new StringEntity(content);
			post.setEntity(entity);
		}
		post.addHeader("Accept", "text/html; application/vnd.paos+xml");
		post.addHeader("PAOS", "ver=\"urn:liberty:paos:2003-08\";\"urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp");
		return post;
	}

	private String makeCall(HttpRequestBase request) throws ClientProtocolException, IOException
	{
		if (_defaultContext == null) {
			_defaultContext = HttpClientContext.create();
			_defaultContext.setCookieStore(_cookieJar);
		}
		debug("Calling " + request.getMethod() + " on " + request.getURI().toString());
		return makeCall(request, _defaultContext);
	}

	private String makeSecureCall(HttpPost request, String username, String password) throws ClientProtocolException,
		IOException
	{
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
		BasicCredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(provider);

		String stars = "";
		for (int i = 1; i < password.length(); ++i) {
			stars += "*";
		}
		debug("Calling " + request.getMethod() + " on " + request.getURI().toString() + " with username=" + username
			+ " and password=" + password.charAt(0) + stars);
		return makeCall(request, context);
	}

	private String makeCall(HttpRequestBase request, HttpClientContext context) throws ClientProtocolException, IOException
	{
		if (_client == null) {
			RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).build();
			_client = HttpClients.custom().setDefaultCookieStore(_cookieJar).setDefaultRequestConfig(config).build();
		}

		CloseableHttpResponse result = _client.execute(request, context);

		if (result.getStatusLine().getStatusCode() >= 400) {
			debug("Request failed: " + result.getStatusLine().getStatusCode() + ": " + result.getStatusLine().getReasonPhrase());
			return null;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(result.getEntity().getContent()));

		String content = "";
		String str = br.readLine();
		do {
			content += str + "\n";
		} while ((str = br.readLine()) != null);
		br.close();

		return content;
	}
}
