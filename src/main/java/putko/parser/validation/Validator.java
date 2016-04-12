package putko.parser.validation;

import java.lang.reflect.Field;

public abstract class Validator<T> {
	
	public abstract void validate(T val, Field field) throws ValidationException;
	
}
