# jsixel-slim

This module lets you lets you encode `PNG`, `JPEG` and `BMP` format images from any source to Sixel
image data streams. This is done without the weight of having to use a full toolkit such as
Swing/AWT or JavaFX, making it ideal from command line applications where you are most likely
to want Sixel support.

It does *not* support decoding of Sixel into any of these formats, and is based on some simple
decoders found in the [Slim LWJGL Library](https://github.com/mattdesl/slim/tree/master/slim/src/slim/texture/io).   

## Quick Start

```xml
<dependency>
	<groupId>com.sshtools</groupdId>
	<artifactId>jsixel-slim</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

### JPMS

```java
requires transitive com.sshtools.jsixel.slim;
```

## PNG, JPEG or BMP -> Sixel

To convert an image in `PNG`, `JPEG` or `BMP` format to a sixel stream.

```java

// Create the image codec
var codec = new SlimCodec();

try (var in = Jsixel.class.getResourceAsStream("jsixel.png")) {
	// Load the image
	var bitmap = codec.load(Optional.of(ImageType.PNG), in);
	
	// Build a sixel encoder
	var enc = new Bitmap2SixelBuilder().
			fromBitmap(bitmap).
			build();
			
	// Write the converted buffer to your output stream
	conv.write(System.out);
}

```

## Sixel -> PNG, JPEG or BMP

Not supported.
