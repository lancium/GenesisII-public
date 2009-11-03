package edu.virginia.vcgr.genii.client.jsdl.sweep.test.person;

import java.io.InputStream;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.Sweep;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepListener;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationContext;

public class SweepTestDriver
{
	static private Node createDocument() throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().newDocument();
	}
	
	static public void runTest(JAXBContext context, String resource)
		throws Throwable
	{
		Marshaller m = context.createMarshaller();
		Unmarshaller u = context.createUnmarshaller();
		PersonDocument doc;
		Sweep sweep;
		InputStream in = SweepTestDriver.class.getResourceAsStream(resource);
		doc = (PersonDocument)u.unmarshal(in);
		in.close();
		sweep = doc.removeSweep();
		Node xmlDoc = createDocument();
		Binder<Node> binder = context.createBinder();
		binder.marshal(doc, xmlDoc);
		EvaluationContext eContext = new EvaluationContext(
			new SweepListenerImpl(m, binder), xmlDoc);
		sweep.evaluate(eContext);
	}
	
	static public void main(String []args) throws Throwable
	{
		JAXBContext context = JAXBContext.newInstance(PersonDocument.class);
		
		runTest(context, "example-sweep-1.xml");
	}
	
	static private class SweepListenerImpl implements SweepListener
	{
		private Marshaller _marshaller;
		private Binder<Node> _binder;
		
		private SweepListenerImpl(Marshaller marshaller, Binder<Node> binder)
		{
			_marshaller = marshaller;
			_binder = binder;
		}
		
		@Override
		public void emitSweepInstance(Node document) throws SweepException
		{
			try
			{
				_marshaller.marshal(
					_binder.updateJAXB(document), System.out);
				System.out.println();
			} 
			catch (JAXBException e)
			{
				throw new SweepException(
					"Unable to emit new document.", e);
			}
		}	
	}
}