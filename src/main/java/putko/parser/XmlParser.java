package putko.parser;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.Getter;

import org.reflections.Reflections;

import putko.parser.annotation.XmlElement;
import putko.parser.annotation.XmlRoot;
import putko.parser.exeptions.ElementNotAnnotatedException;
import putko.parser.exeptions.NoSuchElementException;
import putko.parser.exeptions.NotAllowedTypeException;
import putko.parser.exeptions.ParserException;
import putko.parser.exeptions.WrongAnnotationException;
import putko.parser.validation.Valid;
import putko.parser.validation.ValidationException;
import putko.parser.validation.Validator;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class XmlParser implements ObjectParser {

	private static final Set<Class<? extends Annotation>> ALLOWED_FIELD_ANNOTATIONS = Sets.newHashSet();
	private static final Set<Class<? extends Annotation>> ALLOWED_CLASS_ANNOTATIONS = Sets.newHashSet();
	private static HashMap<Class<? extends Validator<?>>, Validator<?>> ACTIVE_VALIDATORS = Maps.newHashMap();

	@Getter
	private Configuration configuration = new Configuration();

	static {
		Reflections reflections = new Reflections("putko.parser.annotation");
		Set<Class<? extends Annotation>> allAnnotations = reflections.getSubTypesOf(Annotation.class);
		for (Class<? extends Annotation> annotation : allAnnotations) {
			Target target = annotation.getAnnotation(Target.class);
			if (Arrays.stream(target.value()).allMatch(t -> t.equals(ElementType.FIELD))) {
				ALLOWED_FIELD_ANNOTATIONS.add(annotation);
			}
			if (Arrays.stream(target.value()).allMatch(t -> t.equals(ElementType.TYPE))) {
				ALLOWED_CLASS_ANNOTATIONS.add(annotation);
			}
		}
	}

	private static boolean isAllowedType(Class<?> type) {
		return AllowedTypes.getType(type).isPresent();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T> T readObject(String source, Class<T> objectType) throws ParserException, XMLStreamException, ValidationException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
		String currentName = "";
		ElementDescription element = null;
		NavigableMap<String, T> elementTree = Maps.newTreeMap();
		HashMap<String, HashMap<String, ElementDescription>> objectElementsMap = Maps.newHashMap();
		while (reader.hasNext()) {
			int event = reader.next();
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (elementTree.isEmpty()) {
					HashMap<String, ElementDescription> elements = getObjectElements(objectType);
					element = elements.get(reader.getLocalName());
					if (element == null && !configuration.isIgnoreNotAnnotatedElements()) {
						throw new NoSuchElementException(reader.getLocalName());
					}
					objectElementsMap.put(reader.getLocalName(), elements);
					try {
						elementTree.put(reader.getLocalName(), objectType.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						throw new NotAllowedTypeException(currentName, element.getGenericType());
					}
				}
				element = objectElementsMap.get(elementTree.lastEntry().getKey()).get(reader.getLocalName());
				if (element == null) {
					if (!configuration.isIgnoreNotAnnotatedElements())
						throw new NoSuchElementException(reader.getLocalName());
					break;
				}
				switch (element.getType()) {
				case ELEMENT:
					currentName = reader.getLocalName();
					if (!element.getGenericType().isPrimitive() && !isAllowedType(element.getGenericType())) {
						Class<?> c = null;
						try {
							c = Class.forName(element.getGenericType().getTypeName());
							elementTree.put(currentName, (T) c.newInstance());
						} catch (Exception e) {
							throw new NotAllowedTypeException(currentName, element.getGenericType());
						}
						objectElementsMap.put(currentName, getObjectElements(c));
					}
					break;
				default:
					break;
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				if (element != null) {
					Entry<String, T> entry = elementTree.lastEntry();
					Field field;
					try {
						field = entry.getValue().getClass().getDeclaredField(objectElementsMap.get(entry.getKey()).get(currentName).getFieldName());
					} catch (NoSuchFieldException | SecurityException e) {
						throw (ParserException) e.getCause();
					}
					try {
						setFieldValue(field, entry.getValue(), reader.getText().trim(), element);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw (ParserException) e.getCause();
					}

				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (element != null) {
					element = objectElementsMap.get(elementTree.lastEntry().getKey()).get(reader.getLocalName());
					if (element == null) {
						String key = (String) elementTree.keySet().toArray()[elementTree.size() - 2];
						element = objectElementsMap.get(key).get(reader.getLocalName());
					}
					if (elementTree.size() > 1) {
						switch (element.getType()) {
						case ELEMENT:
							if (!element.getGenericType().isPrimitive() && !isAllowedType(element.getGenericType())) {
								try {
									setElement(elementTree, objectElementsMap);
								} catch (NoSuchFieldException e) {
									throw (ParserException) e.getCause();
								}
								elementTree.remove(elementTree.lastEntry().getKey());
							}
							break;
						case ROOT:
							try {
								setElement(elementTree, objectElementsMap);
							} catch (NoSuchFieldException e) {
								throw (ParserException) e.getCause();
							}
							elementTree.remove(elementTree.lastEntry().getKey());
							break;
						default:
							break;
						}
					}
				}
				break;
			}
		}
		return elementTree.lastEntry().getValue();
	}

	private <T> void setElement(NavigableMap<String, T> elementTree, HashMap<String, HashMap<String, ElementDescription>> objectElementsMap)
			throws NoSuchFieldException, ParserException, ValidationException {
		String key = (String) elementTree.keySet().toArray()[elementTree.size() - 2];
		Field field = elementTree.get(key).getClass()
				.getDeclaredField(objectElementsMap.get(key).get(elementTree.lastEntry().getKey()).getFieldName());
		field.setAccessible(true);
		validateField(elementTree.lastEntry().getValue(), field);
		try {
			field.set(elementTree.get(key), elementTree.lastEntry().getValue());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw (ParserException) e.getCause();
		}
	}

	private <T> void setFieldValue(Field field, T value, String text, ElementDescription element) throws NumberFormatException,
			IllegalArgumentException, IllegalAccessException, ValidationException, NotAllowedTypeException {
		field.setAccessible(true);
		if (element.getGenericType().isPrimitive()) {
			if (!AllowedPrimitiveTypes.getPrimitiveType(element.getGenericType()).isPresent()) {
				throw new NotAllowedTypeException(field.getName(), element.getGenericType());
			}
			switch (AllowedPrimitiveTypes.getPrimitiveType(element.getGenericType()).get()) {
			case INT:
				setIntValue(field, value, text);
				break;
			case DOUBLE:
				setDoubleValue(field, value, text);
				break;
			case FLOAT:
				setFloatValue(field, value, text);
				break;
			case BOOLEAN:
				setBooleanValue(field, value, text);
				break;
			case LONG:
				setLongValue(field, value, text);
				break;
			default:
				break;
			}
		} else {
			if (!AllowedTypes.getType(element.getGenericType()).isPresent()) {
				throw new NotAllowedTypeException(field.getName(), element.getGenericType());
			}
			switch (AllowedTypes.getType(element.getGenericType()).get()) {
			case INT:
				setIntValue(field, value, text);
				break;
			case DOUBLE:
				setDoubleValue(field, value, text);
				break;
			case FLOAT:
				setFloatValue(field, value, text);
				break;
			case BOOLEAN:
				setBooleanValue(field, value, text);
				break;
			case LONG:
				setLongValue(field, value, text);
				break;
			case STRING:
				validateField(text, field);
				field.set(value, text);
				break;
			default:
				break;
			}
		}
	}

	private <T> void setIntValue(Field field, T value, String text) throws NumberFormatException, IllegalArgumentException, IllegalAccessException,
			ValidationException {
		Integer val = Integer.parseInt(text);
		validateField(val, field);
		field.set(value, val);
	}

	private <T> void setDoubleValue(Field field, T value, String text) throws NumberFormatException, IllegalArgumentException,
			IllegalAccessException, ValidationException {
		Double val = Double.parseDouble(text);
		validateField(val, field);
		field.set(value, val);
	}

	private <T> void setFloatValue(Field field, T value, String text) throws NumberFormatException, IllegalArgumentException, IllegalAccessException,
			ValidationException {
		Float val = Float.parseFloat(text);
		validateField(val, field);
		field.set(value, val);
	}

	private <T> void setBooleanValue(Field field, T value, String text) throws IllegalArgumentException, IllegalAccessException, ValidationException {
		Boolean val = Boolean.parseBoolean(text);
		validateField(val, field);
		field.set(value, val);
	}

	private <T> void setLongValue(Field field, T value, String text) throws NumberFormatException, IllegalArgumentException, IllegalAccessException,
			ValidationException {
		Long val = Long.parseLong(text);
		validateField(val, field);
		field.set(value, val);
	}

	private <T> HashMap<String, ElementDescription> getObjectElements(Class<T> object) throws ElementNotAnnotatedException, WrongAnnotationException,
			NotAllowedTypeException {
		Set<Class<? extends Annotation>> classParserAnnotations = Sets.intersection(ALLOWED_CLASS_ANNOTATIONS, Arrays.stream(object.getAnnotations())
				.map(annotation -> {
					return annotation.annotationType();
				}).collect(Collectors.toSet()));
		validateElementAnnotations(object.getName(), classParserAnnotations, !configuration.isIgnoreNotAnnotatedElements());
		String name = object.getAnnotation(XmlRoot.class) == null ? "" : object.getAnnotation(XmlRoot.class).name();
		HashMap<String, ElementDescription> objectElements = Maps.newHashMap();
		objectElements.put(name.isEmpty() ? object.getSimpleName() : name,
				ElementDescription.builder().genericType(object.getClass()).fieldName(object.getSimpleName()).type(ValueType.ROOT).build());
		Arrays.stream(object.getDeclaredFields()).forEach(
				field -> {
					Set<Class<? extends Annotation>> fieldParserAnnotations = Sets.intersection(ALLOWED_FIELD_ANNOTATIONS,
							Arrays.stream(field.getAnnotations()).map(annotation -> {
								return annotation.annotationType();
							}).collect(Collectors.toSet()));
					validateElementAnnotations(field.getName(), fieldParserAnnotations, !configuration.isIgnoreNotAnnotatedElements());
					putFieldToObjectElements(field, objectElements);
				});
		return objectElements;
	}

	private void putFieldToObjectElements(Field field, HashMap<String, ElementDescription> objectElements) {
		ElementDescription elementDescription = ElementDescription.builder().build();
		if (field.getAnnotation(XmlElement.class) != null) {
			XmlElement annotation = field.getAnnotation(XmlElement.class);
			elementDescription.setType(ValueType.ELEMENT);
			elementDescription.setFieldName(field.getName());
			elementDescription.setGenericType(field.getType());
			objectElements.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), elementDescription);
		}
	}

	private static void validateElementAnnotations(String elementName, Set<Class<? extends Annotation>> fieldParserAnnotations, boolean performCheck)
			throws WrongAnnotationException, ElementNotAnnotatedException {
		if (performCheck && fieldParserAnnotations.size() == 0)
			throw new ElementNotAnnotatedException(elementName);
		if (fieldParserAnnotations.size() >= 2)
			throw new WrongAnnotationException(fieldParserAnnotations, elementName);
	}

	@SuppressWarnings("unchecked")
	private <T> void validateField(T val, Field field) throws ValidationException {
		if (field.getAnnotation(Valid.class) == null) {
			return;
		}
		Class<? extends Validator<?>>[] validators = field.getAnnotation(Valid.class).value();
		for(int i=0; i<validators.length; i++){
			Validator<T> validatorObject = (Validator<T>) ACTIVE_VALIDATORS.get(validators[i]);
			if (validatorObject == null) {
				try {
					validatorObject = (Validator<T>) validators[i].newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw (RuntimeException)e.getCause();
				}
				ACTIVE_VALIDATORS.put(validators[i], validatorObject);
			}
			validatorObject.validate(val, field);
		};
	}

}
