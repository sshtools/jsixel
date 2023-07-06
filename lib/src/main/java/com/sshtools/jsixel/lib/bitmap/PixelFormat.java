package com.sshtools.jsixel.lib.bitmap;

import com.sshtools.jsixel.lib.LibSixel;

public enum PixelFormat {
	RGB555, RGB565, RGB888, BGR555, BGR565, BGR888, ARGB8888, RGBA8888, ABGR8888, BGRA8888, 
	G1, G2, G4, G8, AG88, GA88, PAL1, PAL2, PAL4, PAL8;

	public int code() {
		switch(this) {
		case AG88:
			return LibSixel.SIXEL_PIXELFORMAT_AG88;
		case GA88:
			return LibSixel.SIXEL_PIXELFORMAT_GA88;
		case RGB555:
			return LibSixel.SIXEL_PIXELFORMAT_RGB555;
		case RGB565:
			return LibSixel.SIXEL_PIXELFORMAT_RGB565;
		case RGB888:
			return LibSixel.SIXEL_PIXELFORMAT_RGB888;
		case BGR555:
			return LibSixel.SIXEL_PIXELFORMAT_BGR555;
		case BGR565:
			return LibSixel.SIXEL_PIXELFORMAT_BGR565;
		case BGR888:
			return LibSixel.SIXEL_PIXELFORMAT_BGR888;
		case ARGB8888:
			return LibSixel.SIXEL_PIXELFORMAT_ARGB8888;
		case RGBA8888:
			return LibSixel.SIXEL_PIXELFORMAT_RGBA8888;
		case ABGR8888:
			return LibSixel.SIXEL_PIXELFORMAT_ABGR8888;
		case BGRA8888:
			return LibSixel.SIXEL_PIXELFORMAT_BGRA8888;
		case G1:
			return LibSixel.SIXEL_PIXELFORMAT_G1;
		case G2:
			return LibSixel.SIXEL_PIXELFORMAT_G2;
		case G4:
			return LibSixel.SIXEL_PIXELFORMAT_G4;
		case G8:
			return LibSixel.SIXEL_PIXELFORMAT_G8;
		case PAL1:
			return LibSixel.SIXEL_PIXELFORMAT_PAL1;
		case PAL2:
			return LibSixel.SIXEL_PIXELFORMAT_PAL2;
		case PAL4:
			return LibSixel.SIXEL_PIXELFORMAT_PAL4;
		case PAL8:
			return LibSixel.SIXEL_PIXELFORMAT_PAL8;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public int bpp() {
		switch(this) {
		case RGB555:
		case RGB565:
		case BGR555:
		case BGR565:
			return 16;
		case RGB888:
		case BGR888:
			return 24;
		case ARGB8888:
		case RGBA8888:
		case ABGR8888:
		case BGRA8888:
			return 32;
		case G1:
			return 1;
		case G2:
			return 2;
		case G4:
			return 4;
		case G8:
			return 8;
		case PAL1:
			return 1;
		case PAL2:
			return 2;
		case PAL4:
			return 4;
		case PAL8:
			return 8;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public boolean indexed() {
		switch(this) {
		case PAL1:
		case PAL2:
		case PAL4:
		case PAL8:
			return true;
		default:
			return false;
		}
	}

}
