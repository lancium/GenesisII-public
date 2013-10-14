package edu.virginia.vcgr.genii.client.iterator;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;

public interface WSIteratorRP
{
	static final public String NS = IteratorConstants.ITERATOR_NS;

	static final public String ELEMENT_COUNT_NAME = "elementCount";
	static final public QName ELEMENT_COUNT_QNAME = new QName(NS, ELEMENT_COUNT_NAME);

	static final public String PREFERRED_BATCH_SIZE_NAME = "preferredBatchSize";
	static final public QName PREFERRED_BATCH_SIZE_QNAME = new QName(NS, PREFERRED_BATCH_SIZE_NAME);

	@ResourceProperty(namespace = NS, localname = ELEMENT_COUNT_NAME)
	public long getElementCount();

	@ResourceProperty(namespace = NS, localname = PREFERRED_BATCH_SIZE_NAME)
	public long getPreferredBlockSize();
}