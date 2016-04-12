package putko.parser;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

public enum AllowedPrimitiveTypes {

	INT(Integer.TYPE),
	LONG(Long.TYPE),
	DOUBLE(Double.TYPE),
	FLOAT(Float.TYPE),
	BOOLEAN(Boolean.TYPE);
	
	@Getter @Setter
	private Class<?> type;
	
	AllowedPrimitiveTypes(Class<?> type){
		this.type = type;
	}
	
	public static Optional<AllowedPrimitiveTypes> getPrimitiveType(Class<?> type){
		return Arrays.stream(values()).filter(value-> value.getType().equals(type)).findFirst();
	}
	
}
