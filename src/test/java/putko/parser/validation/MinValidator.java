package putko.parser.validation;

import java.lang.reflect.Field;

public class MinValidator extends Validator<Number> {

	@Override
	public void validate(Number value, Field field) throws ValidationException {
		if (value.intValue() < 4) {
			throw new MinimumValueException(field.getName() + "should have value greater than 3");
		}
	}

}
