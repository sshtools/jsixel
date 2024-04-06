# jsixel-zxing

This module lets you lets you encode 1D and 2D barcodes produced by the [Zxing](https://github.com/zxing/zxing).

*[Back To JSixel](../README.md)*    

## Quick Start

```xml
<dependency>
	<groupId>com.sshtools</groupdId>
	<artifactId>jsixel-zxing</artifactId>
	<version>0.0.2-SNAPSHOT</version>
</dependency>
```

### JPMS

```java
requires transitive com.sshtools.jsixel.zxing;
```

## Barcode -> Sixel

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

## Sixel -> Barcode

Not supported.
