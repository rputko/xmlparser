package putko.parser;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

public enum AllowedTypes {
	
	INT(Integer.class),
	LONG(Long.class),
	DOUBLE(Double.class),
	FLOAT(Float.class),
	BOOLEAN(Boolean.class),
	STRING(String.class);
	
	@Getter @Setter
	private Class<?> type;
	
	AllowedTypes(Class<?> type){
		this.type = type;
	}
	
	public static Optional<AllowedTypes> getType(Class<?> type){
		return Arrays.stream(values()).filter(value-> value.getType().equals(type)).findFirst();
	}
	
}
