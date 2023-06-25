package com.sshtools.jsixel.converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.sshtools.jsixel.image.ImageDecoder;
import com.sshtools.jsixel.lib.SixelLibrary;
import com.sun.jna.ptr.PointerByReference;

public class Converter {
	static {
//		System.out.println("RP:" + Platform.RESOURCE_PREFIX);
	}
	
	private final ImageDecoder decoder;
	private final ByteBuffer data;

	public Converter(ImageDecoder decoder) throws IOException {
		this.decoder = decoder;
		
		data = ByteBuffer.allocate(decoder.width() * decoder.height() * decoder.bytesPerPixel());
		decoder.decode(data);
	}

	public void write(WritableByteChannel writable) {

		var output = SixelLibrary.INSTANCE.sixel_output_create((data, size, priv) -> {
			var buffer = data.getByteBuffer(0, size);
			try {
				writable.write(buffer);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return 0;
		}, null);
		
		
		try {
			PointerByReference dither;
			switch(decoder.colorType()) {
			case TRUEALPHA:
				dither = SixelLibrary.INSTANCE.sixel_dither_create(255);
				SixelLibrary.INSTANCE.sixel_dither_initialize(dither, data, decoder.width(), decoder.height(),
						SixelLibrary.SIXEL_PIXELFORMAT_RGBA8888, 0, 0, 0);
				break;
			case TRUECOLOR:
				//SixelLibrary.INSTANCE.sixel_dither_new(dither, 255, (PointerByReference)null);
				dither = SixelLibrary.INSTANCE.sixel_dither_create(255);
				SixelLibrary.INSTANCE.sixel_dither_initialize(dither, data, decoder.width(), decoder.height(),
						SixelLibrary.SIXEL_PIXELFORMAT_RGB888);
				break;
			case INDEXED:
				dither = SixelLibrary.INSTANCE.sixel_dither_create(255);
				SixelLibrary.INSTANCE.sixel_dither_set_palette(dither, ByteBuffer.wrap(decoder.palette()));
				SixelLibrary.INSTANCE.sixel_dither_set_pixelformat(dither, SixelLibrary.SIXEL_PIXELFORMAT_PAL8);
				break;
			case GREYSCALE:
		        dither = SixelLibrary.INSTANCE.sixel_dither_get(SixelLibrary.SIXEL_BUILTIN_G8);
		        SixelLibrary.INSTANCE.sixel_dither_set_pixelformat(dither, SixelLibrary.SIXEL_PIXELFORMAT_G8);
				break;
			case GREYALPHA:
		        dither = SixelLibrary.INSTANCE.sixel_dither_get(SixelLibrary.SIXEL_BUILTIN_G1);
		        SixelLibrary.INSTANCE.sixel_dither_set_pixelformat(dither, SixelLibrary.SIXEL_PIXELFORMAT_G1);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			
			try {
				SixelLibrary.INSTANCE.sixel_encode(data, decoder.width(), decoder.height(), 1, dither, output);
			}
			finally {
				SixelLibrary.INSTANCE.sixel_dither_unref(dither);
			}
		}
		finally {
			SixelLibrary.INSTANCE.sixel_output_unref(output);
		}
	}
}
