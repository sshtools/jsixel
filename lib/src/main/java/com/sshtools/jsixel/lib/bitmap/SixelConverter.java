package com.sshtools.jsixel.lib.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

public interface SixelConverter {

	public static BitmapLoader<?, ?> defaultCodec() {
		return ServiceLoader.load(BitmapLoader.class).findFirst()
				.orElseThrow(() -> new UnsupportedOperationException(
						"No image codecs installed. Check you have one of the jsixel image codec modules available on the classpath."));
	}

	default byte[] toByteArray() {
		try (var out = new ByteArrayOutputStream()) {
			write(Channels.newChannel(out));
			return out.toByteArray();
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
	default ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(toByteArray());
	}

	default void write() {
		write(System.out);
	}

	default void write(File file) throws IOException {
		write(file.toPath());
	}

	default void write(Path file) throws IOException {
		try(var out = Files.newOutputStream(file)) {
			write(out);
		}
	}

	default void write(OutputStream out) {
		write(Channels.newChannel(out));
	}
	
	Bitmap bitmap();

	void write(WritableByteChannel writable);

	default void write(StringBuilder stringBuffer) {
		throw new UnsupportedOperationException();
	}
}
