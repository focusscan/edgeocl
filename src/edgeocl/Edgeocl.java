package edgeocl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bitwise.apps.focusscan.scan.ScanFile;
import config.Config;
import config.ConfigException;
import config.ConfigFilter;

public class Edgeocl {
	private static final String LIST_WAVELETS = "--list-wavelets";
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Edgeocl config-file-name");
			System.out.format("Or: Edgeocl %s\n", LIST_WAVELETS);
			return;
		}
		
		if (args[0].equals(LIST_WAVELETS)) {
			System.out.println("Wavelets:");
			for (String wavelet : ConfigFilter.getWavelets()) {
				System.out.format("  %s\n", wavelet);
			}
			return;
		}
		
		Path configPath = Paths.get(args[0]);
		if (!Files.exists(configPath)) {
			System.out.format("Error: config file `%s` does not exist.\n", configPath);
			return;
		}
		
		if (!Files.isReadable(configPath)) {
			System.out.format("Error: config file `%s` is not readable.\n", configPath);
		}
		
		try {
			System.out.format("Using config file `%s`.\n\n", configPath);
			Config config = new Config(configPath);
			
			System.out.format("Scan path: %s\n", config.getScanManifestPath());
			System.out.println("Output:");
			for (ConfigFilter filter : config.getFilters()) {
				System.out.format("  %s: %s (%d steps)\n", filter.getPath(), filter.getWavelet().getName(), filter.getSteps());
			}
			
			ScanFile scanFile = new ScanFile(config.getScanManifestPath());
			System.out.format("Images in scan: %d\n", scanFile.getData().size());
		} catch (ParserConfigurationException | SAXException | IOException | ConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
