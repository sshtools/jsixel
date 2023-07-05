package com.sshtools.jsixel.lib;

import java.io.IOException;
import java.io.UncheckedIOException;

public class LibSixelExtensions {

	public static void throwIfFailed(int status) {
		if (SIXEL_FAILED(status)) {
			var errPointer = LibSixel.INSTANCE.sixel_helper_format_error(status);
			var messagePointer = LibSixel.INSTANCE.sixel_helper_get_additional_message();
			var formattedError = errPointer.getString(0);
			var additionalMessage = messagePointer.getString(0);
			throw new UncheckedIOException(new IOException(String.format("%s. %s", formattedError, additionalMessage)));
		}
	}

	public static boolean SIXEL_FAILED(int status) {
		return (status & 0x1000) != 0;
	}
}
