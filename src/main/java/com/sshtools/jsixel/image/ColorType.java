package com.sshtools.jsixel.image;

public enum ColorType {
    
    GREYSCALE, TRUECOLOR, INDEXED, GREYALPHA, TRUEALPHA ;
    
    /**
     * Checks if the image has a real alpha channel.
     * This method does not check for the presence of a tRNS chunk.
     *
     * @return true if the image has an alpha channel
     * @see #hasAlpha()
     */
    public boolean hasAlphaChannel() {
        return this == TRUEALPHA || this == GREYALPHA;
    }
    
    public boolean isRGB() {
        return this == TRUEALPHA ||
        	   this == TRUECOLOR ||
        	   this == INDEXED;
    }
}
