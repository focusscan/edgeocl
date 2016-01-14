package config;

public class InvalidNodeException extends ConfigException {
	private static final long serialVersionUID = -87395005834669195L;
	public final String expected;
	public final String got;
	
	public InvalidNodeException(String in_expected, String in_got) {
		expected = in_expected;
		got = in_got;
	}
	
	@Override
	public String getMessage() {
		return String.format("Invalid XML node: expected %s, got %s", expected, got);
	}
}
