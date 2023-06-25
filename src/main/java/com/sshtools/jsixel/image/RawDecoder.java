package com.sshtools.jsixel.image;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class RawDecoder implements ImageDecoder {
	
	private final Format format;
	private final ByteBuffer data;
	private final int width;
	private final int height;
	private final ColorType colorType;
	private final int bytesPerPixel;
	private final byte[] palette;

	public RawDecoder(Format format, ColorType colorType, ByteBuffer data, int width, int height, int bytesPerPixel, byte[] palette) {
		this.format = format;
		this.colorType = colorType;
		this.data = data;
		this.width = width;
		this.height= height;
		this.bytesPerPixel = bytesPerPixel;
		this.palette = palette;
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
	public void decode(ByteBuffer buffer, Format format) throws IOException {
		if(this.format.equals(format)) {
			buffer.put(data);
			data.flip();
			buffer.flip();
		}
		else
			throw new UnsupportedOperationException("Differing formats.");
	}

	@Override
	public ColorType colorType() {
		return colorType;
	}

	@Override
	public int bytesPerPixel() {
		return bytesPerPixel;
	}

	@Override
	public Format format() {
		return format;
	}

	@Override
	public byte[] palette() {
		return palette;
	}

}
