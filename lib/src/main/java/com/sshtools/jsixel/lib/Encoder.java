package com.sshtools.jsixel.lib;

import static com.sshtools.jsixel.lib.LibSixelExtensions.throwIfFailed;

import java.io.Closeable;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Encoder implements Closeable {
	
	private final PointerByReference enc;

	public Encoder() {
		enc =  LibSixel.INSTANCE.sixel_encoder_create();
	}
		
	public void opt(char flag) {
		throwIfFailed(LibSixel.INSTANCE.sixel_encoder_setopt(enc, flag, (Pointer)null));
	}
	
	public void opt(char flag, String arg) {
		throwIfFailed(LibSixel.INSTANCE.sixel_encoder_setopt(enc, flag, arg));
	}
	
	public void opt(char flag, long arg) {
		opt(flag, String.valueOf(arg));
	}
	
	public void opt(char flag, boolean arg) {
		opt(flag, String.valueOf(arg));
	}

	public void encode() {
		encode((String)null); 
	}
	
	public void encode(File file) {
		encode(file.getAbsolutePath());
	}
	
	public void encode(Path path) {
		/* TODO: this will only support local paths */
		encode(path.toString());
	}
	
	public void encode(String file) {
		throwIfFailed(LibSixel.INSTANCE.sixel_encoder_encode(enc, file));
	}
	
	public void encodeBytes(ByteBuffer buf, int width, int height, int pixelFormat, byte[] palette, int ncolors) {
		throwIfFailed(LibSixel.INSTANCE.sixel_encoder_encode_bytes(enc, buf, width, height, pixelFormat, palette == null ? null : ByteBuffer.wrap(palette), ncolors));
	}

	@Override
	public void close() {
		LibSixel.INSTANCE.sixel_encoder_unref(enc);
	}
	
	public static void main(String[] args) {
		try(var enc = new Encoder()) {
			enc.encode("src/main/resources/com/sshtools/jsixel/jsixel.png");
		}
	}

}
