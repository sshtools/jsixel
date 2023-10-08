package com.sshtools.jsixel.swt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;

import com.sshtools.jsixel.lib.bitmap.BitmapLoader;
import com.sshtools.jsixel.swt.SWTImageBitmap.SWTImageBitmapBuilder;

public class SWTImageLoader implements BitmapLoader<SWTImageBitmap, SWTImageBitmapBuilder> {

	@Override
	public SWTImageBitmap load(Optional<ImageType> typeHint, InputStream input) throws IOException {
		return builder().fromStream(input).build();
	}

	@Override
	public SWTImageBitmapBuilder builder() {
		return new SWTImageBitmapBuilder();
	}

	@Override
	public void save(ImageType type, SWTImageBitmap bitmap, OutputStream output) throws IOException {
		var iloader = new ImageLoader();
		iloader.data = bitmap.images();
		switch (type) {
		case BMP:
			iloader.save(output, SWT.IMAGE_BMP);
			break;
		case GIF:
			iloader.save(output, SWT.IMAGE_GIF);
			break;
		case JPEG:
			iloader.save(output, SWT.IMAGE_JPEG);
			break;
		case PNG:
			iloader.save(output, SWT.IMAGE_PNG);
			break;
		case ICO:
			iloader.save(output, SWT.IMAGE_ICO);
			break;
		case TIFF:
			iloader.save(output, SWT.IMAGE_TIFF);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
