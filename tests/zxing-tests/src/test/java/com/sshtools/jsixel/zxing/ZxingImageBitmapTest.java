package com.sshtools.jsixel.zxing;

import org.junit.Test;

import com.google.zxing.BarcodeFormat;
import com.sshtools.jsixel.lib.AbstractImageBitmapTests;
import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel;

public class ZxingImageBitmapTest extends AbstractImageBitmapTests {
	
	@Test
	public void testQR() throws Exception {
		compare("/results/test.qr.sixel", new Bitmap2Sixel.Bitmap2SixelBuilder().
				fromBitmap(new ZxingBitmap.ZxingBitmapBuilder().
						withWidth(256).
						withHeight(256).
						fromStream(ZxingImageBitmapTest.class.getResourceAsStream("/test.qr.data")).
						build()).
				build());
	}
	
	@Test
	public void testEAN13() throws Exception {
		compare("/results/test.ean13.sixel", new Bitmap2Sixel.Bitmap2SixelBuilder().
				fromBitmap(new ZxingBitmap.ZxingBitmapBuilder().
						withFormat(BarcodeFormat.EAN_13).
						withWidth(256).
						withHeight(128).
						fromContent("123456789012").
						build()).
				build());
	}

}
