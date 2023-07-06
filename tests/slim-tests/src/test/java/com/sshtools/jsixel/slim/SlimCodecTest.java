package com.sshtools.jsixel.slim;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.Optional;

import org.junit.Test;

import com.sshtools.jsixel.lib.TestUtils;
import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel.Bitmap2SixelBuilder;
import com.sshtools.jsixel.lib.bitmap.BitmapCodec.ImageType;

public class SlimCodecTest {

	@Test
	public void testLoad() throws Exception {
		var codec = new SlimCodec();
		var tmp = Files.createTempFile("jsixel",".sixel");
		try (var in = SlimCodecTest.class.getResourceAsStream("/test.png")) {
			var bitmap = codec.load(Optional.of(ImageType.PNG), in);
			var enc = new Bitmap2SixelBuilder().
					fromBitmap(bitmap).
					build();
			enc.write(tmp);
		}
		
		try (var in = SlimCodecTest.class.getResourceAsStream("/test.sixel")) {
			try (var in2 = Files.newInputStream(tmp)) {
				assertTrue(TestUtils.isEqual(in, in2));
			}
		}
	}
}
