package com.sshtools.jsixel.lib.bitmap;

import com.sshtools.jsixel.lib.LibSixel;

public enum Quality {
	AUTO, HIGH, LOW, FULL, HIGHCOLOR;

	public int code() {
		switch (this) {
		case AUTO:
			return LibSixel.SIXEL_QUALITY_AUTO;
		case HIGH:
			return LibSixel.SIXEL_QUALITY_HIGH;
		case LOW:
			return LibSixel.SIXEL_QUALITY_LOW;
		case FULL:
			return LibSixel.SIXEL_QUALITY_FULL;
		case HIGHCOLOR:
			return LibSixel.SIXEL_QUALITY_HIGHCOLOR;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
