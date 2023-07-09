package com.sshtools.jsixel.awt;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.Optional;

import org.junit.Test;

import com.sshtools.jsixel.awt.AWTImageBitmap.BufferedImageBitmapBuilder;
import com.sshtools.jsixel.lib.TestUtils;
import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel.Bitmap2SixelBuilder;
import com.sshtools.jsixel.lib.bitmap.BitmapLoader.ImageType;
import com.sshtools.jsixel.lib.bitmap.Sixel2Bitmap.Sixel2BitmapBuilder; 

public class AWTImageLoaderTest {

	@Test
	public void testLoad() throws Exception {
		var codec = new AWTImageLoader();
		var tmp = Files.createTempFile("jsixel",".sixel");
		try (var in = AWTImageLoaderTest.class.getResourceAsStream("/test.png")) {
			var bitmap = codec.load(Optional.of(ImageType.PNG), in);
			var enc = new Bitmap2SixelBuilder().
					fromBitmap(bitmap).
					build();
			enc.write(tmp);
		}
		
		try (var in = AWTImageLoaderTest.class.getResourceAsStream("/results/test.png.sixel")) {
			try (var in2 = Files.newInputStream(tmp)) {
				assertTrue(TestUtils.isEqual(in, in2));
			}
		}
	}
	
	@Test
	public void testSave() throws Exception {
		var codec = new AWTImageLoader();
		var enc = new Sixel2BitmapBuilder().
				fromURL(AWTImageLoaderTest.class.getResource("/test.sixel")).
				// TODO very weird ... doesnt work. But it is effectively the same as the above fromURL()!
				// fromResource("test.sixel", BufferedImageCodecTest.class). 
				build();
		var bitmap = enc.bitmap();
		var tmp = Files.createTempFile("jsixel",".png");
		try(var out = Files.newOutputStream(tmp)) {
			var img = new BufferedImageBitmapBuilder().
					fromBitmap(bitmap).
					build();
			codec.save(ImageType.PNG, img, out);	
		}
		
	}
}
