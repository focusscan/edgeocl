package config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config {
	private final Path scanManifestPath;
	private final List<ConfigFilter> filters;
	
	public Config(Path in) throws ParserConfigurationException, SAXException, IOException, ConfigException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(in.toFile());
		
		Node doEdgeTransform = doc.getFirstChild();
		while (null != doEdgeTransform && (Node.ELEMENT_NODE != doEdgeTransform.getNodeType() || !doEdgeTransform.getNodeName().equalsIgnoreCase("doEdgeTransform"))) {
			doEdgeTransform = doEdgeTransform.getNextSibling();
		}
		if (null == doEdgeTransform)
			throw new NoFilterFoundException();
		
		// Process doEdgeTransform attributes
		{
			NamedNodeMap atts = doEdgeTransform.getAttributes();
			Node scan =  atts.getNamedItem("scan");
			if (null == scan)
				throw new NoScanPathDefinedException();
			scanManifestPath = in.getParent().resolve(scan.getNodeValue());
		}
		
		filters = new ArrayList<>();
		for (Node output = doEdgeTransform.getFirstChild(); null != output; output = output.getNextSibling()) {
			if (Node.ELEMENT_NODE == output.getNodeType() && output.getNodeName().equalsIgnoreCase("edgeOutput")) {
				NamedNodeMap atts = output.getAttributes();
				Node wavelet = atts.getNamedItem("wavelet");
				if (null == wavelet)
					throw new NoWaveletSpecifiedException();
				Node steps = atts.getNamedItem("steps");
				if (null == steps)
					throw new NoStepsSpecifiedException();
				Node name = atts.getNamedItem("name");
				if (null == name)
					throw new NoNameSpecifiedException();
				ConfigFilter filter = new ConfigFilter(wavelet.getNodeValue(), Integer.parseInt(steps.getNodeValue()), in.getParent().resolve(name.getNodeValue()));
				filters.add(filter);
			}
		}
	}
	
	public Path getScanManifestPath() {
		return scanManifestPath;
	}
	
	public List<ConfigFilter> getFilters() {
		return filters;
	}
}
