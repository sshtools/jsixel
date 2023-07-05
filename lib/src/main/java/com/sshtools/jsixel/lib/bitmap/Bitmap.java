package com.sshtools.jsixel.lib.bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

import com.sshtools.jsixel.lib.util.DataArrays;

public interface Bitmap {

	@SuppressWarnings("unchecked")
	public abstract static class BitmapBuilder<BUILDER extends BitmapBuilder<BUILDER, BITMAP>, BITMAP>  {
		protected  Optional<ByteBuffer> data = Optional.empty();
		protected  Optional<ReadableByteChannel> readable = Optional.empty();
		protected  Optional<byte[]> palette = Optional.empty();
		
		protected  Optional<Integer> bitsPerPixel = Optional.empty();
		protected  Optional<Integer> width = Optional.empty();
		protected  Optional<Integer> height = Optional.empty();

		public BUILDER withWidth(int width) {
			this.width = Optional.of(width);
			return (BUILDER) this;
		}

		public BUILDER withHeight(int height) {
			this.height = Optional.of(height);
			return (BUILDER) this;
		}

		public BUILDER withSize(int width, int height) {
			return withWidth(width).withHeight(height);
		}

		public BUILDER withBitsPerPixel(int bitsPerPixel) {
			this.bitsPerPixel = Optional.of(bitsPerPixel);
			return (BUILDER) this;
		}

		public BUILDER withPalette(byte[] palette) {
			this.palette = Optional.of(palette);
			return (BUILDER) this;
		}

		public BUILDER fromData(ByteBuffer data) {
			this.data = Optional.of(data);
			return (BUILDER) this;
		}

		public BUILDER fromStream(InputStream in) {
			return fromChannel(Channels.newChannel(in));
		}

		public BUILDER fromChannel(ReadableByteChannel readable) {
			this.readable = Optional.of(readable);
			return (BUILDER) this;
		}
		
		public abstract BITMAP build();
	}

	default byte[] byteArray() {
		return DataArrays.toByteArray(data());
	}

    default ByteBuffer data() {
    	var buf = ByteBuffer.allocate(dataByteSize());
    	write(buf, pixelFormat(), formatType());
    	return buf;
    }

	default int dataByteSize() {
		return width() * height() * bytesPerPixel();
	}
	
	default void write(WritableByteChannel channel) {
		try {
			channel.write(data());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

    default void write(ByteBuffer buffer) {
    	write(buffer, pixelFormat(), formatType());
    }

    void write(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType);
    
	FormatType formatType();

	int width();

	int height();

	default int bytesPerPixel() {
		return bitsPerPixel() / 8;
	}

	int bitsPerPixel();

	PixelFormat pixelFormat();

	byte[] palette();
}
