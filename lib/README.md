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
	<version>0.0.2-SNAPSHOT</version>
</dependency>
```

### JPMS

```java
requires com.sshtools.jsixel.lib;
```

## Low Level API

All `libsixel` functions are accessed in the same way, via `LibSixel.INSTANCE`. 

```java
var lib = LibSixel.INSTANCE;
var encoder = lib.sixel_encoder_create();
lib.sixel_encoder_setopt(encoder, LibSixel.SIXEL_OPTFLAG_DIFFUSION, "atkinson");
lib.sixel_encoder_encode(encoder, "test.png");
lib.sixel_encoder_unref(encoder);
``` 

That is about all you need to know. See `libsixel` examples and documentations for further detail on this API.
Function calls and arguments are documented in the Javadoc of this library.

## High Level File Based API

`Encoder` and `Decoder` work like their counterparts in `libsixel`. It can be used to convert image files
to sixel files and vice versa.

The above encoding example could be rewritten as ..

```java
	try(var encoder = new Encoder()) {
		encoder.opt(LibSixel.SIXEL_OPTFLAG_DIFFUSION, "atkinson");
		encoder.encode(Paths.get("test.png"));
	}
```

Decoding is much the same.

## High Level Toolkit Bitmap API

The most flexible API requires at least one of the optional modules. Choose the best module for your 
GUI toolkit if you are using one of [Swing/AWT](../swing/README.md), [JavaFX](../javafx/README.md), or
[SWT](../swt/README.md). Or pick the [Slim](../slim/READ.me) module for command line applications where
you might not want to depend on a full GUI toolkit.

You will need this API if you intend to working with image sources and targets other than local files
(`Path` and `File`). If you need to work with classpath resources, `InputStream`, `OutputStream`, `WritableChannel`, `ReadableChannel`, `URL`, `ByteBuffer`, 
, `byte[]` and `String`, this is the API for you. 

For most cases, you just need to ensure the optional module is on the classpath and included in
`module-info.java` if you are using JPMS. 

### Image To Sixel

#### The Quick Way

The quickest way to get a result is to use a `Bitmap2Sixel` instance, created by a `Bitmap2SixelBuilder`.

For example, to load a PNG image resource, and output sixel data to the console.

```java
var enc = new Bitmap2SixelBuilder().
		fromResource("jsixel.png", JSixel.class).
		build();
enc.write(System.out);		
```

As a second example showing the loading of the same PNG image, setting of encoding options, and obtaining a `ByteBuffer` of sixel data.

```java
var enc = new Bitmap2SixelBuilder().
		fromResource("jsixel.png", JSixel.class).
		withPalette(BuiltInPalette.MONO_DARK).
		build();
var buf = enc.toByteBuffer();		
```

#### Using a specific Bitmap type

The general pattern for converting an image (from any source), is to obtain the appropriate implementation
of a `Bitmap` via the corresponding `BitmapBuilder` implementation. 

Once you have a `Bitmap`, you then pass this to an instance of a `Bitmap2Sixel`, which itself is created
using a `Bitmap2SixelBuilder`. 
