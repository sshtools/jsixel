package com.sshtools.jsixel.awt;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.sshtools.jsixel.lib.bitmap.Bitmap;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.lib.util.ByteBufferBackedInputStream;
import com.sshtools.jsixel.lib.util.DataArrays;

public final class AWTImageBitmap implements Bitmap {

	public final static class BufferedImageBitmapBuilder
			extends BitmapBuilder<BufferedImageBitmapBuilder, AWTImageBitmap> {

		private Optional<BufferedImage> image = Optional.empty();
		private Optional<Bitmap> bitmap = Optional.empty();

		public BufferedImageBitmapBuilder fromImage(BufferedImage image) {
			this.image = Optional.of(image);
			return this;
		}

		public BufferedImageBitmapBuilder fromBitmap(Bitmap bitmap) {
			this.bitmap = Optional.of(bitmap);
			return this;
		}

		@Override
		public AWTImageBitmap build() {
			if (image.isPresent()) {
				return new AWTImageBitmap(image.get());
			} else {
				try {
					BufferedImage img;
					if(readable.isPresent()) {
						img = ImageIO.read(Channels.newInputStream(readable.get()));
					}
					else {
						if(bitmap.isPresent()) {
							var bm = bitmap.get();
							var t = calcType(bm);
							if(bm.formatType() == FormatType.PALETTE) {
								var cm = rgbPaletteToColorModel(bm.pixelFormat(), bm.palette());
								img = new BufferedImage(bm.width(), bm.height(), t, cm);
							}
							else if(bm.formatType() == FormatType.GRAYSCALE) {
									throw new UnsupportedOperationException();
							} else {
								img = new BufferedImage(bm.width(), bm.height(), t);
							}
							
							var raster = img.getRaster();
							var dataBuffer = raster.getDataBuffer();
							if (dataBuffer instanceof DataBufferByte) {
								raster.setDataElements(0, 0, bm.width(), bm.height(), bm.byteArray());
							} else if (dataBuffer instanceof DataBufferUShort) {
								raster.setDataElements(0, 0, bm.width(), bm.height(), DataArrays.toShortArray(bm.data().asShortBuffer()));
							} else if (dataBuffer instanceof DataBufferShort) {
								raster.setDataElements(0, 0, bm.width(), bm.height(), DataArrays.toShortArray(bm.data().asShortBuffer()));
							} else if (dataBuffer instanceof DataBufferInt) {
								raster.setDataElements(0, 0, bm.width(), bm.height(), DataArrays.toIntArray(bm.data().asIntBuffer()));
							} else {
								throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
							}
							
						}
						else if (data.isPresent()) {
							img = ImageIO.read(new ByteBufferBackedInputStream(data.get()));
						} else {
							throw new IllegalStateException("No image source supplied.");
						}
					}
					return new AWTImageBitmap(img);
				}
				catch(IOException ioe) {
					throw new UncheckedIOException(ioe);
				}
			}
		}
	}

	private final BufferedImage image;
	private final PixelFormat format;
	private final FormatType formatType;

	private AWTImageBitmap(BufferedImage image) {
		this.image = image;
		format = calcFormat(image.getType(), image.getColorModel().getPixelSize());
		formatType = calcType(image);
	}

	@Override
	public int bitsPerPixel() {
		return image.getColorModel().getPixelSize();
	}

	@Override
	public void write(ByteBuffer buffer, PixelFormat fmt, FormatType formatType) {
		var defaultFormat = pixelFormat();
		var decodableImage = this.image;
		if (!Objects.equals(fmt, defaultFormat)) {
			decodableImage = convertTo(decodableImage, calcType(fmt, formatType));
		}

		var dataBuffer = decodableImage.getRaster().getDataBuffer();
		if (dataBuffer instanceof DataBufferByte) {
			buffer.put(((DataBufferByte) dataBuffer).getData());
		} else if (dataBuffer instanceof DataBufferUShort) {
			buffer.asShortBuffer().put(((DataBufferUShort) dataBuffer).getData());
		} else if (dataBuffer instanceof DataBufferShort) {
			buffer.asShortBuffer().put(((DataBufferShort) dataBuffer).getData());
		} else if (dataBuffer instanceof DataBufferInt) {
			buffer.asIntBuffer().put(((DataBufferInt) dataBuffer).getData());
		} else {
			throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
		}
		buffer.flip();
	}

