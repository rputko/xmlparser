package putko.parser;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.junit.Assert;
import org.junit.Test;

import putko.parser.annotation.XmlElement;
import putko.parser.annotation.XmlRoot;
import putko.parser.exeptions.ElementNotAnnotatedException;
import putko.parser.exeptions.NoSuchElementException;
import putko.parser.exeptions.NotAllowedTypeException;
import putko.parser.validation.MinValidator;
import putko.parser.validation.MinimumValueException;
import putko.parser.validation.Valid;

public class ParserTest {

	@Test(expected=NoSuchElementException.class)
	public void testIncorrectElementName() throws Exception{
		ObjectParser parser = new XmlParser();
		parser.readObject("<Test><string>test</string><long>5</long><aa></aa></Test>", TestXml.class);
	}
	
	@Test
	public void shouldReadCorrectlyTextXml() throws Exception{
		ObjectParser parser = new XmlParser();
		TestXml object = parser.readObject(
				"<Test>"
				+ "<string>test</string>"
				+ "<long>5</long>"
				+ 	"<internal>"
				+ 		"<secondString>secondTest</secondString>"
				+ 		"<number>2</number>"
				+ 	"</internal>"
				+ "</Test>", TestXml.class);
		TestXml object1 = TestXml.builder().testString("test").
				testLong(5l).internal(InternalXml.builder().number(2)
						.secondString("secondTest").build()).build();
		Assert.assertEquals(object1, object);
	}
	
	@Test(expected=MinimumValueException.class)
	public void shouldThrowValidationException() throws Exception{
		ObjectParser parser = new XmlParser();
		parser.readObject("<validator><number>2</number></validator>", WithValidator.class);
	}
	
	@Test(expected=ElementNotAnnotatedException.class)
	public void shouldThrowElementNotAnnotatedException() throws Exception{
		ObjectParser parser = new XmlParser();
		parser.readObject("<validator><number>2</number></validator>", WithoutAnnotations.class);
	}
	
	@Test
	public void shouldNotThrowElementNotAnnotatedException() throws Exception{
		ObjectParser parser = new XmlParser();
		parser.getConfiguration().setIgnoreNotAnnotatedElements(true);
		parser.readObject("<WithoutAnnotations><number>2</number></WithoutAnnotations>", WithoutAnnotations.class);
	}
	
	@Test(expected=NotAllowedTypeException.class)
	public void shouldThrowNotAllowedType() throws Exception{
		ObjectParser parser = new XmlParser();
		parser.getConfiguration().setIgnoreNotAnnotatedElements(true);
		parser.readObject("<NotAllowedType><number>2</number></NotAllowedType>", NotAllowedType.class);
	}
	
	@Test
	public void testAllowedFieldTypes() throws Exception{
		ObjectParser parser = new XmlParser();
		TestTypes object = parser.readObject("<types>"
				+ "<string>test</string>"
				+ "<longValue>1231234</longValue>"
				+ "<doubleValue>6.4</doubleValue>"
				+ "<floatValue>33.52</floatValue>"
				+ "<integerValue>55</integerValue>"
				+ "<booleanValue>true</booleanValue>"
				+ "<longPrimitiveValue>9</longPrimitiveValue>"
				+ "<doublePrimitiveValue>344.7</doublePrimitiveValue>"
				+ "<floatPrimitiveValue>67.77</floatPrimitiveValue>"
				+ "<integerPrimitiveValue>1</integerPrimitiveValue>"
				+ "<booleanPrimitiveValue>false</booleanPrimitiveValue>"
				+ "</types>", TestTypes.class);
		TestTypes expected = TestTypes.builder()
				.text("test")
				.longValue(1231234l)
				.doubleValue(6.4d)
				.floatValue(33.52f)
				.integerValue(55)
				.booleanValue(true)
				.longPrimitiveValue(9l)
				.doublePrimitiveValue(344.7d)
				.floatPrimitiveValue(67.77f)
				.integerPrimitiveValue(1)
				.booleanPrimitiveValue(false)
				.build();
		Assert.assertEquals(expected, object);
	}
	
	@XmlRoot(name="types")
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@lombok.Builder
	public static class TestTypes{
		
		@XmlElement(name="string")
		@Getter @Setter
		private String text;
		
		@XmlElement(name="longValue")
		@Getter @Setter
		private Long longValue;
		
		@XmlElement(name="doubleValue")
		@Getter @Setter
		private Double doubleValue;
		
		@XmlElement(name="integerValue")
		@Getter @Setter
		private Integer integerValue;
		
		@XmlElement(name="booleanValue")
		@Getter @Setter
		private Boolean booleanValue;
		
		@XmlElement(name="floatValue")
		@Getter @Setter
		private Float floatValue;
		
		@XmlElement(name="longPrimitiveValue")
		@Getter @Setter
		private long longPrimitiveValue;
		
		@XmlElement(name="doublePrimitiveValue")
		@Getter @Setter
		private double doublePrimitiveValue;
		
		@XmlElement(name="integerPrimitiveValue")
		@Getter @Setter
		private int integerPrimitiveValue;
		
		@XmlElement(name="booleanPrimitiveValue")
		@Getter @Setter
		private boolean booleanPrimitiveValue;
		
		@XmlElement(name="floatPrimitiveValue")
		@Getter @Setter
		private float floatPrimitiveValue;
	}
	
	
	
	@XmlRoot(name="Test")
	@lombok.Builder
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class TestXml{
		
		@XmlElement(name="string")
		private String testString;
		
		@XmlElement(name="long")
		private Long testLong;
		
		@XmlElement(name="internal")
		private InternalXml internal;
	}
	
	@XmlRoot
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@lombok.Builder
	public static class InternalXml{
		@XmlElement(name="secondString")
		private String secondString;
		@XmlElement(name = "number")
		private int number;
	}
	
	@XmlRoot(name="validator")
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@lombok.Builder
	public static class WithValidator{
		@XmlElement(name = "number")
		@Valid({MinValidator.class})
		private Integer number;
	}
	
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@lombok.Builder
	public static class WithoutAnnotations{
		private Integer number;
	}	
	
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@XmlRoot
	@lombok.Builder
	public static class NotAllowedType{
		@XmlElement(name="number")
		private BigDecimal number;
	}	
	
}
