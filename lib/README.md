# jsixel-lib

This is the low level Java bindings to [libsixel](https://github.com/saitoha/libsixel) itself, the
high level file encoding and decoding API, and the high level bitmap encoding and decoding API 
(may require optional packages for full functionality).

*[Back To JSixel](../README.md)* 

## Quick Start

```xml
<dependency>
	<groupId>com.sshtools</groupdId>
	<artifactId>jsixel-lib</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

### JPMS

```java
requires com.sshtools.jsixel.lib;
```

## Low Level API

All `libsixel` functions are access in the same way, via `SixelLibrary.INSTANCE`. 

```java
var encoder =  SixelLibrary.INSTANCE.sixel_encoder_create();

// 

``` 

That is about all you need to known. See `libsixel` examples and documentations for further detail on this API.
Function calls and arguments are documented in the Javadoc of this library.

## High Level File Based API

`Encoder` and `Decoder` work like their counterparts in `libsixel`. 