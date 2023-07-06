package com.sshtools.jsixel.awt;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sshtools.jsixel.lib.AbstractImageBitmapTests;
import com.sshtools.jsixel.lib.bitmap.SixelConverter;

public class AWTImageBitmapTest extends AbstractImageBitmapTests {
	
	@Test
	public void testLoader() {
		// Just make sure we have the right loader
		assertTrue(SixelConverter.defaultCodec() instanceof AWTImageLoader);
	}

	@Test
	public void testGif() throws Exception {
		simpleTest("/test.gif", "/test.gif.sixel");
	}

	@Test
	public void testPng() throws Exception {
		simpleTest("/test.png", "/test.png.sixel");
	}

	@Test
	public void testAlphaPng() throws Exception {
		simpleTest("/test.alpha.png", "/test.alpha.png.sixel", bldr -> {
			bldr.withTransparent(85);
		});
	}

	@Test
	public void testPngIndexed() throws Exception {
		simpleTest("/test.indexed.png", "/test.indexed.png.sixel");
	}

	@Test
	public void testAlphaPngGrayScale8() throws Exception {
		simpleTest("/test.alpha.grayscale8.png", "/test.alpha.grayscale8.png.sixel");
	}

	@Test
	public void testPngGrayScale8() throws Exception {
		simpleTest("/test.grayscale8.png", "/test.grayscale8.png.sixel");
	}

	@Test
	public void testPngGrayScale16() throws Exception {
		/* NOTE: native format not supported, will be converted to G8 */
		simpleTest("/test.grayscale16.png", "/test.grayscale8.png.sixel");
	}

	@Test
	public void testJpeg() throws Exception {
		simpleTest("/test.jpeg", "/test.jpeg.sixel");
	}

	@Test
	public void testBmp() throws Exception {
		simpleTest("/test.bmp", "/test.bmp.sixel");
	}
}
