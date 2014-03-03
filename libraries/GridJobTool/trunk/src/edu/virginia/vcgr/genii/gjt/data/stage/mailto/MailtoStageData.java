package edu.virginia.vcgr.genii.gjt.data.stage.mailto;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class MailtoStageData extends AbstractStageData {
	@XmlAttribute(name = "email-address")
	private String _emailAddress = null;

	@XmlAttribute(name = "subject")
	private String _subject = null;

	@XmlAttribute(name = "attachment-name")
	private String _attachmentName = null;

	@Override
	protected void activateImpl() {
		fireParameterizableStringModified("", _emailAddress);
		fireParameterizableStringModified("", _subject);
		fireParameterizableStringModified("", _attachmentName);
		fireJobDescriptionModified();
	}

	@Override
	protected void deactivateImpl() {
		fireParameterizableStringModified(_emailAddress, "");
		fireParameterizableStringModified(_subject, "");
		fireParameterizableStringModified(_attachmentName, "");
		fireJobDescriptionModified();
	}

	MailtoStageData() {
		super(StageProtocol.mailto);
	}

	final String emailAddress() {
		return _emailAddress;
	}

	final void emailAddress(String emailAddress) {
		String old = _emailAddress;
		_emailAddress = emailAddress;

		fireParameterizableStringModified(old, _emailAddress);
		fireJobDescriptionModified();
	}

	final String subject() {
		return _subject;
	}

	final void subject(String subject) {
		String old = _subject;
		_subject = subject;

		fireParameterizableStringModified(old, _subject);
		fireJobDescriptionModified();
	}

	final String attachmentName() {
		return _attachmentName;
	}

	final void attachmentName(String attachmentName) {
		String old = _attachmentName;
		_attachmentName = attachmentName;

		fireParameterizableStringModified(old, _attachmentName);
		fireJobDescriptionModified();
	}

	@Override
	public String toString() {
		boolean sawQuestionMark = false;

		StringBuilder builder = new StringBuilder("mailto:");
		if (_emailAddress != null && _emailAddress.length() > 0)
			builder.append(_emailAddress);
		else
			builder.append("<unknown>");

		if (_subject != null && _subject.length() > 0) {
			builder.append("?subject=");
			builder.append(_subject);
			sawQuestionMark = true;
		}

		if (_attachmentName != null && _attachmentName.length() > 0) {
			builder.append(sawQuestionMark ? "&amp;" : "?");
			builder.append("X-AttachmentFilename=" + _attachmentName);
		}

		return builder.toString();
	}

	@Override
	public void analyze(String filename, Analysis analysis) {
		if (_emailAddress == null || _emailAddress.length() == 0) {
			analysis.addError(
					"Data stage for file \"%s\" needs an email address!",
					filename);
		}

		if (_subject == null || _subject.length() == 0)
			analysis.addWarning(
					"Data stage for file \"%s\" should have a subject.",
					filename);

		if (_attachmentName == null || _attachmentName.length() == 0)
			analysis.addWarning(
					"It is recommended that you include an attachment name for data stage \"%s\".",
					filename);
		else if (_attachmentName.contains("/"))
			analysis.addError(
					"Attachment filename for data stage file \"%s\" cannot "
							+ "contain the / character.", filename);
		else if (_attachmentName.contains("\\"))
			analysis.addError(
					"Attachment filename for data stage file \"%s\" cannot "
							+ "contain the \\ character.", filename);
	}

	@Override
	public String getJSDLURI() {
		return toString();
	}
}