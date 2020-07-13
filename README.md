# json-generator

Annotation processor to generate toString method with JSON string output.

### Supported types

- Primitives
- Wrapper classes
- Annotated dependent classes
- Map implementations
- Collections implementations

Arrays are not supported yet. All unsupported types will be added as a JSON string value.

### Usage

1. Add following dependencies

```$xslt
annotationProcessor group: 'org.kushira.generator', name: 'json-generator', version: '0.0.1'
compile group: 'org.kushira.generator', name: 'json-generator', version: '0.0.1'
```

2. Add the following annotation to all the classes you wanted toString generated (including any dependent classes)

```Java
@JSONToString
public class Order {
   
}
```

3. Build the project

## Disclaimer

Project is under development. Please contact kushira@gmail.com.