package com.sshtools.jsixel.slim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.BitmapLoader.ImageType;
import com.sshtools.jsixel.lib.util.ByteBufferBackedInputStream;

public interface SlimBitmap extends Bitmap {

	public final static class SlimBitmapBuilder extends BitmapBuilder<SlimBitmapBuilder, SlimBitmap> {

		private Optional<ImageType> typeHint;

		public SlimBitmapBuilder withTypeHint(Optional<ImageType> typeHint) {
			this.typeHint = typeHint;
			return this;

		}

		public SlimBitmapBuilder withTypeHint(ImageType typeHint) {
			return withTypeHint(Optional.of(typeHint));
		}

		@Override
		public SlimBitmap build() {
			try {
				if (readable.isPresent()) {
					switch (typeHint.or(this::guessType).orElseThrow(
							() -> new IllegalStateException("Type of image must be provided with this codec if it cannot be guessed."))) {
					case GIF:
						return new GIFBitmap(readFully(Channels.newInputStream(readable.get())));
					case BMP:
						return new BMPBitmap(readFully(Channels.newInputStream(readable.get())));
					case PNG:
						return new PNGBitmap(readFully(Channels.newInputStream(readable.get()))); 
					case JPEG:
						return new JPEGBitmap(readFully(Channels.newInputStream(readable.get())));
					default:
						throw new UnsupportedOperationException();
					}
				} else if (data.isPresent()) {
					switch (typeHint.or(this::guessType).orElseThrow(
							() -> new IllegalStateException("Type of image must be provided with this codec if it cannot be guessed."))) {
					case GIF:
						return new GIFBitmap(new ByteBufferBackedInputStream(data.get()));
					case PNG:
						return new PNGBitmap(new ByteBufferBackedInputStream(data.get()));
					case BMP:
						return new BMPBitmap(new ByteBufferBackedInputStream(data.get()));
					case JPEG:
						return new JPEGBitmap(new ByteBufferBackedInputStream(data.get()));
					default:
						throw new UnsupportedOperationException();
					}
				} else
					throw new UnsupportedOperationException();
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
		
		private InputStream readFully(InputStream in) throws IOException {
			var out  =new ByteArrayOutputStream();
			in.transferTo(out);
			return new ByteArrayInputStream(out.toByteArray());
		}

		Optional<ImageType> guessType() {
			return Optional.empty();
		}

	}
}
