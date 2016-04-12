package putko.parser.exeptions;

public class NotAllowedTypeException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4012642698436304378L;

	public NotAllowedTypeException(String fieldName, Class<?> genericType) {
		super("Type " + genericType.getName() + " of field " + fieldName + " is not allowed");
	}

}
