package putko.parser.exeptions;

public class ElementNotAnnotatedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7689146274717137823L;

	public ElementNotAnnotatedException(String name) {
		super("Element: " + name + " is not annotated with parser annotation");
	}

	
	
}
