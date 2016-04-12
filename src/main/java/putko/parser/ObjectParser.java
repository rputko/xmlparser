package putko.parser;

import javax.xml.stream.XMLStreamException;

import lombok.Getter;
import lombok.Setter;
import putko.parser.exeptions.ParserException;
import putko.parser.validation.ValidationException;

@SuppressWarnings("restriction")
public interface ObjectParser {

	public <T> T readObject(String source, Class<T> objectType) throws  XMLStreamException, ParserException, NoSuchFieldException, ValidationException;
	
	public Configuration getConfiguration();
	
	public static class Configuration{
		@Setter @Getter
		private boolean ignoreNotAnnotatedElements = false;
	}
	
}
