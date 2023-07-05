# jsixel-awt

This module lets you use `BufferedImage` from Java's Swing/AWT, allow you to either encode a BufferedImage
to sixel data, or decoding sixel data to a BufferedImage. 

## Quick Start

```xml
<dependency>
	<groupId>com.sshtools</groupdId>
	<artifactId>jsixel-awt</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

### JPMS

```java
requires transitive com.sshtools.jsixel.awt;
```

## BufferedImage -> Sixel

To convert a `BufferedImage` to a sixel stream.

```java

// Create the image codec
var codec = new BufferedImageCodec();

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

## Sixel -> BufferedImage

To convert a sixel stream to a `BufferedImage`..

```
XXXXX
```