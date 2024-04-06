package com.sshtools.jsixel.lib;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel;
import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel.Bitmap2SixelBuilder;

public class AbstractImageBitmapTests {

	protected void simpleTest(String imageResource, String sixelResource) throws Exception {
		simpleTest(imageResource, sixelResource, null);
	}

	protected void simpleTest(String imageResource, String sixelResource,
			Consumer<Bitmap2SixelBuilder> builderConfigure) throws Exception {
		var bldr = new Bitmap2SixelBuilder().fromResource(imageResource, AbstractImageBitmapTests.class);
		if (builderConfigure != null)
			builderConfigure.accept(bldr);
		var enc = bldr.build();
		System.out.println(imageResource + " = " + enc.bitmap());
		compare(sixelResource, enc);
	}

	protected void compare(String sixelResource, Bitmap2Sixel enc) throws IOException {
		var tmp = Files.createTempFile("jsixel", ".sixel");
		enc.write(tmp);
		try (var in = AbstractImageBitmapTests.class.getResourceAsStream(sixelResource)) {
			try (var in2 = Files.newInputStream(tmp)) {
				assertTrue(TestUtils.isEqual(in, in2));
			}
		}
	}
}
