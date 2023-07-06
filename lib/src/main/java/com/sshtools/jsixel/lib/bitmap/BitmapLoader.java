package com.sshtools.jsixel.lib.bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.Bitmap.BitmapBuilder;

public interface BitmapLoader<BITMAP extends Bitmap, BUILDER extends BitmapBuilder<?, BITMAP>> {
	
	public enum ImageType {
		PNG, JPEG, BMP, GIF, OTHER
	}
	
	BUILDER builder();
	
	BITMAP load(Optional<ImageType> typeHint, InputStream imageData) throws IOException;
	
	void save(ImageType type, BITMAP bitmap, OutputStream imageData) throws IOException;
	
	
}
