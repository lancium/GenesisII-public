package edu.virginia.vcgr.genii.container.rfork;

import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import javax.xml.namespace.QName;

public class RandomByteIOAttributeHandlers extends ByteIOAttributeHandlers
{
	static public final String RANDOM_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/random-access";
	
	public RandomByteIOAttributeHandlers(RandomByteIOResourceFork fork,
		AttributePackage pkg) throws NoSuchMethodException
	{
		super(fork, pkg);
	}
	
	protected QName GetSizeNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "Size");
		
	}
	
	protected QName GetReadableNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "Readable");
		
	}
	
	protected QName GetWriteableNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "Writeable");
		
	}
	
	protected QName GetTransferMechanismNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "TransferMechanism");
		
	}
	
	protected QName GetCreateTimeNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "CreateTime");
		
	}
	
	protected QName GetModificationTimeNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "ModificationTime");
		
	}
	
	protected QName GetAccessTimeNamespace()
	{
		return new QName(RANDOM_BYTEIO_NS, "AccessTime");
		
	}
}