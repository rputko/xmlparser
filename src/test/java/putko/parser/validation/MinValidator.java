package putko.parser.validation;

public class MinValidator extends Validator<Number> {

	@Override
	public void validate(Number value) throws ValidationException {
		if (value.intValue() < 4) {
			throw new MinimumValueException("Should be more than 3");
		}
	}

}
