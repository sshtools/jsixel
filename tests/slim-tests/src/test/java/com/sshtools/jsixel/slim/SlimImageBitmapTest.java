package com.sshtools.jsixel.slim;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sshtools.jsixel.lib.AbstractImageBitmapTests;
import com.sshtools.jsixel.lib.bitmap.SixelConverter;

public class SlimImageBitmapTest extends AbstractImageBitmapTests {
	
	@Test
	public void testLoader() {
		// Just make sure we have the right loader
		assertTrue(SixelConverter.defaultCodec() instanceof SlimLoader);
	}


	@Test
	public void testGif() throws Exception {
		simpleTest("/test.gif", "/results/test.gif.sixel");
	}

	@Test
	public void testPng() throws Exception {
		simpleTest("/test.png", "/results/test.png.sixel");
	}

	@Test
	public void testAlphaPng() throws Exception {
		simpleTest("/test.alpha.png", "/results/test.alpha.png.sixel", bldr -> {
			bldr.withTransparent(85);
		});
	}

	@Test
	public void testGifAnim() throws Exception {
		simpleTest("/anim.gif", "/results/anim.gif.sixel", bldr -> {
			bldr.withTransparent(0);
		});
	}

	@Test
	public void testPngIndexed() throws Exception {
		simpleTest("/test.indexed.png", "/results/test.indexed.png.sixel");
	}

	@Test
	public void testAlphaPngGrayScale8() throws Exception {
		simpleTest("/test.alpha.grayscale8.png", "/results/test.alpha.grayscale8.png.sixel");
	}

	@Test
	public void testAlphaPngGrayScale16() throws Exception {
		/* NOTE: native format not supported, will be converted to G8 */
		simpleTest("/test.alpha.grayscale16.png", "/results/test.alpha.grayscale8.png.sixel");
	}

	@Test
	public void testPngGrayScale8() throws Exception {
		simpleTest("/test.grayscale8.png", "/results/test.grayscale8.png.sixel");
	}

	@Test
	public void testPngGrayScale16() throws Exception {
		/* NOTE: native format not supported, will be converted to G8 */
		simpleTest("/test.grayscale16.png", "/results/test.grayscale8.png.sixel");
	}

	@Test
	public void testJpeg() throws Exception {
		simpleTest("/test.jpeg", "/results/test.jpeg.sixel");
	}

	@Test
	public void testBmp() throws Exception {
		simpleTest("/test.bmp", "/results/test.bmp.sixel");
	}
}
