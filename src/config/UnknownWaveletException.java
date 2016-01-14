package config;

public class UnknownWaveletException extends ConfigException {
	private static final long serialVersionUID = 6686404677113094118L;
	public final String name;
	
	public UnknownWaveletException(String in_name) {
		name = in_name;
	}
	
	@Override
	public String getMessage() {
		return String.format("Unknown wavelet: %s", name);
	}
}
