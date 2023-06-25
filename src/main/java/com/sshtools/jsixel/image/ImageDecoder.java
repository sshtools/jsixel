package com.sshtools.jsixel.image;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ImageDecoder {
	
	ColorType colorType();

	int width();

	int height();

    default void decode(ByteBuffer buffer) throws IOException {
    	decode(buffer, format());
    }

    void decode(ByteBuffer buffer, Format fmt) throws IOException;

	int bytesPerPixel();

	Format format();

	byte[] palette();
}
