package com.sshtools.jsixel.lib.bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;

import com.sshtools.jsixel.lib.LibSixel;
import com.sshtools.jsixel.lib.LibSixelExtensions;
import com.sun.jna.ptr.PointerByReference;

public final class Bitmap2Sixel implements SixelConverter {

	public static class Bitmap2SixelBuilder {
		private Optional<Bitmap> bitmap = Optional.empty();
		private Optional<InputStream> stream = Optional.empty();
		private Optional<Path> path = Optional.empty();
		private Optional<URL> url = Optional.empty();
		private Optional<BitmapCodec.ImageType> type = Optional.empty();

		public Bitmap2SixelBuilder fromBitmap(Bitmap bitmap) {
			this.bitmap = Optional.of(bitmap);
			return this;
		}

		public Bitmap2SixelBuilder fromStream(InputStream stream) {
			this.stream = Optional.of(stream);
			return this;
		}

		public Bitmap2SixelBuilder fromPath(Path path) {
			this.path = Optional.of(path);
			return this;
		}

		public Bitmap2SixelBuilder fromFile(File file) {
			return fromPath(file.toPath());
		}

		public Bitmap2SixelBuilder fromResource(String resource) {
			return fromResource(resource, getClass().getClassLoader());
		}

		public Bitmap2SixelBuilder fromResource(String resource, Class<?> clazz) {
			var url = clazz.getResource(resource);
			if (url == null)
				throw new UncheckedIOException(new FileNotFoundException(resource));
			return fromURL(url);
		}

		public Bitmap2SixelBuilder fromResource(String resource, ClassLoader loader) {
			var url = loader.getResource(resource);
			if (url == null)
				throw new UncheckedIOException(new FileNotFoundException(resource));
			return fromURL(url);
		}

		public Bitmap2SixelBuilder fromURL(URL url) {
			this.url = Optional.of(url);
			return this;
		}

		public Bitmap2SixelBuilder withType(BitmapCodec.ImageType type) {
			this.type = Optional.of(type);
			return this;
		}

		public Bitmap2Sixel build() throws IOException {
			return new Bitmap2Sixel(this);
		}
	}

	private final Bitmap bitmap;
	private final ByteBuffer data;

	private Bitmap2Sixel(Bitmap2SixelBuilder builder) throws IOException {
		if (builder.bitmap.isPresent())
			bitmap = builder.bitmap.get();
		else {
			var stream = builder.stream.orElse(null);
			if (stream == null) {
				if (builder.path.isPresent()) {
					try (var in = Files.newInputStream(builder.path.get())) {
						bitmap = getCodec().load(builder.type, in);
					}
				} else {
					if (builder.url.isPresent()) {
						try (var in = builder.url.get().openStream()) {
							bitmap = getCodec().load(builder.type, in);
						}
					} else {
						throw new IllegalStateException(
								"No source of bitmap image provided to builder. Please use fromBitmap(), fromPath(), fromFile(), fromStream() etc");
					}
				}
			} else {
				bitmap = getCodec().load(builder.type, stream);
			}
		}

		data = bitmap.data();
	}

	private BitmapCodec<?, ?> getCodec() {
		return ServiceLoader.load(BitmapCodec.class).findFirst()
				.orElseThrow(() -> new UnsupportedOperationException(
						"No image codecs installed. Check you have one of the jsixel image codec modules available on the classpath."));
	}

	@Override
	public void write(WritableByteChannel writable) {

		var output = LibSixel.INSTANCE.sixel_output_create((data, size, priv) -> {
			var buffer = data.getByteBuffer(0, size);
			try {
				writable.write(buffer);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return 0;
		}, null);

		doOutput(output);
	}

	@Override
	public void write(StringBuilder stringBuffer) {

		var output = LibSixel.INSTANCE.sixel_output_create((data, size, priv) -> {
			var buffer = data.getByteArray(0, size);
			try {
				stringBuffer.append(new String(buffer, "ASCII"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
			return 0;
		}, null);

		doOutput(output);
	}

	private void doOutput(PointerByReference output) {
		try {
			PointerByReference dither = null;
			try {
				switch (bitmap.formatType()) {
				case COLOR:
					dither = LibSixel.INSTANCE.sixel_dither_create(255);
					LibSixelExtensions.throwIfFailed(LibSixel.INSTANCE.sixel_dither_initialize(dither, data, bitmap.width(), bitmap.height(),
							bitmap.pixelFormat().pixelFormatCode(), 0, 0, 0));
					break;
				case PALETTE:
					dither = LibSixel.INSTANCE.sixel_dither_create(255);
					LibSixel.INSTANCE.sixel_dither_set_palette(dither, ByteBuffer.wrap(bitmap.palette()));
					LibSixel.INSTANCE.sixel_dither_set_pixelformat(dither, bitmap.pixelFormat().pixelFormatCode());
					break;
				case GRAYSCALE:
					switch (bitmap.pixelFormat()) {
					case G1:
						dither = LibSixel.INSTANCE.sixel_dither_get(LibSixel.SIXEL_BUILTIN_G1);
					default:
						dither = LibSixel.INSTANCE.sixel_dither_get(LibSixel.SIXEL_BUILTIN_G8);
					}
					LibSixel.INSTANCE.sixel_dither_set_pixelformat(dither, bitmap.pixelFormat().pixelFormatCode());
					break;
				default:
					throw new UnsupportedOperationException();
				}

				LibSixelExtensions.throwIfFailed(LibSixel.INSTANCE.sixel_encode(data, bitmap.width(), bitmap.height(), 1, dither, output));
			} finally {
				if (dither != null)
					LibSixel.INSTANCE.sixel_dither_unref(dither);
			}
		} finally {
			LibSixel.INSTANCE.sixel_output_unref(output);
		}
	}

	@Override
	public Bitmap bitmap() {
		return bitmap;
	}
}
