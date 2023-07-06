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

import com.sshtools.jsixel.lib.LibSixel;
import com.sshtools.jsixel.lib.LibSixelExtensions;
import com.sun.jna.ptr.PointerByReference;

public final class Bitmap2Sixel implements SixelConverter {

	public static class Bitmap2SixelBuilder {
		private Optional<Boolean> optimizePalette = Optional.empty();
		private Optional<Integer> colors = Optional.empty();
		private Optional<Quality> quality = Optional.empty();
		private Optional<BuiltInPalette> builtInPalette = Optional.empty();
		private Optional<Integer> complexionScore = Optional.empty();
		private Optional<Integer> transparent = Optional.empty();
		private Optional<Bitmap> bitmap = Optional.empty();
		private Optional<byte[]> palette = Optional.empty();
		private Optional<Diffuse> diffuse = Optional.empty();
		private Optional<InputStream> stream = Optional.empty();
		private Optional<Path> path = Optional.empty();
		private Optional<URL> url = Optional.empty();
		private Optional<BitmapLoader.ImageType> type = Optional.empty();

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

		public Bitmap2SixelBuilder withType(BitmapLoader.ImageType type) {
			this.type = Optional.of(type);
			return this;
		}

		public Bitmap2SixelBuilder withColors(int colors) {
			this.colors = Optional.of(colors);
			return this;
		}

		public Bitmap2SixelBuilder withComplexionScore(int complexionScore) {
			this.complexionScore = Optional.of(complexionScore);
			return this;
		}

		public Bitmap2SixelBuilder withTransparent(int transparent) {
			this.transparent = Optional.of(transparent);
			return this;
		}

		public Bitmap2SixelBuilder withOptimizePalette(boolean optimizePalette) {
			this.optimizePalette = Optional.of(optimizePalette);
			return this;
		}

		public Bitmap2SixelBuilder withDiffuse(Diffuse diffuse) {
			this.diffuse = Optional.of(diffuse);
			return this;
		}

		public Bitmap2SixelBuilder withPalette(byte[] palette) {
			this.palette = Optional.of(palette);
			return this;
		}

		public Bitmap2SixelBuilder withQuality(Quality quality) {
			this.quality = Optional.of(quality);
			return this;
		}

		public Bitmap2SixelBuilder withPalette(BuiltInPalette builtInPalette) {
			this.builtInPalette = Optional.of(builtInPalette);
			return this;
		}

		public Bitmap2Sixel build() throws IOException {
			return new Bitmap2Sixel(this);
		}
	}

	private final Bitmap bitmap;
	private final ByteBuffer data;
	private final int colors;
	private final Optional<Diffuse> diffuse;
	private final Optional<Boolean> optimizePalette;
	private final Optional<Integer> complexionScore;
	private final Optional<byte[]> palette;
	private final Optional<Quality> quality;
	private final Optional<BuiltInPalette> builtInPalette;
	private final Optional<Integer> transparent;

	private Bitmap2Sixel(Bitmap2SixelBuilder builder) throws IOException {
		if (builder.bitmap.isPresent())
			bitmap = builder.bitmap.get();
		else {
			var stream = builder.stream.orElse(null);
			if (stream == null) {
				if (builder.path.isPresent()) {
					try (var in = Files.newInputStream(builder.path.get())) {
						bitmap = SixelConverter.defaultCodec().load(builder.type, in);
					}
				} else {
					if (builder.url.isPresent()) {
						try (var in = builder.url.get().openStream()) {
							bitmap = SixelConverter.defaultCodec().load(builder.type, in);
						}
					} else {
						throw new IllegalStateException(
								"No source of bitmap image provided to builder. Please use fromBitmap(), fromPath(), fromFile(), fromStream() etc");
					}
				}
			} else {
				bitmap = SixelConverter.defaultCodec().load(builder.type, stream);
			}
		}

		data = bitmap.data();
		colors = builder.colors.orElse(255);
		diffuse = builder.diffuse;
		palette = builder.palette.or(() -> bitmap.palette());
		complexionScore = builder.complexionScore;
		optimizePalette = builder.optimizePalette;
		quality = builder.quality;
		builtInPalette = builder.builtInPalette;
		transparent = builder.transparent;
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
	
	private PointerByReference createDither() {
		if (builtInPalette.isPresent())
			return LibSixel.INSTANCE.sixel_dither_get(builtInPalette.get().code());
		else {
			switch (bitmap.formatType()) {
			case COLOR:
				var dither = LibSixel.INSTANCE.sixel_dither_create(colors); 
				LibSixelExtensions.throwIfFailed(LibSixel.INSTANCE.sixel_dither_initialize(dither, data, bitmap.width(), bitmap.height(),
						bitmap.pixelFormat().code(), 0, 0, quality.map(Quality::code).orElse(0)));
				return dither;
			case PALETTE:
				return LibSixel.INSTANCE.sixel_dither_create(colors);
			case GRAYSCALE:
				switch (bitmap.pixelFormat()) {
				case G1:
					return LibSixel.INSTANCE.sixel_dither_get(LibSixel.SIXEL_BUILTIN_G1);
				default:
					return LibSixel.INSTANCE.sixel_dither_get(LibSixel.SIXEL_BUILTIN_G8);
				}
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	private void doOutput(PointerByReference output) {
		try {
			PointerByReference dither = createDither();
			try {
				LibSixel.INSTANCE.sixel_dither_set_pixelformat(dither, bitmap.pixelFormat().code());
				
				if(palette.isPresent()) {
					LibSixel.INSTANCE.sixel_dither_set_palette(dither, ByteBuffer.wrap(palette.get()));
				}
				
				if(diffuse.isPresent()) {
					LibSixel.INSTANCE.sixel_dither_set_diffusion_type(dither, diffuse.get().code());
				}
				
				if(complexionScore.isPresent()) {
					LibSixel.INSTANCE.sixel_dither_set_complexion_score(dither, complexionScore.get());
				}
				
				if(optimizePalette.isPresent()) {
					LibSixel.INSTANCE.sixel_dither_set_optimize_palette(dither, optimizePalette.get() ? 0 : 1);
				}
				
				if(transparent.isPresent()) {
					LibSixel.INSTANCE.sixel_dither_set_transparent(dither, transparent.get());
				}

				LibSixelExtensions.throwIfFailed(LibSixel.INSTANCE.sixel_encode(data, bitmap.width(), bitmap.height(), bitmap.bitsPerPixel(), dither, output));
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
