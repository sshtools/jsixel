package com.sshtools.jsixel.slim;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

import com.ibasco.image.gif.GifFrame;
import com.ibasco.image.gif.GifImageReader;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;

public class GIFBitmap implements SlimBitmap {
	
	private final int width;
	private final int height;
	private final PixelFormat format;
	private final GifFrame[] data;
	private int frame = 0;
	private ByteBuffer composite;

	public GIFBitmap(InputStream input) throws IOException {
		try (var reader = new GifImageReader(input)) {
			width = reader.getMetadata().getWidth();
			height = reader.getMetadata().getHeight();
			switch (reader.getPixelFormat()) {
			case ARGB:
				format = PixelFormat.ARGB8888;
				break;
			case BGRA:
				format = PixelFormat.BGRA8888;
				break;
			default:
				throw new UnsupportedOperationException();
			}
			var l = new ArrayList<GifFrame>();
			while (reader.hasRemaining()) {
				var frame = reader.read();
				l.add(frame);
			}
			data = l.toArray(new GifFrame[0]);
		}
	}

	@Override
	public int bitsPerPixel() {
		return 32;
	}
	
	@Override
	public FormatType formatType() {
		return FormatType.COLOR;
	}

	@Override
	public boolean frame(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
		if (pixelFormat != pixelFormat() || formatType != formatType()) {
			throw new UnsupportedOperationException("Conversion not supported");
		}
		if(frame >= data.length) {
			frame = 0;
		}
		if(frame < data.length) {

			if(composite == null) {
				composite = ByteBuffer.allocateDirect(width * height * 4);
			}

			var ibuf = composite.asIntBuffer();
			var thisFrame = data[frame];
			if(frame == 0) {
				ibuf.put(thisFrame.getData());
			}
			else {
				var fbuf = thisFrame.getData();
				var frameHeight = thisFrame.getHeight();
				var frameWidth = thisFrame.getWidth();
				var frameTop = thisFrame.getTopPos();
				var frameLeft = thisFrame.getLeftPos();
				for(var y = 0 ; y < frameHeight; y++) {
					var offset = (( y  + frameTop ) * width) + frameLeft;
					for(var x = 0 ; x < frameWidth; x++) {
						ibuf.put(offset + x, blend(ibuf.get(offset + x), fbuf[(y * frameWidth) + x]));
					}
				}
			}
			
			buffer.put(composite);
			composite.flip();
			
			buffer.flip();
			frame++;
		}
		return hasMoreFrames();

	}

	@Override
	public boolean hasMoreFrames() {
		return frame < data.length;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public Optional<byte[]> palette() {
		return Optional.empty();
	}

	@Override
	public PixelFormat pixelFormat() {
		return format;
	}

	@Override
	public String toString() {
		return "GIFBitmap [bitsPerPixel()=" + bitsPerPixel() + ",width()=" + width() + ", height()=" + height() + ", pixelFormat()=" + pixelFormat()
				+ ", formatType()=" + formatType() + ", palette()=" + palette() + "]";
	}

	@Override
	public int width() {
		return width;
	}

	int blend(int rgba1, int rgba2) {
		if(format == PixelFormat.ARGB8888) {
			if((rgba2 & 0xff000000) == 0) 
				return rgba1;
			else
				return rgba2;
		}
		else {
			if((rgba2 & 0xff) == 0) 
				return rgba1;
			else
				return rgba2;
		}
	}

}
