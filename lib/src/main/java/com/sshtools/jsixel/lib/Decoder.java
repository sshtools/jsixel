package com.sshtools.jsixel.lib;

import static com.sshtools.jsixel.lib.LibSixelExtensions.throwIfFailed;

import java.io.Closeable;
import java.io.File;
import java.nio.file.Path;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Decoder implements Closeable {
	
	private final PointerByReference dec;

	public Decoder() {
		dec = LibSixel.INSTANCE.sixel_decoder_create();
	}
		
	public void opt(char flag) {
		throwIfFailed(LibSixel.INSTANCE.sixel_decoder_setopt(dec, flag, (Pointer)null));
	}
	
	public void opt(char flag, String arg) {
		throwIfFailed(LibSixel.INSTANCE.sixel_decoder_setopt(dec, flag, arg));
	}
	
	public void opt(char flag, long arg) {
		opt(flag, String.valueOf(arg));
	}
	
	public void opt(char flag, boolean arg) {
		opt(flag, String.valueOf(arg));
	}
	
	public void decode(File file) {
		decode(file.getAbsolutePath());
	}
	
	public void decode(Path file) {
		decode(file.toString());
	}
	
	public void decode(String file) {
		opt(LibSixel.SIXEL_OPTFLAG_INPUT, file);
		decode();
	}

	public void decode() {
		throwIfFailed(LibSixel.INSTANCE.sixel_decoder_decode(dec));
	}
	
	@Override
	public void close() {
		LibSixel.INSTANCE.sixel_decoder_unref(dec);
	}

}
