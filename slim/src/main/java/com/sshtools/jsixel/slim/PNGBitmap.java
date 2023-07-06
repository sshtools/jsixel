/*
 * Copyright (c) 2008-2011, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sshtools.jsixel.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.slim.PNGDecoder.Format;

/**
 *
 * @author Matthias Mann
 */
public class PNGBitmap implements SlimBitmap {
    
    private PNGDecoder decoder;
    
    PNGBitmap(InputStream inputStream) throws IOException {
    	
        decoder = new PNGDecoder(inputStream);
    }

	@Override
	public void write(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
        try {
        	var pngFormat = decideFormat(pixelFormat, formatType);
			decoder.decode(buffer, width()*pngFormat.getNumComponents(), pngFormat);
			buffer.flip();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public FormatType formatType() {

    	switch(decoder.colorType) {
    	case PNGDecoder.COLOR_GREYALPHA:
    	case PNGDecoder.COLOR_GREYSCALE:
    		return FormatType.GRAYSCALE;
    	case PNGDecoder.COLOR_INDEXED:
    		return FormatType.PALETTE;
    	case PNGDecoder.COLOR_TRUEALPHA:
    	case PNGDecoder.COLOR_TRUECOLOR:
    		return FormatType.COLOR;
    	default:
    		throw new UnsupportedOperationException();
    	}
	}

	@Override
	public int width() {
		return decoder.getWidth();
	}

	@Override
	public int height() {
		return decoder.getHeight();
	}

	@Override
	public int bitsPerPixel() {
    	switch (decoder.colorType) {
		case PNGDecoder.COLOR_GREYSCALE:
			return 8;
		case PNGDecoder.COLOR_TRUECOLOR:
			return 24;
		case PNGDecoder.COLOR_TRUEALPHA:
			return 32;
		case PNGDecoder.COLOR_INDEXED:
			return 8;
		default:
			throw new IllegalStateException("unsupported color format");
		}
	}

	@Override
	public PixelFormat pixelFormat() {
        switch (decoder.colorType) {
        case PNGDecoder.COLOR_TRUECOLOR:
            return PixelFormat.RGBA8888;
        case PNGDecoder.COLOR_TRUEALPHA:
            return PixelFormat.RGBA8888;
        case PNGDecoder.COLOR_GREYSCALE:
            return PixelFormat.G8;
        case PNGDecoder.COLOR_GREYALPHA:
            return PixelFormat.G1;
        case PNGDecoder.COLOR_INDEXED:
            return PixelFormat.PAL8;
        default:
            throw new UnsupportedOperationException("Not yet implemented");
        }
	}

	@Override
	public Optional<byte[]> palette() {
		return Optional.ofNullable(decoder.palette);
	}

	private Format decideFormat(PixelFormat pixelFormat, FormatType formatType) {
		switch(formatType) {
		case COLOR:
			switch(pixelFormat) {
			case ABGR8888:
				return Format.ABGR;
			case BGRA8888:
				return Format.BGRA;
			case RGBA8888:
				return Format.RGBA;
			case RGB888:
				return Format.RGB;
			default:
				break;
			}
			break;
		default:
			break;
		}
		throw new UnsupportedOperationException(pixelFormat + " / " + formatType);
	}
}
