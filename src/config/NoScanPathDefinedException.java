package config;

public class NoScanPathDefinedException extends ConfigException {
	private static final long serialVersionUID = 6123927910547218138L;

	public NoScanPathDefinedException() {
	}
	
	@Override
	public String getMessage() {
		return "Filter specification does not specify a scan manifest.";
	}
}
