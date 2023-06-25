package com.sshtools.jsixel.image;

public enum Format {
    ALPHA(1, true),
    LUMINANCE(1, false),
    LUMINANCE_ALPHA(2, true),
    RGB(3, false),
    RGBA(4, true),
    BGRA(4, true),
    ABGR(4, true);

    final int numComponents;
    final boolean hasAlpha;

    private Format(int numComponents, boolean hasAlpha) {
        this.numComponents = numComponents;
        this.hasAlpha = hasAlpha;
    }

    public int getNumComponents() {
        return numComponents;
    }

    public boolean isHasAlpha() {
        return hasAlpha;
    }
}