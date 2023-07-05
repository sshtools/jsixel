package com.sshtools.jsixel.lib.bitmap;

import com.sshtools.jsixel.lib.LibSixel;

public enum Diffuse {
	AUTO, NONE, ATKINSON, FS, JAJUNI, STUCKI, BURKES, A_DITHER, X_DITHER;

	public int code() {
		switch (this) {
		case NONE:
			return LibSixel.SIXEL_DIFFUSE_NONE;
		case AUTO:
			return LibSixel.SIXEL_DIFFUSE_AUTO;
		case ATKINSON:
			return LibSixel.SIXEL_DIFFUSE_ATKINSON;
		case FS:
			return LibSixel.SIXEL_DIFFUSE_FS;
		case JAJUNI:
			return LibSixel.SIXEL_DIFFUSE_JAJUNI;
		case STUCKI:
			return LibSixel.SIXEL_DIFFUSE_STUCKI;
		case BURKES:
			return LibSixel.SIXEL_DIFFUSE_BURKES;
		case A_DITHER:
			return LibSixel.SIXEL_DIFFUSE_A_DITHER;
		case X_DITHER:
			return LibSixel.SIXEL_DIFFUSE_X_DITHER;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
