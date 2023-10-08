package com.sshtools.jsixel.swt;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.lib.util.ByteBufferBackedInputStream;
import com.sshtools.jsixel.lib.util.DataArrays;

public final class SWTImageBitmap implements Bitmap {

	private final static Map<FormatKey, PaletteData> paletteData = new HashMap<>();
	
	static final class FormatKey {
		final PixelFormat pixelFormat;
		final int depth;
		
		FormatKey(PixelFormat pixelFormat, int depth) {
			this.pixelFormat = pixelFormat;
			this.depth = depth;
		}

		@Override
		public int hashCode() {
			return Objects.hash(depth, pixelFormat);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FormatKey other = (FormatKey) obj;
			return depth == other.depth && Objects.equals(pixelFormat, other.pixelFormat);
		}
		
	}
	
	static {
		paletteData.put(new FormatKey(PixelFormat.ABGR8888, 32), new PaletteData(0x000000ff, 0x0000ff00, 0x00ff0000));
		paletteData.put(new FormatKey(PixelFormat.RGB888, 24), new PaletteData(0x00ff0000, 0x0000ff00, 0x000000ff));
		paletteData.put(new FormatKey(PixelFormat.ARGB8888, 32), new PaletteData(0x00ff0000, 0x0000ff00, 0x000000ff));
		paletteData.put(new FormatKey(PixelFormat.BGR888, 24), new PaletteData(0xff000000, 0x00ff0000, 0x0000ff00));
		paletteData.put(new FormatKey(PixelFormat.RGB555, 16), new PaletteData(0x00007c00, 0x000003e0, 0x0000001f));
		paletteData.put(new FormatKey(PixelFormat.RGB565, 16), new PaletteData(0x0001f800, 0x000007e0, 0x0000001f));
		paletteData.put(new FormatKey(PixelFormat.BGR555, 16), new PaletteData(0x0000001f, 0x000003e0, 0x00007c00));
		paletteData.put(new FormatKey(PixelFormat.BGR565, 16), new PaletteData(0x0000001f, 0x000007e0, 0x0001f800));
		paletteData.put(new FormatKey(PixelFormat.BGRA8888, 32), new PaletteData(0x0000ff00, 0x00ff0000, 0xff000000));
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

		@SuppressWarnings("resource")
		@Override
		public SWTImageBitmap build() { 
			if (image.isPresent()) {
				return new SWTImageBitmap(image.get());
			} else {
				var loader = new ImageLoader();
				if (readable.isPresent()) {
					return new SWTImageBitmap(loader.load(Channels.newInputStream(readable.get())));
				} else {
					if (bitmap.isPresent()) {
						var bm = bitmap.get();
						ImageData img;
						if (bm.formatType() == FormatType.PALETTE) {
							switch (bm.pixelFormat()) {
							case PAL8:
								img = new ImageData(bm.width(), bm.height(), bm.bitsPerPixel(),
										rgbPaletteToPaletteData(bm.pixelFormat(), bm.palette().orElseThrow(() -> new IllegalStateException("Bitmap has no palette."))));
								break;
							default:
								throw new UnsupportedOperationException();
							}
						} else if (bm.formatType() == FormatType.GRAYSCALE) {
							throw new UnsupportedOperationException();
						} else {
							var pd = paletteData.get(new FormatKey(bm.pixelFormat(), bm.bitsPerPixel()));
							if(pd == null)
								throw new UnsupportedOperationException();
								
							img = new ImageData(bm.width(), bm.height(), bm.bitsPerPixel(), pd);
						}
						img.data = DataArrays.toByteArray(bm.data());
						return new SWTImageBitmap(img);
					} else if (data.isPresent()) {
						return new SWTImageBitmap(loader.load(new ByteBufferBackedInputStream(data.get())));
					} else {
						throw new IllegalStateException("No image source supplied.");
					}
				}
			}
		}
	}

	private final ImageData[] images;
	private final PixelFormat format;
	private final FormatType formatType;
	private final Optional<byte[]> palette;
	private int frame;

	private SWTImageBitmap(ImageData... images) {
		this.images = images;

		format = calcFormat(images[0]);
		formatType = calcType(images[0]);
		palette = calcPalette(images[0]);
	}

	private PixelFormat calcFormat(ImageData image) {
		if(image.palette.isDirect) {
			for(var v : paletteData.entrySet()) {
				if(v.getKey().depth == image.depth && paletteDataEquals(image.palette, v.getValue())) {
					return v.getKey().pixelFormat;
				}
			}
			throw new UnsupportedOperationException("Color depth of " + image.depth + " and palette " + paletteInfo(image) + " not supported.");
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
		return images[0].depth;
	}

	@Override
	public boolean frame(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
		if (pixelFormat != pixelFormat() || formatType != formatType()) {
			throw new UnsupportedOperationException("Conversion not supported");
		}
		if(frame >= images.length) {
			frame = 0;
		}
		if(frame < images.length) {
			buffer.put(images[frame].data);
			
			buffer.flip();
			frame++;
		}
		return hasMoreFrames();

	}
	
	@Override
	public long delay() {
		return frame < images.length ? images[frame].delayTime * 10 : 0;
	}

	@Override
	public boolean hasMoreFrames() {
		return frame < images.length;
	}

	@Override
	public FormatType formatType() {
		return formatType;
	}

	@Override
	public int height() {
		return images[0].height;
	}

	@Override
	public Optional<byte[]> palette() {
		return palette;
	}

	@Override
	public PixelFormat pixelFormat() {
		return format;
	}

	@Override
	public int width() {
		return images[0].width;
	}

	ImageData[] images() {
		return images;
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

	private Optional<byte[]> calcPalette(ImageData image) {
		if (image.palette.isDirect)
			return Optional.empty();
		else {
			var b = new byte[image.palette.colors.length * 3];
			for (int i = 0; i < image.palette.colors.length; i++) {
				var rgb = image.palette.colors[i];
				b[i * 3] = (byte) rgb.red;
				b[(i * 3) + 1] = (byte) rgb.green;
				b[(i * 3) + 2] = (byte) rgb.blue;
			}
			return Optional.of(b);
		}
	}

	@Override
	public String toString() {
		var pd = paletteInfo(images[0]);
		return "SWTImageBitmap [bitsPerPixel()=" + bitsPerPixel() + ",width()=" + width() + ", height()=" + height() + ", pixelFormat()=" + pixelFormat()
				+ ", formatType()=" + formatType() + ", palette()=" + palette() + ",pd=" + pd + ",transparentPixel=" + images[0].transparentPixel + ",sourceDpeth" + images[0].depth + ",type=" + images[0].type + ",maskPad=" + images[0].maskPad + "]";
	}

	private String paletteInfo(ImageData image) {
		return String.format("%08x %08x %08x", image.palette.redMask,image.palette.greenMask,image.palette.blueMask);
	}

}
