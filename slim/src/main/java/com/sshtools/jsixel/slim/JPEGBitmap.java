package com.sshtools.jsixel.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;

public class JPEGBitmap implements SlimBitmap {

	private final JPEGDecoder decoder;
	private final int width;
	private final int height;

	JPEGBitmap(InputStream in) throws IOException {
		decoder = new JPEGDecoder(in);
		decoder.decodeHeader();
		width = decoder.getImageWidth();
		height = decoder.getImageHeight();
		if (!decoder.startDecode())
			throw new IOException("Failed to start decoding.");
	}

	@Override
	public boolean frame(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
		try {
			decoder.decodeRGB(buffer, width * 4, decoder.getNumMCURows());
			buffer.flip();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return false;
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

	@Override
	public String toString() {
		return "JPEGBitmap [bitsPerPixel()=" + bitsPerPixel() + ",width()=" + width() + ", height()=" + height() + ", pixelFormat()=" + pixelFormat()
				+ ", formatType()=" + formatType() + ", palette()=" + palette() + "]";
	}
}
