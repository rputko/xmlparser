package putko.parser.exeptions;

public class NoSuchElementException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6979833806378086593L;

	public NoSuchElementException(String name){
		super("There is no element " + name + " in your class definition");
	}
	
}
