package com.sshtools.jsixel.lib.bitmap;

import com.sshtools.jsixel.lib.LibSixel;

public enum BuiltInPalette {
	MONO_DARK, MONO_LIGHT, XTERM16, XTERM256, VT340_MONO, VT340_COLOR, G1, G2, G4, G8;

	public int code() {
		switch (this) {
		case MONO_DARK:
			return LibSixel.SIXEL_BUILTIN_MONO_DARK;
		case MONO_LIGHT:
			return LibSixel.SIXEL_BUILTIN_MONO_LIGHT;
		case XTERM16:
			return LibSixel.SIXEL_BUILTIN_XTERM16;
		case XTERM256:
			return LibSixel.SIXEL_BUILTIN_XTERM256;
		case VT340_MONO:
			return LibSixel.SIXEL_BUILTIN_VT340_MONO;
		case VT340_COLOR:
			return LibSixel.SIXEL_BUILTIN_VT340_COLOR;
		case G1:
			return LibSixel.SIXEL_BUILTIN_G1;
		case G2:
			return LibSixel.SIXEL_BUILTIN_G2;
		case G4:
			return LibSixel.SIXEL_BUILTIN_G4;
		case G8:
			return LibSixel.SIXEL_BUILTIN_G8;
		default:
			throw new UnsupportedOperationException();
		}
	}

}
