package config;

public class NoStepsSpecifiedException extends ConfigException {
	private static final long serialVersionUID = -1060695875190325309L;

	public NoStepsSpecifiedException() {
	}
	
	@Override
	public String getMessage() {
		return "Output does not specify the number of wavelet steps to perform.";
	}
}
