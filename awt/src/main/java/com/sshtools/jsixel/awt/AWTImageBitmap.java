package com.sshtools.jsixel.awt;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadataNode;

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

		@SuppressWarnings("resource")
		@Override
		public AWTImageBitmap build() {
			if (image.isPresent()) {
				return new AWTImageBitmap(new ImageFrame(image.get()));
			} else {
				try {
					ImageFrame[] img;
					if (readable.isPresent()) {
						img = readFrames(Channels.newInputStream(readable.get()));
					} else {
						if (bitmap.isPresent()) {
							var bm = bitmap.get();
							var t = calcType(bm);
							BufferedImage bim;
							if (bm.formatType() == FormatType.PALETTE) {
								var cm = rgbPaletteToColorModel(bm.pixelFormat(), bm.palette()
										.orElseThrow(() -> new IllegalStateException("Bitmap has no palette.")));
								bim = new BufferedImage(bm.width(), bm.height(), t, cm);
							} else if (bm.formatType() == FormatType.GRAYSCALE) {
								throw new UnsupportedOperationException();
							} else {
								bim = new BufferedImage(bm.width(), bm.height(), t);
							}

							var raster = bim.getRaster();
							var dataBuffer = raster.getDataBuffer();
							if (dataBuffer instanceof DataBufferByte) {
								raster.setDataElements(0, 0, bm.width(), bm.height(), bm.byteArray());
							} else if (dataBuffer instanceof DataBufferUShort) {
								raster.setDataElements(0, 0, bm.width(), bm.height(),
										DataArrays.toShortArray(bm.data().asShortBuffer()));
							} else if (dataBuffer instanceof DataBufferShort) {
								raster.setDataElements(0, 0, bm.width(), bm.height(),
										DataArrays.toShortArray(bm.data().asShortBuffer()));
							} else if (dataBuffer instanceof DataBufferInt) {
								raster.setDataElements(0, 0, bm.width(), bm.height(),
										DataArrays.toIntArray(bm.data().asIntBuffer()));
							} else {
								throw new IllegalArgumentException(
										"Not implemented for data buffer type: " + dataBuffer.getClass());
							}

							img = new ImageFrame[] { new ImageFrame(bim) };

						} else if (data.isPresent()) {
							img = readFrames(new ByteBufferBackedInputStream(data.get()));
						} else {
							throw new IllegalStateException("No image source supplied.");
						}
					}
					return new AWTImageBitmap(img);
				} catch (IOException ioe) {
					throw new UncheckedIOException(ioe);
				}
			}
		}

		public BufferedImageBitmapBuilder fromBitmap(Bitmap bitmap) {
			this.bitmap = Optional.of(bitmap);
			return this;
		}

		public BufferedImageBitmapBuilder fromImage(BufferedImage image) {
			this.image = Optional.of(image);
			return this;
		}
	}

	private static class ImageFrame {
		private final int delay;
		private final BufferedImage image;
		private final String disposal;
		private final int width, height;

		private ImageFrame(BufferedImage image) {
			this.image = image;
			this.delay = -1;
			this.disposal = null;
			this.width = image.getWidth();
			this.height = image.getHeight();
		}

		private ImageFrame(BufferedImage image, int delay, String disposal, int width, int height) {
			this.image = image;
			this.delay = delay;
			this.disposal = disposal;
			this.width = width;
			this.height = height;
		}
	}

	public static IndexColorModel rgbPaletteToColorModel(PixelFormat pixelFormat, byte[] bs) {
		switch (pixelFormat) {
		case PAL8:
			return new IndexColorModel(8, bs.length / 3, bs, 0, false, 0);
		default:
			throw new UnsupportedOperationException("TODO");
		}
	}

	private static int calcType(Bitmap fmt) {
		return calcType(fmt.pixelFormat(), fmt.formatType());
	}

	private static FormatType calcType(BufferedImage image) {
		var colorModel = image.getColorModel();
		if (colorModel instanceof IndexColorModel) {
			return FormatType.PALETTE;
		} else {
			var space = colorModel.getColorSpace();
			if (space.getType() == ColorSpace.TYPE_GRAY)
				return FormatType.GRAYSCALE;
			return FormatType.COLOR;
		}
	}

	private static int calcType(PixelFormat pixelFormat, FormatType formatType) {
		switch (formatType) {
		case COLOR:
			switch (pixelFormat) {
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
			switch (pixelFormat) {
			case G8:
				return BufferedImage.TYPE_BYTE_GRAY;
			default:
				throw new UnsupportedOperationException();
			}
		case PALETTE:
			switch (pixelFormat) {
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

	private static ImageFrame[] readFrames(InputStream stream) throws IOException {
		var frames = new ArrayList<ImageFrame>(2);

		var iis = ImageIO.createImageInputStream(stream);
		var reader = ImageIO.getImageReaders(iis).next();
		reader.setInput(iis);

		int lastx = 0;
		int lasty = 0;

		int width = -1;
		int height = -1;

		var metadata = reader.getStreamMetadata();

		Color backgroundColor = null;

		if (metadata != null && metadata.getClass().getSimpleName().startsWith("GIF")) {
			var globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

			var globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
			var globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

			if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0) {
				var screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

				if (screenDescriptor != null) {
					width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
					height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
				}
			}

			if (globalColorTable != null && globalColorTable.getLength() > 0) {
				var colorTable = (IIOMetadataNode) globalColorTable.item(0);

				if (colorTable != null) {
					String bgIndex = colorTable.getAttribute("backgroundColorIndex");

					IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
					while (colorEntry != null) {
						if (colorEntry.getAttribute("index").equals(bgIndex)) {
							int red = Integer.parseInt(colorEntry.getAttribute("red"));
							int green = Integer.parseInt(colorEntry.getAttribute("green"));
							int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

							backgroundColor = new Color(red, green, blue);
							break;
						}

						colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
					}
				}
			}

			BufferedImage master = null;
			boolean hasBackround = false;

			for (int frameIndex = 0;; frameIndex++) {
				BufferedImage image;
				try {
					image = reader.read(frameIndex);
				} catch (IndexOutOfBoundsException io) {
					break;
				}

				if (width == -1 || height == -1) {
					width = image.getWidth();
					height = image.getHeight();
				}

				var root = (IIOMetadataNode) reader.getImageMetadata(frameIndex)
						.getAsTree("javax_imageio_gif_image_1.0");
				var gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
				var children = root.getChildNodes();

				var delay = Integer.valueOf(gce.getAttribute("delayTime"));

				var disposal = gce.getAttribute("disposalMethod");

				if (master == null) {
					master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					master.createGraphics().setColor(backgroundColor);
					master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

					hasBackround = image.getWidth() == width && image.getHeight() == height;

					master.createGraphics().drawImage(image, 0, 0, null);
				} else {
					int x = 0;
					int y = 0;

					for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
						var nodeItem = children.item(nodeIndex);

						if (nodeItem.getNodeName().equals("ImageDescriptor")) {
							var map = nodeItem.getAttributes();

							x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
							y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
						}
					}

					if (disposal.equals("restoreToPrevious")) {
						BufferedImage from = null;
						for (int i = frameIndex - 1; i >= 0; i--) {
							if (!frames.get(i).disposal.equals("restoreToPrevious") || frameIndex == 0) {
								from = frames.get(i).image;
								break;
							}
						}

						{
							var model = from.getColorModel();
							var alpha = from.isAlphaPremultiplied();
							var raster = from.copyData(null);
							master = new BufferedImage(model, raster, alpha, null);
						}
					} else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null) {
						if (!hasBackround || frameIndex > 1) {
							master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).width,
									frames.get(frameIndex - 1).height);
						}
					}
					master.createGraphics().drawImage(image, x, y, null);

					lastx = x;
					lasty = y;
				}

				{
					BufferedImage copy;

					{
						var model = master.getColorModel();
						var alpha = master.isAlphaPremultiplied();
						var raster = master.copyData(null);
						copy = new BufferedImage(model, raster, alpha, null);
					}
					frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
				}

				master.flush();
			}
		}
		else {
			frames.add(new ImageFrame(reader.read(0)));
		}
		reader.dispose();

		return frames.toArray(new ImageFrame[frames.size()]);
	}

	private final ImageFrame[] images;
	private final PixelFormat format;
	private final FormatType formatType;
	private final Optional<byte[]> palette;

	private int frame;

	private AWTImageBitmap(ImageFrame... images) {

		PixelFormat format = null;
		for (int i = 0; i < images.length; i++) {
			var image = images[i];
			var img = image.image;
			try {
				var fmt = calcFormat(img.getType(), img.getColorModel().getPixelSize());
				if (format == null)
					format = fmt;
			} catch (UnsupportedOperationException uoe) {
				var closest = BufferedImage.TYPE_INT_ARGB;
				switch (img.getType()) {
				case BufferedImage.TYPE_USHORT_GRAY:
					closest = BufferedImage.TYPE_BYTE_GRAY;
					break;
				}
				images[i] = new ImageFrame(convertTo(img, closest), images[i].delay, images[i].disposal,
						images[i].width, images[i].height);
				var fmt = calcFormat(closest, images[i].image.getColorModel().getPixelSize());
				if (format == null)
					format = fmt;
			}
		}

		this.format = format;
		this.images = images;

		formatType = calcType(images[0].image);
		palette = calcPalette(images[0].image);
	}

	@Override
	public long delay() {
		return frame < images.length && images[frame].delay > -1 ? images[frame].delay * 10 : 0;
	}

	@Override
	public FormatType formatType() {
		return formatType;
	}

	@Override
	public boolean frame(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
		if (frame >= images.length) {
			frame = 0;
		}
		if (frame < images.length) {
			writeFrame(images[frame], buffer, pixelFormat, formatType);
			frame++;
		}
		return hasMoreFrames();

	}

	@Override
	public boolean hasMoreFrames() {
		return frame < images.length;
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
	public String toString() {
		return "AWTImageBitmap [bitsPerPixel()=" + bitsPerPixel() + ",width()=" + width() + ", height()=" + height()
				+ ", pixelFormat()=" + pixelFormat() + ", formatType()=" + formatType() + ", palette()=" + palette()
				+ "]";
	}

	@Override
	public int width() {
		return images[0].width;
	}

	RenderedImage image() {
		/* TODO saving animated GIFs? */
		return images[0].image;
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
			case 1:
				return PixelFormat.G1;
			case 2:
				return PixelFormat.G2;
			case 4:
				return PixelFormat.G4;
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
			throw new UnsupportedOperationException(
					MessageFormat.format("BufferedImage type {0} ({1} bpp) not supported.", type, bpp));
		}
	}

	private Optional<byte[]> calcPalette(BufferedImage image) {
		var cm = image.getColorModel();
		if (cm instanceof IndexColorModel) {
			var cmIndexed = (IndexColorModel) cm;
			var pal = new int[cmIndexed.getMapSize()];
			cmIndexed.getRGBs(pal);
			var bytePal = new byte[pal.length * 3];
			for (int i = 0; i < pal.length; i++) {
				bytePal[i * 3] = (byte) (pal[i] >> 16 & 0xff);
				bytePal[(i * 3) + 1] = (byte) (pal[i] >> 8 & 0xff);
				bytePal[(i * 3) + 2] = (byte) (pal[i] & 0xff);
			}
			return Optional.of(bytePal);
		} else
			return Optional.empty();
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

	private boolean writeFrame(ImageFrame frame, ByteBuffer buffer, PixelFormat fmt, FormatType formatType) {
		var defaultFormat = pixelFormat();
		var defaultFormatType = formatType();
		var decodableImage = frame.image;
		if (!Objects.equals(fmt, defaultFormat) || !Objects.equals(formatType, defaultFormatType)) {
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
		return false;
	}
}
