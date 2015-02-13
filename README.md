# JSONBuilder

JSONBuilder is a simple non-intrusive Java library for generating JSON strings out of POJO object structures. It is non-intrusive in that it doesn't require any modification of existing POJO structures, including adding annotations.
 - Lightweight
 - Non-intrusive
 - Simple use

## Usage
### Basic usage

```java
Person person = new Person(12, "John Doe", "1978-10-21");
JSONBuilder json = new JSONBuilder();
json.include("id","name");
String s = json.serialize(person);
```
The String 's' now is equal to:
```json
{"id":12,"name":"John Doe"}
```
### Nested structures
Nested structures are supported
```java
Person person = new Person(12, "John Doe", "1978-10-21", new Address("Downing street", 12));
JSONBuilder json = new JSONBuilder();
json.include("id","name","address.street");
String s = json.serialize(person);
```
Will generate:
```json
{"id":12,"name":"John Doe","address":{"street":"Downing street"}}
```
### Transposition
Transposition allows properties to be relocated in the output tree under a different path than in the source tree.
```java
Person person = new Person(12, "John Doe", "1978-10-21", new Address("Downing street", 12));
JSONBuilder json = new JSONBuilder();
json.include("id","name");
json.includeTransposed("address.street","street")
String s = json.serialize(person);
```
Will generate:
```json
{"id":12,"name":"John Doe","street":"Downing street"}
```

### And more
 - Datatype conversion using json.withTransformer(...)
 - Serialization of collections (including primitives)
 - Serialization of maps (as key-value pairs)
 - Case conversion using Guava's CaseFormat as JSONBuilder constructor parameters
 - See the tests provided for samples
