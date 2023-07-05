package com.sshtools.jsixel.lib.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Optional;

public final class RawBitmap implements Bitmap {

	public final static class RawBitmapBuilder extends BitmapBuilder<RawBitmapBuilder, RawBitmap> {

		protected Optional<FormatType> formatType = Optional.empty();
		protected Optional<PixelFormat> pixelFormat = Optional.empty();

		public RawBitmapBuilder withFormatType(FormatType formatType) {
			this.formatType = Optional.of(formatType);
			return this;
		}

		public RawBitmapBuilder withPixelFormat(PixelFormat format) {
			this.pixelFormat = Optional.of(format);
			return this;
		}

		@Override
		public RawBitmap build() {
			return new RawBitmap(this);
		}
	}

	private final FormatType formatType;
	private final PixelFormat pixelFormat;
	private final ByteBuffer data;
	private final int width;
	private final int height;
	private final byte[] palette;
	private final int bitsPerPixel;

	private RawBitmap(RawBitmapBuilder builder) {
		this.formatType = builder.formatType
				.orElseThrow(() -> new IllegalStateException("Format type must be specified."));
		this.pixelFormat = builder.pixelFormat
				.orElseThrow(() -> new IllegalStateException("Format must be specified."));
		this.width = builder.width.orElseThrow(() -> new IllegalStateException("Width must be specified."));
		this.height = builder.height.orElseThrow(() -> new IllegalStateException("Height must be specified."));
		this.data = builder.data.orElseGet(() -> {
			if (builder.readable.isPresent()) {
				/* TODO probably better classes for this */
				var bout = new ByteArrayOutputStream();
				try {
					Channels.newInputStream(builder.readable.get()).transferTo(bout);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				return ByteBuffer.wrap(bout.toByteArray());
			} else
				throw new IllegalStateException("Data or stream must be specified.");

		});
		this.palette = builder.palette.orElse(new byte[0]);
		this.bitsPerPixel = builder.bitsPerPixel.orElse(this.data.remaining() / (this.width * this.height));
	}

	@Override
	public void write(ByteBuffer buffer, PixelFormat fmt, FormatType formatType)  {
		if (fmt != pixelFormat)
			throw new IllegalArgumentException("Cannot convert format.");

		buffer.put(data);
		buffer.flip();
		data.flip();
	}

	@Override
	public FormatType formatType() {
		return formatType;
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
		return bitsPerPixel;
	}

	@Override
	public PixelFormat pixelFormat() {
		return pixelFormat;
	}

	@Override
	public byte[] palette() {
		return palette;
	}

	@Override
    public ByteBuffer data() {
    	return data.asReadOnlyBuffer();
    }

	@Override
	public int dataByteSize() {
		return data.capacity();
	}

}
