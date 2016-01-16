package edgeocl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

import bitwise.apps.focusscan.scan.EdgeFile;
import bitwise.apps.focusscan.scan.EdgeFileDatum;
import bitwise.apps.focusscan.scan.ScanFile;
import bitwise.apps.focusscan.scan.ScanFileDatum;
import config.Config;
import config.ConfigException;
import config.ConfigFilter;
import edgeocl.pipeline.LoadImageTask;
import edgeocl.pipeline.Pipeline;

public class Edgeocl {
	private static final String LIST_WAVELETS = "--list-wavelets";
	private static final String LIST_OPENCL = "--list-opencl";
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Edgeocl config-file-name");
			System.out.format("Or: Edgeocl %s\n", LIST_WAVELETS);
			System.out.format("Or: Edgeocl %s\n", LIST_OPENCL);
			return;
		}
		
		if (args[0].equals(LIST_WAVELETS)) {
			System.out.println("Wavelets:");
			for (String wavelet : ConfigFilter.getWavelets()) {
				System.out.format("  %s\n", wavelet);
			}
			return;
		}
		
		if (args[0].equals(LIST_OPENCL)) {
			System.out.println("OpenCL platforms and devices:");
			CLPlatform[] platforms = CLPlatform.listCLPlatforms();
			for (int p = 0; p < platforms.length; p++) {
				CLPlatform platform = platforms[p];
				System.out.format("  %d %s:\n", p, platform.getName());
				CLDevice[] devices = platform.listCLDevices();
				for (int d = 0; d < devices.length; d++) {
					CLDevice device = devices[d];
					System.out.format("    %d %s\n", d, device.getName());
				}
			}
			CLPlatform dplatform = CLPlatform.getDefault(/* type(CPU) */);
			CLDevice ddevice = dplatform.getMaxFlopsDevice();
			System.out.format("Default platform/device: %s [%s]\n", dplatform.getName(), ddevice.getName());
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
		
		CLPlatform platform = CLPlatform.getDefault(/* type(CPU) */);
		CLContext context = CLContext.create(platform.getMaxFlopsDevice());
		
		try {
			Config config = new Config(configPath);
			
			System.out.format("Scan path: %s\n", config.getScanManifestPath());
			
			ScanFile scanFile = new ScanFile(config.getScanManifestPath());
			System.out.format("Images in scan: %d\n", scanFile.getData().size());
			
			System.out.println("Output:");
			for (ConfigFilter filter : config.getFilters()) {
				System.out.format("  %s: %s (%d steps)\n", filter.getPath(), filter.getWavelet().getName(), filter.getSteps());
			}
			
			doEdgeTransform(config, scanFile, platform, context);
		} catch (ParserConfigurationException | SAXException | IOException | ConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		context.release();
	}
	
	private static void doEdgeTransform(Config config, ScanFile scanFile, CLPlatform platform, CLContext context) throws IOException, InterruptedException {
		int outputCount = 0;
		
		// Create the edge manifests
		ArrayList<EdgeFile> edgeManifests = new ArrayList<>();
		for (ConfigFilter filter : config.getFilters()) {
			// Create the output directory for the filter
			if (Files.exists(filter.getPath())) {
				System.out.format("Path already exists: `%s`\n", filter.getPath());
				System.out.println("Aborting with no data computed or written.");
				return;
			}
			
			// Create the manifest file
			EdgeFile edgeFile = new EdgeFile();
			edgeFile.setScanPath(filter.getPath().relativize(config.getScanManifestPath()).toString());
			edgeFile.setWavelet(filter.getWaveletName());
			edgeFile.setSteps(filter.getSteps());
			for (ScanFileDatum scanFileDatum : scanFile.getData()) {
				String stillName = Paths.get(scanFileDatum.getStillImage()).getFileName().toString();
				stillName = stillName.substring(0, stillName.lastIndexOf('.'));
				
				EdgeFileDatum edgeFileDatum = new EdgeFileDatum();
				edgeFileDatum.setImageNumber(scanFileDatum.getImageNumber());
				edgeFileDatum.setPath(String.format("%s.bmp", stillName));
				edgeFile.getData().add(edgeFileDatum);
				
				outputCount++;
			}
			edgeManifests.add(edgeFile);
		}
		
		// Create output directories and save edge manifests
		for (int i = 0; i < config.getFilters().size(); i++) {
			ConfigFilter filter = config.getFilters().get(i);
			EdgeFile edgeFile = edgeManifests.get(i);
			Files.createDirectory(filter.getPath());
			edgeFile.saveToFile(filter.getPath().resolve("edgeManifest.xml"));
		}
		
		// Create the processing pipeline
		CLCommandQueue queue = context.getDevices()[0].createCommandQueue();
		Pipeline pipeline = new Pipeline(config, edgeManifests, platform, context, queue, outputCount);
		
		// Start the OpenCL thread
		Thread oclThread = new Thread(new EdgeTransform(pipeline));
		oclThread.setDaemon(true);
		oclThread.start();
		
		// Populate the input to the pipeline
		for (ScanFileDatum scanFileDatum : scanFile.getData()) {
			Path path = config.getScanManifestPath().getParent().resolve(scanFileDatum.getStillImage());
			LoadImageTask lit = new LoadImageTask(pipeline, scanFileDatum.getImageNumber(), path);
			pipeline.loadPool.submit(lit);
		}
		
		// Wait for the pipeline to clear
		pipeline.latch.await();
		
		pipeline.loadPool.shutdown();
		oclThread.interrupt();
		oclThread.join();
		pipeline.savePool.shutdown();
	}
}
