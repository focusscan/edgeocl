package config;

public class NoNameSpecifiedException extends ConfigException {
	private static final long serialVersionUID = -3284630814817589246L;

	public NoNameSpecifiedException() {
	}
	
	@Override
	public String getMessage() {
		return "Output does not specify a name.";
	}
}
