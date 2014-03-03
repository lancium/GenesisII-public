package edu.virginia.vcgr.genii.gjt.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.gjt.util.ClassRelativeIOSource;
import edu.virginia.vcgr.genii.gjt.util.FileIOSource;
import edu.virginia.vcgr.genii.gjt.util.IOSource;
import edu.virginia.vcgr.genii.gjt.util.IOUtils;
import edu.virginia.vcgr.genii.gjt.util.OverridenIOSource;

@XmlRootElement(name = "SPMDVariations")
class SPMDVariations
{
	static final private String FILENAME = "spmd-variations.xml";

	@XmlElement(name = "variation")
	private List<SPMDVariation> _variations = new Vector<SPMDVariation>();

	Map<String, SPMDVariation> variations()
	{
		Map<String, SPMDVariation> ret = new HashMap<String, SPMDVariation>(_variations.size());

		for (SPMDVariation variation : _variations)
			ret.put(variation.name(), variation);

		return ret;
	}

	static SPMDVariations readVariations(File configuraitonDirectory) throws IOException, JAXBException
	{
		IOSource source =
			new OverridenIOSource(new FileIOSource(new File(configuraitonDirectory, FILENAME)), new ClassRelativeIOSource(
				SPMDVariations.class, FILENAME));
		InputStream in = null;

		try {
			in = source.open();
			JAXBContext context = JAXBContext.newInstance(SPMDVariations.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (SPMDVariations) unmarshaller.unmarshal(in);
		} finally {
			IOUtils.close(in);
		}
	}
}