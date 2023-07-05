package com.sshtools.jsixel.awt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.sshtools.jsixel.awt.AWTImageBitmap.BufferedImageBitmapBuilder;
import com.sshtools.jsixel.lib.bitmap.BitmapCodec;

public class AWTImageCodec implements BitmapCodec<AWTImageBitmap, BufferedImageBitmapBuilder> {
 
	@Override
	public AWTImageBitmap load(Optional<ImageType>  typeHint, InputStream input) throws IOException {
		return builder().
				fromStream(input).
				build();
	}

	@Override
	public BufferedImageBitmapBuilder builder() {
		return new BufferedImageBitmapBuilder();
	}

	@Override
	public void save(ImageType type, AWTImageBitmap bitmap, OutputStream output) throws IOException {
		ImageIO.write(bitmap.image(), type.name(), output);
	}
}
