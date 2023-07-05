package com.sshtools.jsixel.swtimage;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.lib.util.ByteBufferBackedInputStream;
import com.sshtools.jsixel.lib.util.DataArrays;

public final class SWTImageBitmap implements Bitmap {

	private final static Map<PixelFormat, PaletteData> paletteData = new HashMap<>();
	
	static {
		paletteData.put(PixelFormat.ABGR8888, new PaletteData(0x000000ff, 0x0000ff00, 0x00ff0000));
		paletteData.put(PixelFormat.BGR888, new PaletteData(0x000000ff, 0x0000ff00, 0x00ff0000));
		paletteData.put(PixelFormat.ARGB8888, new PaletteData(0x00ff0000, 0x0000ff00, 0x000000ff));
		paletteData.put(PixelFormat.RGB888, new PaletteData(0xff000000, 0x00ff0000, 0x0000ff00));
		paletteData.put(PixelFormat.RGB555, new PaletteData(0x00007c00, 0x000003e0, 0x0000001f));
		paletteData.put(PixelFormat.RGB565, new PaletteData(0x0001f800, 0x000007e0, 0x0000001f));
		paletteData.put(PixelFormat.BGR555, new PaletteData(0x0000001f, 0x000003e0, 0x00007c00));
		paletteData.put(PixelFormat.BGR565, new PaletteData(0x0000001f, 0x000007e0, 0x0001f800));
		paletteData.put(PixelFormat.BGRA8888, new PaletteData(0x0000ff00, 0x00ff0000, 0xff000000));
	}

	public final static class SWTImageBitmapBuilder extends BitmapBuilder<SWTImageBitmapBuilder, SWTImageBitmap> {

		private Optional<ImageData> image = Optional.empty();
		private Optional<Bitmap> bitmap = Optional.empty();

		public SWTImageBitmapBuilder fromImage(ImageData image) {
			this.image = Optional.of(image);
			return this;
		}

		public SWTImageBitmapBuilder fromBitmap(Bitmap bitmap) {
			this.bitmap = Optional.of(bitmap);
			return this;
		}

		@Override
		public SWTImageBitmap build() {
			if (image.isPresent()) {
				return new SWTImageBitmap(image.get());
			} else {
				if (readable.isPresent()) {
					return new SWTImageBitmap(new ImageData(Channels.newInputStream(readable.get())));
				} else {
					if (bitmap.isPresent()) {
						var bm = bitmap.get();
						ImageData img;
						if (bm.formatType() == FormatType.PALETTE) {
							switch (bm.pixelFormat()) {
							case PAL8:
								img = new ImageData(bm.width(), bm.height(), bm.bitsPerPixel(),
										rgbPaletteToPaletteData(bm.pixelFormat(), bm.palette()));
								break;
							default:
								throw new UnsupportedOperationException();
							}
						} else if (bm.formatType() == FormatType.GRAYSCALE) {
							throw new UnsupportedOperationException();
						} else {
							var pd = paletteData.get(bm.pixelFormat());
							if(pd == null)
								throw new UnsupportedOperationException();
								
							img = new ImageData(bm.width(), bm.height(), bm.bitsPerPixel(), pd);
						}
						img.data = DataArrays.toByteArray(bm.data());
						return new SWTImageBitmap(img);
					} else if (data.isPresent()) {
						return new SWTImageBitmap(new ImageData(new ByteBufferBackedInputStream(data.get())));
					} else {
						throw new IllegalStateException("No image source supplied.");
					}
				}
			}
		}
	}

	private final ImageData image;
	private final PixelFormat format;
	private final FormatType formatType;
	private final byte[] palette;

	private SWTImageBitmap(ImageData image) {
		this.image = image;

		format = calcFormat(image);
		formatType = calcType(image);
		palette = calcPalette(image);
	}

	private PixelFormat calcFormat(ImageData image) {
		if(image.palette.isDirect) {
			for(var v : paletteData.entrySet()) {
				if(paletteDataEquals(image.palette, v.getValue())) {
					return v.getKey();
				}
			}
			throw new UnsupportedOperationException("Color depth of " + image.depth + " and palette " + image.palette + " not supported.");
		}
		else {
			if(image.depth == 8) {
				return PixelFormat.PAL8;
			}
			else
				throw new UnsupportedOperationException("Only 8 bit palette images supported.");
		}
	}

	private boolean paletteDataEquals(PaletteData p1, PaletteData p2) {
		return p1.isDirect == p2.isDirect &&
				Arrays.equals(p1.colors, p2.colors) &&
				p1.redMask == p2.redMask &&
				p1.redShift == p2.redShift &&
				p1.greenMask == p2.greenMask &&
				p1.greenShift == p2.greenShift &&
				p1.blueMask == p2.blueMask &&
				p1.blueShift == p2.blueShift;
	}

	@Override
	public int bitsPerPixel() {
		return image.depth;
	}

	@Override
	public void write(ByteBuffer buffer, PixelFormat fmt, FormatType formatType) {
		if (fmt != pixelFormat() || formatType != formatType()) {
			throw new UnsupportedOperationException("Conversion not supported");
		}
		// TODO lots of conversions here. Also take note of 16 bit image has reversed byte order
		buffer.put(image.data);
		
		buffer.flip();
	}

	@Override
	public FormatType formatType() {
		return formatType;
	}

	@Override
	public int height() {
		return image.height;
	}

	@Override
	public byte[] palette() {
		return palette;
	}

	@Override
	public PixelFormat pixelFormat() {
		return format;
	}

	@Override
	public int width() {
		return image.width;
	}

	ImageData image() {
		return image;
	}

	private static FormatType calcType(ImageData image) {
		if (image.palette.isDirect) {
			return FormatType.COLOR;
		} else
			return FormatType.PALETTE;
	}

	private static PaletteData rgbPaletteToPaletteData(PixelFormat pixelFormat, byte[] palette) {
		var l = new RGB[palette.length / 3];
		for (var i = 0; i < palette.length; i += 3) {
			l[i / 3] = new RGB(Byte.toUnsignedInt(palette[i]), Byte.toUnsignedInt(palette[i + 1]), Byte.toUnsignedInt(palette[i + 2]));
		}
		return new PaletteData(l);
	}

	private byte[] calcPalette(ImageData image) {
		if (image.palette.isDirect)
			return new byte[0];
		else {
			var b = new byte[image.palette.colors.length * 3];
			for (int i = 0; i < image.palette.colors.length; i++) {
				var rgb = image.palette.colors[i];
				b[i * 3] = (byte) rgb.red;
				b[(i * 3) + 1] = (byte) rgb.green;
				b[(i * 3) + 2] = (byte) rgb.blue;
			}
			return b;
		}
	}

}
