package com.sshtools.jsixel.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.sshtools.jsixel.javafx.JavaFXImageBitmap.JavaFXImageBitmapBuilder;
import com.sshtools.jsixel.lib.bitmap.BitmapLoader;

public class JavaFXImageLoader implements BitmapLoader<JavaFXImageBitmap, JavaFXImageBitmapBuilder> {
 
	@Override
	public JavaFXImageBitmap load(Optional<ImageType>  typeHint, InputStream input) throws IOException {
		return builder().
				fromStream(input).
				build();
	}

	@Override
	public JavaFXImageBitmapBuilder builder() {
		return new JavaFXImageBitmapBuilder();
	}

	@Override
	public void save(ImageType type, JavaFXImageBitmap bitmap, OutputStream output) throws IOException {
		throw new UnsupportedOperationException();
	}
}
