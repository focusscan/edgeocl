package config;

public class NoWaveletSpecifiedException extends ConfigException {
	private static final long serialVersionUID = 8532762402918266947L;

	public NoWaveletSpecifiedException() {
	}
	
	@Override
	public String getMessage() {
		return "Output does not specify a wavelet.";
	}
}
