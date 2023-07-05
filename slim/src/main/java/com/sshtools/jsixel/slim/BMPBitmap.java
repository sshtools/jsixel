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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;


/**
 *
 * @author Matthias Mann
 */
public class BMPBitmap implements SlimBitmap {

    private static final int MIN_HDR_SIZE = 14+12;
    
    private static int align4(int x) {
        return (x + 3) & ~3;
    }
    private final byte[] header;
    private final int bpp;
	private final InputStream inputStream;
	private final int width;
	private final int height;
    private final PixelFormat format;
    private int startPos;
	private boolean flipY;

    BMPBitmap(InputStream inputStream) throws IOException {
    	this.inputStream = inputStream;
        header = new byte[14+124];
        readFully(header, 0, MIN_HDR_SIZE);

        if(hdrByte(0) != 0x42 || hdrByte(1) != 0x4D) {
            throw new IOException("Invalid header");
        }

        int imageStart = hdrDWord(0x0A);
        int hdrSize = hdrWord(0xE);
        int w = 0;
        int h = 0;

        if(imageStart < hdrSize) {
            throw new IOException("Invalid size");
        }
        
        readFully(header, MIN_HDR_SIZE, hdrSize-MIN_HDR_SIZE);

        switch(hdrSize) {
            case 12:
                if(hdrWord(0x16) != 1) {
                    // number of color planes
                	throw new UnsupportedOperationException();
                }

                bpp = hdrWord(0x18);
                w = hdrWord(0x12);
                h = hdrWord(0x14);
                break;

            case 40:
                if(hdrWord(0x1A) != 1) {
                    // number of color planes
                	throw new UnsupportedOperationException();
                }

                if(hdrWord(0x1E) != 0) {
                    // compression
                	throw new UnsupportedOperationException();
                }

                bpp = hdrWord(0x1C);
                w = hdrDWord(0x12);
                h = hdrDWord(0x16);

                if(h < 0) {
                    flipY = false;
                    h = -h;
                } else {
                    flipY = true;
                }
                break;

            default:
            	throw new UnsupportedOperationException();
        }

        if(w <= 0 || h <= 0) {
        	throw new IOException("Empty image");
        }
        
        if(bpp != 24 && bpp != 32) {
        	throw new UnsupportedOperationException();
        }

        format = PixelFormat.BGRA8888;
        width = w;
        height = h;

        skipFully(imageStart - hdrSize);
    }

    @Override
	public int bitsPerPixel() {
		return 32;
	}

    @Override
	public FormatType formatType() {
		return FormatType.COLOR;
	}

    @Override
	public int height() {
		return height;
	}

    @Override
	public byte[] palette() {
		return new byte[0];
	}

    @Override
	public PixelFormat pixelFormat() {
		return format;
	}

    @Override
	public int width() {
		return width;
	}

    @Override
	public void write(ByteBuffer buffer, PixelFormat pixelFormat, FormatType formatType) {
        startPos = buffer.position();
        try {
	        switch(bpp) {
	            case 24:
	                decode24(buffer);
	                break;
	            case 32:
	                decodeSimple(buffer);
	                break;
	            default:
	                throw new AssertionError();
	        }
        }
        catch(IOException ioe) {
        	throw new UncheckedIOException(ioe);
        }
		
	}

    protected final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    protected final void readFully(byte[] b, int off, int len) throws IOException {
        while(len > 0) {
            int read = inputStream.read(b, off, len);
            if(read <= 0) {
                throw new EOFException();
            }
            off += read;
            len -= read;
        }
    }

    protected final void skipFully(int amount) throws IOException {
        while(amount > 0) {
            int skipped = (int)inputStream.skip(amount);
            if(skipped <= 0) {
                throw new EOFException();
            }
            amount -= skipped;
        }
    }

	private void decode24(ByteBuffer bb) throws IOException {
        byte[] tmp = new byte[align4(width * 3)];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            for(int x = 0, w = width * 3; x < w; x += 3) {
                bb.put(tmp[x]).put(tmp[x + 1]).put(tmp[x + 2]).put((byte)255);
            }
        }
    }

	private void decodeSimple(ByteBuffer bb) throws IOException {
        int lineLen = width * 4;
        byte[] tmp = new byte[align4(lineLen)];
        for(int y = 0; y < height; y++) {
            setPos(bb, y);
            readFully(tmp);
            bb.put(tmp, 0, lineLen);
        }
    }

	private int hdrByte(int idx) {
        return header[idx] & 255;
    }

	private int hdrDWord(int idx) {
        return (hdrWord(idx + 2) << 16) | hdrWord(idx);
    }

	private int hdrWord(int idx) {
        return (hdrByte(idx + 1) << 8) | hdrByte(idx);
    }

	private int pixelOffset(int y) {
        if(flipY) {
            y = height - y - 1;
        }
        return y * width;
    }

	private void setPos(ByteBuffer buf, int y) {
        buf.position(startPos + pixelOffset(y) * 4);
    }
}
