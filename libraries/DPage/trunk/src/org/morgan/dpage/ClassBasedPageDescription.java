package org.morgan.dpage;

import java.io.IOException;

class ClassBasedPageDescription implements PageDescription {
	private Class<? extends DynamicPage> _pageClass;

	@SuppressWarnings("unchecked")
	public ClassBasedPageDescription(ClassLoader loader, String className)
			throws ClassNotFoundException {
		_pageClass = (Class<? extends DynamicPage>) loader.loadClass(className);
	}

	@Override
	public DynamicPage loadPage() throws IOException {
		try {
			return _pageClass.newInstance();
		} catch (InstantiationException e) {
			throw new IOException("Unable to load dynamic page.", e);
		} catch (IllegalAccessException e) {
			throw new IOException("Unable to load dynamic page.", e);
		}
	}
}