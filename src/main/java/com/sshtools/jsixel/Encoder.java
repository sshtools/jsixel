package com.sshtools.jsixel;

import java.io.Closeable;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import com.sshtools.jsixel.lib.SixelLibrary;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Encoder implements Closeable {
	
	private final PointerByReference enc;
	private final Set<Memory> allocated = new HashSet<>();

	public Encoder() {
		enc = new PointerByReference();
		SixelLibrary.INSTANCE.sixel_encoder_new(enc, (PointerByReference)null);
	}
		
	public void opt(char flag) {
		SixelLibrary.INSTANCE.sixel_encoder_setopt(enc, flag, (Pointer)null);
	}
	
	public void opt(char flag, String arg) {
		SixelLibrary.INSTANCE.sixel_encoder_setopt(enc, flag, arg);
	}
	
	public void opt(char flag, int arg) {
		var memo = new Memory(4);
		allocated.add(memo);
		memo.setInt(0, arg);
		SixelLibrary.INSTANCE.sixel_encoder_setopt(enc, flag, memo);
	}
	
	public void opt(char flag, boolean arg) {
		var memo = new Memory(1);
		allocated.add(memo);
		memo.setInt(0, arg ? 1 : 0);
		SixelLibrary.INSTANCE.sixel_encoder_setopt(enc, flag, memo);
	}
	
	public void opt(char flag, long arg) {
		var memo = new Memory(8);
		allocated.add(memo);
		memo.setLong(0, arg);
		SixelLibrary.INSTANCE.sixel_encoder_setopt(enc, flag, memo);
	}
	
	public void encode(File file) {
		encode(file.getAbsolutePath());
	}
	
	public void encode(String file) {
		SixelLibrary.INSTANCE.sixel_encoder_encode(enc, file);
	}
	
	public void encodeBytes(ByteBuffer buf, int width, int height, int pixelFormat, byte[] palette, int ncolors) {
		SixelLibrary.INSTANCE.sixel_encoder_encode_bytes(enc, buf, width, height, pixelFormat, palette == null ? null : ByteBuffer.wrap(palette), ncolors);
	}

	@Override
	public void close() {
		allocated.forEach(Memory::close);
		SixelLibrary.INSTANCE.sixel_encoder_unref(enc);
	}

}
