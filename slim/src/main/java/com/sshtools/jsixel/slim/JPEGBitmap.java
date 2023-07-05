package com.sshtools.jsixel.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;

public class JPEGBitmap implements SlimBitmap {

	private JPEGDecoder decoder;
	private int width;
	private int height;

	JPEGBitmap(InputStream in) throws IOException {
		decoder = new JPEGDecoder(in);
		decoder.decodeHeader();
		width = decoder.getImageWidth();
		height = decoder.getImageHeight();
		if (!decoder.startDecode())
			throw new IOException("Failed to start decoding.");
	}

	@Override
	public void write(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
		try {
			decoder.decodeRGB(buffer, width * 4, decoder.getNumMCURows());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public FormatType formatType() {
		return FormatType.COLOR;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int bitsPerPixel() {
		return 32;
	}

	@Override
	public PixelFormat pixelFormat() {
		return PixelFormat.RGBA8888;
	}

	@Override
	public Optional<byte[]> palette() {
		return Optional.empty();
	}
}
