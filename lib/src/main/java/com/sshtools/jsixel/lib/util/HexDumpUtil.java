package com.sshtools.jsixel.lib.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public final class HexDumpUtil {
	public static String formatHexDump(ByteBuffer buffer) {
		return formatHexDump(DataArrays.toByteArray(buffer));
	}

	public static String formatHexDump(byte[] array) {
		return formatHexDump(array, 0, array.length);
	}
	
	public static String formatHexDump(byte[] array, int offset, int length) {
		final int width = 16;

		StringBuilder builder = new StringBuilder();

		for (int rowOffset = offset; rowOffset < offset + length; rowOffset += width) {
			builder.append(String.format("%06d:  ", rowOffset));

			for (int index = 0; index < width; index++) {
				if (rowOffset + index < array.length) {
					builder.append(String.format("%02x ", array[rowOffset + index]));
				} else {
					builder.append("   ");
				}
			}

			if (rowOffset < array.length) {
				int asciiWidth = Math.min(width, array.length - rowOffset);
				builder.append("  |  ");
				try {
					builder.append(new String(array, rowOffset, asciiWidth, "UTF-8").replaceAll("\r\n", " ")
							.replaceAll("\n", " "));
				} catch (UnsupportedEncodingException ignored) {
					// If UTF-8 isn't available as an encoding then what can we do?!
				}
			}

			builder.append(String.format("%n"));
		}

		return builder.toString();
	}
}