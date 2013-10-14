package edu.virginia.g3.fsview;

public class AnonymousAuthenticationInformation extends AbstractFSViewAuthenticationInformation
{
	static final long serialVersionUID = 0L;

	public AnonymousAuthenticationInformation()
	{
		super(FSViewAuthenticationInformationTypes.Anonymous);
	}

	@Override
	final public String toString()
	{
		return "Anonymous";
	}
}