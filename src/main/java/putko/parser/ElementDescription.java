package putko.parser;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import putko.parser.validation.Validator;

@ToString
@Builder
public class ElementDescription {

	@Getter @Setter
	private String fieldName;
	
	@Getter @Setter
	private ValueType type;
	
	@Getter @Setter
	private Class<?> genericType;
	
	@Getter @Setter
	private Set<Class<? extends Validator<?>>> validators;
	
}
