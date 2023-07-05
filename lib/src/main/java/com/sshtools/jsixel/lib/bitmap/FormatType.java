package com.sshtools.jsixel.lib.bitmap;

import com.sshtools.jsixel.lib.LibSixel;

public enum FormatType {
	COLOR, GRAYSCALE, PALETTE;
	
	int toFormatTypeCode() {
		switch(this) {
		case COLOR:
			return LibSixel.SIXEL_FORMATTYPE_COLOR;
		case GRAYSCALE:
			return LibSixel.SIXEL_FORMATTYPE_GRAYSCALE;
		case PALETTE:
			return LibSixel.SIXEL_FORMATTYPE_PALETTE;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
