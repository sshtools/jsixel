package com.sshtools.jsixel.javafx;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Objects;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.lib.util.ByteBufferBackedInputStream;
import com.sshtools.jsixel.lib.util.DataArrays;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public final class JavaFXImageBitmap implements Bitmap {

	public final static class JavaFXImageBitmapBuilder
			extends BitmapBuilder<JavaFXImageBitmapBuilder, JavaFXImageBitmap> {

		private Optional<Image> image = Optional.empty();
		private Optional<Bitmap> bitmap = Optional.empty();

		public JavaFXImageBitmapBuilder fromImage(Image image) {
			this.image = Optional.of(image);
			return this;
		}

		public JavaFXImageBitmapBuilder fromBitmap(Bitmap bitmap) {
			this.bitmap = Optional.of(bitmap);
			return this;
		}

		@Override
		public JavaFXImageBitmap build() {
			if (image.isPresent()) {
				return new JavaFXImageBitmap(image.get());
			} else {
				Image img;
				if(readable.isPresent()) {
					img = new Image(Channels.newInputStream(readable.get()));
				}
				else {
					if(bitmap.isPresent()) {
						var bm = bitmap.get();
						var t = calcType(bm);
						var wimg = new WritableImage(bm.width(), bm.height());
						if(bm.formatType() == FormatType.PALETTE) {
							switch(t) {
							case BYTE_INDEXED:
								wimg.getPixelWriter().setPixels(0, 0, bm.width(), bm.height(), indexedPixelFormat(palette), DataArrays.toByteArray(bm.data()), 0, bm.width());
								break;
							default:
								throw new UnsupportedOperationException();
							}
						}
						else if(bm.formatType() == FormatType.GRAYSCALE) {
							throw new UnsupportedOperationException();
						}
						else {
							switch(t) {
							case BYTE_BGRA:
							case BYTE_BGRA_PRE:
								wimg.getPixelWriter().setPixels(0, 0, bm.width(), bm.height(), javafx.scene.image.PixelFormat.getByteBgraInstance(), DataArrays.toByteArray(bm.data()), 0, 3 * bm.width());
								break;
							case INT_ARGB:
							case INT_ARGB_PRE:
								wimg.getPixelWriter().setPixels(0, 0, bm.width(), bm.height(), javafx.scene.image.PixelFormat.getIntArgbInstance(), DataArrays.toIntArray(bm.data()), 0, 4 * bm.width());
								break;
							default:
								throw new UnsupportedOperationException();
							}
						}
						img = wimg;
					}
					else if (data.isPresent()) {
						img = new Image(new ByteBufferBackedInputStream(data.get()));
					} else {
						throw new IllegalStateException("No image source supplied.");
					}
				}
				return new JavaFXImageBitmap(img);
			}
		}
	}

	private final Image image;
	private final PixelFormat format;
	private final FormatType formatType;

	private JavaFXImageBitmap(Image image) {

		this.image = image;
		
		format = calcFormat(image);
		formatType = calcType(image);
	}

	@Override
	public int bitsPerPixel() {
		return format.bpp();
	}

	@Override
	public void write(ByteBuffer buffer, PixelFormat fmt, FormatType formatType) {
		var defaultFormat = pixelFormat();
		if (!Objects.equals(fmt, defaultFormat)) {
			throw new UnsupportedOperationException("Conversion not supported.");
		}
		// TODO write image data to buffer
		
		if(formatType == FormatType.PALETTE) {
			switch(image.getPixelReader().getPixelFormat().getType()) {
//			case BYTE_INDEXED:
//				var arr = new byte[width() * height()];
//				image.getPixelReader().getPixels(0, 0, width(), height(), indexedPixelFormat(Optional.of(palette())), arr, 0, width());
//				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		else if(formatType == FormatType.GRAYSCALE) {
			throw new UnsupportedOperationException();
		}
		else {
			switch(image.getPixelReader().getPixelFormat().getType()) {
			case BYTE_BGRA: {
				var arr = new byte[width() * height() * 4];
				image.getPixelReader().getPixels(0, 0, width(), height(), WritablePixelFormat.getByteBgraInstance(), arr, 0, width() * 4);
				buffer.put(arr);
				break;
			}
			case BYTE_BGRA_PRE: {
				var arr = new byte[width() * height() * 4];
				image.getPixelReader().getPixels(0, 0, width(), height(), WritablePixelFormat.getByteBgraPreInstance(), arr, 0, width() * 4);
				buffer.put(arr);
				break;
			}
			case INT_ARGB: {
				var arr = new int[width() * height()];
				image.getPixelReader().getPixels(0, 0, width(), height(), WritablePixelFormat.getIntArgbInstance(), arr, 0, width() * 4);
				buffer.asIntBuffer().put(arr);
				break;
			}
			case INT_ARGB_PRE: {
				var arr = new int[width() * height()];
				image.getPixelReader().getPixels(0, 0, width(), height(), WritablePixelFormat.getIntArgbPreInstance(), arr, 0, width() * 4);
				buffer.asIntBuffer().put(arr);
				break;
			}
			default:
				throw new UnsupportedOperationException();
			}
		}
		

		buffer.flip();
	}

	@Override
	public FormatType formatType() {
		return formatType;
	}

	@Override
	public int height() {
		return (int)image.getHeight();
	}

	@Override
	public byte[] palette() {
		if(format.indexed()) {
			/* TODO: cant find how to do this (yet i hope) */
			throw new UnsupportedOperationException();
		}
		else {
			return new byte[0];
		}
	}

	@Override
	public PixelFormat pixelFormat() {
		return format;
	}

	@Override
	public int width() {
		return (int)image.getWidth();
	}

	Image image() {
		return image;
	}

	private PixelFormat calcFormat(Image image) {
		switch (image.getPixelReader().getPixelFormat().getType()) {
		case BYTE_BGRA:
		case BYTE_BGRA_PRE:
			return PixelFormat.BGRA8888;
		case BYTE_INDEXED:
			return PixelFormat.PAL8;
		case INT_ARGB:
		case INT_ARGB_PRE:
			return PixelFormat.ARGB8888;
		case BYTE_RGB:
			return PixelFormat.RGB888;
		default:
			throw new UnsupportedOperationException();
		}
	}

	private static FormatType calcType(Image image) {
		switch (image.getPixelReader().getPixelFormat().getType()) {
		case BYTE_INDEXED:
			return FormatType.PALETTE;
		default:
			return FormatType.COLOR;
		}
	}

	private static Type calcType(Bitmap fmt) {
		return calcType(fmt.pixelFormat(), fmt.formatType());
	}

	private static Type calcType(PixelFormat pixelFormat, FormatType formatType) {
		switch(formatType) {
		case COLOR:
			switch(pixelFormat) {
			case BGRA8888:
				return Type.BYTE_BGRA;
			case RGB888:
				return Type.BYTE_RGB;
			case ARGB8888:
				return Type.INT_ARGB;
			default:
				throw new UnsupportedOperationException();
			}
		case GRAYSCALE:
			throw new UnsupportedOperationException();
		case PALETTE:
			switch(pixelFormat) {
			case PAL8:
				return Type.BYTE_INDEXED;
			default:
				throw new UnsupportedOperationException();
			}
		default:
			throw new UnsupportedOperationException();
		}
	}

	private static javafx.scene.image.PixelFormat<ByteBuffer> indexedPixelFormat(Optional<byte[]> palette) {
		return javafx.scene.image.PixelFormat.createByteIndexedInstance(rgbPaletteToRgbaPalette(palette.orElseThrow(() -> new IllegalStateException("Palette must be supplied."))));
	}

	private static int[] rgbPaletteToRgbaPalette(byte[] d) {
		throw new UnsupportedOperationException();
	}

}
