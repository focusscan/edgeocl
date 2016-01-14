package config;

public class NoFilterFoundException extends ConfigException {
	private static final long serialVersionUID = -1621504518203087467L;

	public NoFilterFoundException() {
	}
	
	@Override
	public String getMessage() {
		return "Configuration file does not contain a filter definition.";
	}
}
