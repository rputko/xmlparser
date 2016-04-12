# Overview

This projects aims to show aproach to maping Xml to plan Java objects.
It's built upon Reflections and Guava using Streaming API for XML (StAX)

There is no need to create a xml parser from scratch using for instance ANTLR.
So the main goal is to show how to use existing solution.

# Installation

Clone project and type:

```
mvn clean install
```

After that you can add maven dependency as follows:

```xml
<dependency>
	<groupId>putko</groupId>
	<artifactId>xmlparser</artifactId>
	<version>0.0.1</version>
</dependency>
```

# Usage

Just create parser
```java
ObjectParser parser = new XmlParser();
```
and you ready to go.

You can read POJO object from xml, for example:

```java
	@XmlRoot(name="Test")
	public class TestXml{
		
		@XmlElement(name="string")
		private String testString;
		
		@XmlElement(name="long")
		private Long testLong;
		
		@XmlElement(name="internal")
		private InternalXml internal;
	}
	
	@XmlRoot
	public static class InternalXml{
		@XmlElement(name="secondString")
		private String secondString;
		@XmlElement(name = "number")
		private int number;
	}

	TestXml object = parser.readObject("<Test><string>test</string><long>6</long><internal><secondString>test2</secondString><number>5</number></internal></Test>", TestXml.class);
```

You can specify name for elements or leave it as it is
	
#Configuration

You can ignore not annotated elements

```java
ObjectParser parser = new XmlParser();
parser.getConfiguration().setIgnoreNotAnnotatedElements(true);	
```
	
# Validators

You can add custom validators:

```java
@XmlElement(name="long")
@Valid({CustomValidator.class})
private Long testLong;
```

```java
public class MinValidator extends Validator<Number> {

	@Override
	public void validate(Number value) {
		if(value.intValue()<4){
			throw new MinimumValueException("Should be more than 3");
		}
	}
}

```
	
## Known Limitations

*Currently only elements (@XmlElement) and root (@XmlRoot) can be read, yet you can have POJO classes as type
*Attributes, Collections and Extended classes are not supported



