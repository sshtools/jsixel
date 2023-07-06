# JSixel

![JSixel Logo](./src/web/web-title.png "Logo")

JNA based Java bindings for [libsixel](https://github.com/saitoha/libsixel).

## Who Is This For?

 * Java terminal emulator authors who want to parse and display sixel images.
 * Java command line application authors who want to output images inside other terminals that support sixels.
 * Image tool authors who may wish to convert to and from sixels.

## Features

 * Ships with pre-compiled `libsixel` libraries for Linux Intel and Arm, Windows and Mac OS (all 64 bit). Other systems must have it installed externally.
 * Low level bindings matching `libsixel` API.
 * High level file based encoder and decoder (similar to `python/libsixel/encoder.py` and `python/libsixel/decoder.py`).
 * High level object oriented API for converting image files to sixel files and vice versa.
 * [Clones](converters/README.md) of `img2sixel` and `sixel2png` with full command line option compatibility.
 * JPMS compatible.
 
For more information on basic usage see [lib/README.md](lib/README.md)
 
### Modules

Libsixel's built-in image encoders only work with files, so the following optional modules should be used if you want to convert using sources 
and targets other than files using an object oriented API.

The module to pick will depend on the GUI toolkit (if any) you wish to use.  

 * [Swing/AWT using](awt/README.md) using `BufferedImage`.
 * [JavaFX](javafx/README.md) using `Image`.
 * [SWT](swt/README.md) using `ImageData`.
 * [Slim](slim/README.md) basic decoder (For PNG, JPEG and BMP only at the moment).
 * Supply and consume raw [RGB/A](lib/README.md) and indexed data via `ByteBuffer`.
 
 Each module has slightly different capabilities.
 
 | Module | Toolkit | Encode Image File -> Sixel File | Decode Sixel File -> Image File | Encode Sixel Stream -> Image Stream | Decode Sixel Stream -> Image Stream |
 | ------ | ------- | ------------------------------- | ------------------------------- | ----------------------------------- | ----------------------------------- |
 | None   | Any | Yes | Yes | No | No |
 | [jsixel-awt](awt/README.MD) | Swing/AWT | Yes | Yes | Yes | Yes |
 | [jsixel-javafx](javafx/README.MD) | JavaFX | Yes | Raw Only | Yes | Raw Only |
 | [jsixel-swt](swt/README.MD) | SWT | Yes | Raw Only | Yes | Raw Only |
 | [jsixel-slim](slim/README.MD) | None | Yes | Raw Only | Yes | Raw Only |
 
  * While all modules support raw bitmaps, *Raw Only* means it will only decode to raw indexed bitmaps (as supplied by libsixel). 
  * All other modules can in theory at least load all of the image types the toolkit supports, although at this
    stage there may be some bugs in lesser used formats. Only AWT and SWT can currently save to specialised image formats.   
 
## TODO 
 * Graal compatibility.
 * Animation support.
 * More pre-built libraries for different platforms and architectures.
 * Use newer fork of libsixel.