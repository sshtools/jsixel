package com.sshtools.jsixel.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.BitmapLoader;
import com.sshtools.jsixel.slim.SlimBitmap.SlimBitmapBuilder;

public class SlimCodec implements BitmapLoader<SlimBitmap, SlimBitmapBuilder> {

	@Override
	public SlimBitmap load(Optional<ImageType> typeHint, InputStream input) throws IOException {
		return builder().fromStream(input).withTypeHint(typeHint).build();
	}

	@Override
	public void save(ImageType type, SlimBitmap bitmap, OutputStream output) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SlimBitmapBuilder builder() {
		return new SlimBitmapBuilder();
	}
}
