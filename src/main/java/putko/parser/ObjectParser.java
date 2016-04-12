package putko.parser;

import lombok.Getter;
import lombok.Setter;

public interface ObjectParser {

	public <T> T readObject(String source, Class<T> objectType) throws Exception;
	
	public Configuration getConfiguration();
	
	public static class Configuration{
		@Setter @Getter
		private boolean ignoreNotAnnotatedElements = false;
	}
	
}
