package com.sshtools.jsixel;

import java.io.Closeable;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.sshtools.jsixel.lib.SixelLibrary;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Decoder implements Closeable {
	
	private final PointerByReference dec;
	private final Set<Memory> allocated = new HashSet<>();

	public Decoder() {
		dec = new PointerByReference();
		SixelLibrary.INSTANCE.sixel_decoder_new(dec, (PointerByReference)null);
	}
		
	public void opt(char flag) {
		SixelLibrary.INSTANCE.sixel_decoder_setopt(dec, flag, (Pointer)null);
	}
	
	public void opt(char flag, String arg) {
		SixelLibrary.INSTANCE.sixel_decoder_setopt(dec, flag, arg);
	}
	
	public void opt(char flag, int arg) {
		var memo = new Memory(4);
		allocated.add(memo);
		memo.setInt(0, arg);
		SixelLibrary.INSTANCE.sixel_decoder_setopt(dec, flag, memo);
	}
	
	public void opt(char flag, boolean arg) {
		var memo = new Memory(1);
		allocated.add(memo);
		memo.setInt(0, arg ? 1 : 0);
		SixelLibrary.INSTANCE.sixel_decoder_setopt(dec, flag, memo);
	}
	
	public void opt(char flag, long arg) {
		var memo = new Memory(8);
		allocated.add(memo);
		memo.setLong(0, arg);
		SixelLibrary.INSTANCE.sixel_decoder_setopt(dec, flag, memo);
	}
	
	public void decode(File file) {
		decode(file.getAbsolutePath());
	}
	
	public void decode(String file) {
		opt(SixelLibrary.SIXEL_OPTFLAG_INPUT, file);
		SixelLibrary.INSTANCE.sixel_decoder_decode(dec);
	}
	
	@Override
	public void close() {
		allocated.forEach(Memory::close);
		SixelLibrary.INSTANCE.sixel_decoder_unref(dec);
	}

}
