package bitwise.apps.focusscan.scan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class EdgeFile {
	private static final String EDGE_SCAN = "edgeScan";
	private static final String PROPERTIES = "properties";
	private static final String SCAN_PATH = "scanPath";
	private static final String WAVELET = "wavelet";
	private static final String STEPS = "steps";
	private static final String DATA = "data";
	private static final String EDGE_CAPTURE = "edgeCapture";
	private static final String IMAGE_NUMBER = "imageNumber";
	private static final String PATH = "path";
	
	private String scanPath = "";
	private String wavelet = "";
	private int steps = 0;
	
	private List<EdgeFileDatum> data = new ArrayList<>();
	
	public EdgeFile() {
		
	}
	
	public EdgeFile(Path in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(in.toFile());
		
		Node edgeScan = doc.getFirstChild();
		while (null != edgeScan && (Node.ELEMENT_NODE != edgeScan.getNodeType() || !edgeScan.getNodeName().equalsIgnoreCase(EDGE_SCAN))) {
			edgeScan = edgeScan.getNextSibling();
		}
		assert (null != edgeScan);
		
		// Scan properties
		{
			Node properties = edgeScan.getFirstChild();
			while (null != properties && (Node.ELEMENT_NODE != properties.getNodeType() || !properties.getNodeName().equalsIgnoreCase(PROPERTIES))) {
				properties = properties.getNextSibling();
			}
			assert (null != properties);
			
			for (Node prop = properties.getFirstChild(); prop != null; prop = prop.getNextSibling()) {
				if (Node.ELEMENT_NODE == prop.getNodeType()) {
					if (prop.getNodeName().equalsIgnoreCase(SCAN_PATH)) {
						scanPath = prop.getNodeValue();
					}
					else if (prop.getNodeName().equalsIgnoreCase(WAVELET)) {
						wavelet = prop.getNodeValue();
					}
					else if (prop.getNodeName().equalsIgnoreCase(STEPS)) {
						steps = Integer.parseInt(prop.getTextContent());
					}
				}
			}
		}
		// End scan properties
		
		Node dataNode = edgeScan.getFirstChild();
		while (null != dataNode && (Node.ELEMENT_NODE != dataNode.getNodeType() || !dataNode.getNodeName().equalsIgnoreCase(DATA))) {
			dataNode = dataNode.getNextSibling();
		}
		assert (null != dataNode);
		for (Node capture = dataNode.getFirstChild(); capture != null; capture = capture.getNextSibling()) {
			if (Node.ELEMENT_NODE == capture.getNodeType() && capture.getNodeName().equalsIgnoreCase(EDGE_CAPTURE)) {
				int imageNumber = -1;
				String path = null;
				
				imageNumber = Integer.parseInt(capture.getAttributes().getNamedItem(IMAGE_NUMBER).getNodeValue());
				assert (imageNumber >= 0);
				
				for (Node prop = capture.getFirstChild(); prop != null; prop = prop.getNextSibling()) {
					if (Node.ELEMENT_NODE == prop.getNodeType()) {
						if (prop.getNodeName().equalsIgnoreCase(PATH)) {
							path = prop.getTextContent();
						}
					}
				}
				
				EdgeFileDatum datum = new EdgeFileDatum();
				datum.setImageNumber(imageNumber);
				datum.setPath(path);
				data.add(datum);
			}
		}
	}
	
	public boolean saveToFile(Path out) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(getXml(), new StreamResult(out.toFile()));
		} catch (TransformerException e) {
			return false;
		}
		
		return true;
	}
	
	public DOMSource getXml() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Document doc = docBuilder.newDocument();
		
		Element edgeScanE = doc.createElement(EDGE_SCAN);
		doc.appendChild(edgeScanE);
		
		{
			Element edgeScanPropertiesE = doc.createElement(PROPERTIES);
			edgeScanE.appendChild(edgeScanPropertiesE);
			
			{
				Element scanPathE = doc.createElement(SCAN_PATH);
				scanPathE.appendChild(doc.createTextNode(scanPath));
				edgeScanPropertiesE.appendChild(scanPathE);
			}
			
			{
				Element waveletE = doc.createElement(WAVELET);
				waveletE.appendChild(doc.createTextNode(wavelet));
				edgeScanPropertiesE.appendChild(waveletE);
			}
			
			{
				Element stepsE = doc.createElement(STEPS);
				stepsE.appendChild(doc.createTextNode(String.format("%d", steps)));
				edgeScanPropertiesE.appendChild(stepsE);
			}
		}
		
		{
			Element dataE = doc.createElement(DATA);
			edgeScanE.appendChild(dataE);
			
			for (EdgeFileDatum datum : data) {
				Element captureE = doc.createElement(EDGE_CAPTURE);
				captureE.setAttribute(IMAGE_NUMBER, String.format("%d", datum.getImageNumber()));
				dataE.appendChild(captureE);
				
				{
					Element liveViewE = doc.createElement(PATH);
					liveViewE.appendChild(doc.createTextNode(datum.getPath()));
					captureE.appendChild(liveViewE);
				}
			}
		}
		
		return new DOMSource(doc);
	}
	
	public String getScanPath() {
		return scanPath;
	}
	
	public void setScanPath(String in) {
		scanPath = in;
	}
	
	public String getWavelet() {
		return wavelet;
	}
	
	public void setWavelet(String in) {
		wavelet = in;
	}
	
	public int getSteps() {
		return steps;
	}
	
	public void setSteps(int in) {
		steps = in;
	}
	
	public List<EdgeFileDatum> getData() {
		return data;
	}
	
	public EdgeFileDatum getDataByImageNumber(int imageNumber) {
		for (EdgeFileDatum datum : data) {
			if (datum.getImageNumber() == imageNumber)
				return datum;
		}
		return null;
	}
}
