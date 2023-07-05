package com.sshtools.jsixel.lib.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class DataArrays {

	public static byte[] toByteArray(ByteBuffer buf) {
		buf = buf.asReadOnlyBuffer();
		var arr = new byte[buf.remaining()];
		buf.get(arr);
		return arr;
	}

	public static short[] toShortArray(ShortBuffer buf) {
		buf = buf.asReadOnlyBuffer();
		var arr = new short[buf.remaining()];
		buf.get(arr);
		return arr;
	}

	public static short[] toShortArray(ByteBuffer buf) {
		return toShortArray(buf.asShortBuffer());
	}

	public static int[] toIntArray(IntBuffer buf) {
		buf = buf.asReadOnlyBuffer();
		var arr = new int[buf.remaining()];
		buf.get(arr);
		return arr;
	}

	public static int[] toIntArray(ByteBuffer buf) {
		return toIntArray(buf.asIntBuffer());
	}

}
