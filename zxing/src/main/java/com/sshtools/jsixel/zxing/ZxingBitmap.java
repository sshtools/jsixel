package com.sshtools.jsixel.zxing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Optional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;

public interface ZxingBitmap extends Bitmap {

	public final static class ZxingBitmapBuilder extends BitmapBuilder<ZxingBitmapBuilder, ZxingBitmap> {

		private Optional<BitMatrix> matrix = Optional.empty();
		private Optional<String> content = Optional.empty();
		private Optional<Charset> charset = Optional.empty();
		private BarcodeFormat format = BarcodeFormat.QR_CODE;

		public ZxingBitmapBuilder withCharset(Charset charset) {
			this.charset = Optional.of(charset);
			return this;
		}

		public ZxingBitmapBuilder withCharset(String charset) {
			return withCharset(Charset.forName(charset));
		}

		public ZxingBitmapBuilder withMatrix(BitMatrix matrix) {
			this.matrix = Optional.of(matrix);
			return this;
		}

		public ZxingBitmapBuilder withFormat(BarcodeFormat format) {
			this.format = format;
			return this;
		}

		public ZxingBitmapBuilder fromContent(String content) {
			this.content = Optional.of(content);
			return this;
		}

		@Override
		public ZxingBitmap build() {
			try {
				if (matrix.isPresent()) {
					return new DefaultZxingBitmap(matrix.get());
				} else if (content.isPresent()) {
					return new DefaultZxingBitmap(
							new MultiFormatWriter().encode(content.get(), format, width.orElse(0), height.orElse(0)));
				} else if (readable.isPresent()) {
					return new DefaultZxingBitmap(new MultiFormatWriter().encode(read(readable.get()), format,
							width.orElse(0), height.orElse(0)));
				} else if (data.isPresent()) {
					return new DefaultZxingBitmap(new MultiFormatWriter().encode(read(data.get()), format,
							width.orElse(0), height.orElse(0)));
				} else
					throw new UnsupportedOperationException();
			} catch (WriterException we) {
				throw new IllegalStateException("Failed to build bitmap.", we);
			}
		}

		private String read(ReadableByteChannel readable) {
			var arr = new ByteArrayOutputStream();
			var in = ByteBuffer.allocate(1024);
			int numRead;
			try {
				while ((numRead = readable.read(in)) != -1) {
					in.flip();
					arr.write(in.array(), 0, numRead);
				}
				return new String(arr.toByteArray(), charset.orElse(Charset.defaultCharset()));
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		private String read(ByteBuffer buf) {
			var arr = new byte[buf.remaining()];
			buf.get(arr);
			return new String(arr, charset.orElse(Charset.defaultCharset()));
		}
	}

	public final static class DefaultZxingBitmap implements ZxingBitmap {

		private BitMatrix matrix;

		private DefaultZxingBitmap(BitMatrix matrix) {
			this.matrix = matrix;
			
		}

		@Override
		public boolean frame(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
			if (pixelFormat != pixelFormat())
				throw new IllegalArgumentException("Cannot convert format.");

			for(int y = 0 ; y < matrix.getHeight(); y++) {
				for(int x = 0 ; x < matrix.getWidth(); x++) {
					buffer.put((y * matrix.getWidth()) + x, (byte)(matrix.get(x, y) ? 0xff : 0));
				}
			}
			return false;
		}

		@Override
		public FormatType formatType() {
			return FormatType.GRAYSCALE;
		}

		@Override
		public int width() {
			return matrix.getWidth();
		}

		@Override
		public int height() {
			return matrix.getHeight();
		}

		@Override
		public PixelFormat pixelFormat() {
			// TODO other formats
			return PixelFormat.G8;
		}

		@Override
		public Optional<byte[]> palette() {
			return Optional.empty();
		}

	}
}
