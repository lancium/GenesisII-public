package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public enum SubscriptionPolicyTypes {
	UseRawPolicy(UseRawSubscriptionPolicy.class), BatchEvents(BatchEventsSubscriptionPolicy.class), CollapseEvents(
		CollapseEventsSubscriptionPolicy.class), IgnoreDuplicateEvents(IgnoreDuplicatesSubscriptionPolicy.class), PersistentNotification(
		PersistentNotificationSubscriptionPolicy.class);

	static private JAXBContext _context = null;

	private Class<? extends SubscriptionPolicy> _policyClass;

	private SubscriptionPolicyTypes(Class<? extends SubscriptionPolicy> policyClass)
	{
		_policyClass = policyClass;
	}

	static public JAXBContext context() throws JAXBException
	{
		synchronized (SubscriptionPolicyTypes.class) {
			if (_context == null) {
				Class<?>[] classes = new Class<?>[SubscriptionPolicyTypes.values().length];
				int lcv = 0;
				for (SubscriptionPolicyTypes type : SubscriptionPolicyTypes.values())
					classes[lcv++] = type._policyClass;

				_context = JAXBContext.newInstance(classes);
			}
		}

		return _context;
	}
}