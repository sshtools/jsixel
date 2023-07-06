package com.sshtools.jsixel.lib.bitmap;

import static com.sshtools.jsixel.lib.LibSixelExtensions.throwIfFailed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.sshtools.jsixel.lib.LibSixel;
import com.sshtools.jsixel.lib.bitmap.RawBitmap.RawBitmapBuilder;
import com.sshtools.jsixel.lib.util.DataArrays;
import com.sun.jna.ptr.PointerByReference;

public final class Sixel2Bitmap implements SixelConverter {

	public static class Sixel2BitmapBuilder {
		private Optional<ByteBuffer> data = Optional.empty();
		private Optional<InputStream> stream = Optional.empty();
		private Optional<Path> path = Optional.empty();
		private Optional<URL> url = Optional.empty();

		public Sixel2BitmapBuilder fromString(String string) {
			return fromData(ByteBuffer.wrap(string.getBytes()));
		}

		public Sixel2BitmapBuilder fromData(byte[] data) {
			return fromData(ByteBuffer.wrap(data));
		}

		public Sixel2BitmapBuilder fromData(ByteBuffer data) {
			this.data = Optional.of(data);
			return this;
		}

		public Sixel2BitmapBuilder fromStream(InputStream stream) {
			this.stream = Optional.of(stream);
			return this;
		}

		public Sixel2BitmapBuilder fromPath(Path path) {
			this.path = Optional.of(path);
			return this;
		}

		public Sixel2BitmapBuilder fromFile(File file) {
			return fromPath(file.toPath());
		}

		public Sixel2BitmapBuilder fromResource(String resource) {
			return fromResource(resource, getClass().getClassLoader());
		}

		public Sixel2BitmapBuilder fromResource(String resource, Class<?> clazz) {
			var url = clazz.getResource(resource);
			if (url == null)
				throw new UncheckedIOException(new FileNotFoundException(resource));
			return fromURL(url);
		}

		public Sixel2BitmapBuilder fromResource(String resource, ClassLoader loader) {
			var url = loader.getResource(resource);
			if (url == null)
				throw new UncheckedIOException(new FileNotFoundException(resource));
			return fromURL(url);
		}

		public Sixel2BitmapBuilder fromURL(URL url) {
			this.url = Optional.of(url);
			return this;
		}

		public Sixel2Bitmap build() throws IOException {
			return new Sixel2Bitmap(this);
		}
	}

	private final RawBitmap bitmap;

	private Sixel2Bitmap(Sixel2BitmapBuilder builder) throws IOException {
		ByteBuffer sixelData;

		if (builder.data.isPresent())
			sixelData = builder.data.get();
		else {
			var stream = builder.stream.orElse(null);
			if (stream == null) {
				if (builder.path.isPresent()) {
					try (var out = new ByteArrayOutputStream((int) Files.size(builder.path.get()))) {
						try (var in = Files.newInputStream(builder.path.get())) {
							in.transferTo(out);
						}
						sixelData = ByteBuffer.wrap(out.toByteArray());
					}
				} else {
					if (builder.url.isPresent()) {
						try (var out = new ByteArrayOutputStream()) {
							try (var in = builder.url.get().openStream()) {
								in.transferTo(out);
							}
							sixelData = ByteBuffer.wrap(out.toByteArray());
						}
					} else {
						throw new IllegalStateException(
								"No source of bitmap image provided to builder. Please use fromBitmap(), fromPath(), fromFile(), fromStream() etc");
					}
				}
			} else {
				try (var out = new ByteArrayOutputStream()) {
					stream.transferTo(out);
					sixelData = ByteBuffer.wrap(out.toByteArray());
				}
			}
		}

		var realWidth = IntBuffer.allocate(1);
		var realHeight = IntBuffer.allocate(1);
		var nColors = IntBuffer.allocate(1);

		var db = ByteBuffer.allocateDirect(sixelData.remaining());
		db.put(sixelData);
		db.flip();
		sixelData.flip();
		sixelData = db;

		var rem = sixelData.remaining();
		var palData = new PointerByReference();

		var imageRef = new PointerByReference();
		throwIfFailed(LibSixel.INSTANCE.sixel_decode_raw(sixelData, rem, imageRef, realWidth, realHeight, palData,
				nColors, (PointerByReference) null));

		var nColorsVal = nColors.get(0);

		var realWidthVal = realWidth.get();
		var realHeightVal = realHeight.get();
		var sz = realWidthVal * realHeightVal;
		var sixelBitmap = imageRef.getValue().getByteBuffer(0, sz);
		var palette = palData.getValue().getByteBuffer(0, nColorsVal * 3);

		bitmap = new RawBitmapBuilder().
				fromData(sixelBitmap).
				withPalette(DataArrays.toByteArray(palette)).
				withSize(realWidthVal, realHeightVal).
				withBitsPerPixel(8).
				withFormatType(FormatType.PALETTE).
				withPixelFormat(PixelFormat.PAL8). // TODO check monochrome
				build();
	}

	@Override
	public byte[] toByteArray() {
		return bitmap.byteArray();
	}

	@Override
	public ByteBuffer toByteBuffer() {
		return bitmap.data();
	}

	@Override
	public void write(WritableByteChannel writable) {
		bitmap.write(writable);
	}

	@Override
	public Bitmap bitmap() {
		return bitmap;
	}
}
