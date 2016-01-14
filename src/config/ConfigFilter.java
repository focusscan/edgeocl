package config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

import jwave.transforms.wavelets.Wavelet;
import jwave.transforms.wavelets.daubechies.Daubechies20;
import jwave.transforms.wavelets.daubechies.Daubechies4;
import jwave.transforms.wavelets.haar.Haar1;
import jwave.transforms.wavelets.other.CDF97;

public class ConfigFilter {
	private static HashMap<String, Wavelet> waveletMap = null;
	
	private static HashMap<String, Wavelet> getWaveletMap() {
		if (null != waveletMap)
			return waveletMap;
		waveletMap = new HashMap<>();
		waveletMap.put("haar1", new Haar1());
		waveletMap.put("cdf9/7", new CDF97());
		waveletMap.put("d4", new Daubechies4());
		waveletMap.put("d20", new Daubechies20());
		
		return waveletMap;
	}
	
	private static Wavelet _getWavelet(String name) throws UnknownWaveletException {
		Wavelet ret = getWaveletMap().get(name);
		if (null == ret)
			throw new UnknownWaveletException(name);
		return ret;
	}
	
	public static Set<String> getWavelets() {
		return getWaveletMap().keySet();
	}
	
	private final Wavelet wavelet;
	private final int steps;
	private final Path path;
	
	public ConfigFilter(String in_wavelet, int in_steps, Path in_path) throws UnknownWaveletException {
		wavelet = _getWavelet(in_wavelet);
		steps = in_steps;
		path = in_path;
	}
	
	public Wavelet getWavelet() {
		return wavelet;
	}
	
	public int getSteps() {
		return steps;
	}
	
	public Path getPath() {
		return path;
	}
}
