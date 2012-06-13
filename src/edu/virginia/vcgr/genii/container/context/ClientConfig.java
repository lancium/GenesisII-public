package edu.virginia.vcgr.genii.container.context;

/*
 * This identifies the client accessing the container. The client is supposed to generate a GUID that will 
 * uniquely identify it in the container. Since GUID's are random strings and we use SSL for transport
 * layer security, chance sniffing and using another client's GUID is very low. The client can create new
 * GUIDs to fool the container. However, that will only affect the faulty/mischievous client, not others.
 * */
public class ClientConfig {
	
	private static ThreadLocal<ClientConfig> _clientConfig = 
		new ThreadLocal<ClientConfig>() {
			@Override
			protected ClientConfig initialValue() {
				return null;
			}
	};
	
	private String clientId;
	
	public static void setClientConfig(String clientId) {
		if (clientId == null) return;
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.clientId = clientId;
		_clientConfig.set(clientConfig);
	}
	
	public static ClientConfig getCurrentClientConfig() {
		return _clientConfig.get();
	}

	public String getClientId() {
		return clientId;
	}
}
