package putko.parser.exeptions;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

public class WrongAnnotationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6797241990266750113L;

	public WrongAnnotationException(
			Set<Class<? extends Annotation>> fieldParserAnnotations, String name) {
		super("Field " + name + " cannot be at once " + String.join(", ", fieldParserAnnotations.stream().map(annotation->{
			return annotation.getName();
		}).collect(Collectors.toList()).stream().toArray(String[]::new)));
	}

}
