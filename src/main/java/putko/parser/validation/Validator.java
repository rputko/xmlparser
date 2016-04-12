package putko.parser.validation;

public abstract class Validator<T> {
	
	public abstract void validate(T val) throws ValidationException;
	
}