	@Override
	public FormatType formatType() {
		return formatType;
	}

	@Override
	public int height() {
		return image.getHeight();
	}

	@Override
	public byte[] palette() {
		var cm = image.getColorModel();
		if (cm instanceof IndexColorModel) {
			throw new UnsupportedOperationException("TODO");
		}
		return new byte[0];
	}

	@Override
	public PixelFormat pixelFormat() {
		return format;
	}

	@Override
	public int width() {
		return image.getWidth();
	}

	RenderedImage image() {
		return image;
	}

	private PixelFormat calcFormat(int type, int bpp) {
		switch (type) {
		case BufferedImage.TYPE_USHORT_555_RGB:
			return PixelFormat.RGB555;
		case BufferedImage.TYPE_USHORT_565_RGB:
			return PixelFormat.RGB565;
		case BufferedImage.TYPE_3BYTE_BGR:
			return PixelFormat.BGR888;
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			return PixelFormat.ABGR8888;
		case BufferedImage.TYPE_BYTE_INDEXED:
			switch (bpp) {
			case 1:
				return PixelFormat.PAL1;
			case 2:
				return PixelFormat.PAL2;
			case 4:
				return PixelFormat.PAL4;
			case 8:
				return PixelFormat.PAL8;
			default:
				throw new UnsupportedOperationException();
			}
		case BufferedImage.TYPE_BYTE_GRAY:
			switch (bpp) {
			case 8:
				return PixelFormat.G8;
			default:
				throw new UnsupportedOperationException();
			}
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE:
			return PixelFormat.ARGB8888;
		case BufferedImage.TYPE_INT_BGR:
			return PixelFormat.BGRA8888;
		case BufferedImage.TYPE_INT_RGB:
			return PixelFormat.RGBA8888;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public static IndexColorModel rgbPaletteToColorModel(PixelFormat pixelFormat, byte[] bs) {
		switch(pixelFormat) {
		case PAL8:
			return new IndexColorModel(8, bs.length / 3, bs, 0, false, 0);
		default:
			throw new UnsupportedOperationException("TODO");
		}
	}

	private static FormatType calcType(BufferedImage image) {
		var colorModel = image.getColorModel();
		if (colorModel instanceof IndexColorModel) {
			return FormatType.PALETTE;
		} else {
			var space = colorModel.getColorSpace();
			if (space.getType() == ColorSpace.CS_GRAY)
				return FormatType.GRAYSCALE;
			return FormatType.COLOR;
		}
	}

	private static int calcType(Bitmap fmt) {
		return calcType(fmt.pixelFormat(), fmt.formatType());
	}

	private static int calcType(PixelFormat pixelFormat, FormatType formatType) {
		switch(formatType) {
		case COLOR:
			switch(pixelFormat) {
			case ABGR8888:
				return BufferedImage.TYPE_4BYTE_ABGR;
			case ARGB8888:
				return BufferedImage.TYPE_INT_ARGB;
			case BGR888:
				return BufferedImage.TYPE_3BYTE_BGR;
			case RGB555:
				return BufferedImage.TYPE_USHORT_555_RGB;
			case RGB565:
				return BufferedImage.TYPE_USHORT_565_RGB;
			case RGBA8888:
				return BufferedImage.TYPE_INT_RGB;
			case BGRA8888:
				return BufferedImage.TYPE_INT_BGR;
			default:
				throw new UnsupportedOperationException();
			}
		case GRAYSCALE:
			switch(pixelFormat) {
			case G8:
				return BufferedImage.TYPE_BYTE_GRAY;
			default:
				throw new UnsupportedOperationException();
			}
		case PALETTE:
			switch(pixelFormat) {
			case PAL1:
			case PAL2:
			case PAL4:
			case PAL8:
				return BufferedImage.TYPE_BYTE_INDEXED;
			default:
				throw new UnsupportedOperationException();
			}
		default:
			throw new UnsupportedOperationException();
		}
	}

	private BufferedImage convertTo(BufferedImage sourceImage, int type) {
		BufferedImage img;
		if (sourceImage.getColorModel() instanceof IndexColorModel)
			img = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), type,
					(IndexColorModel) sourceImage.getColorModel());
		else
			img = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), type);
		img.getGraphics().drawImage(sourceImage, 0, 0, null);
		return img;
	}

}
