# jsixel-converters

This module provides Java clones of the `img2sixel` and `sixel2png` utilities the come with `libsixel`.

## Usage

```
Usage: jimg2sixel [-78DegHiIkPRSuv] [-b=BUILTINPALETTE] [-B=BGCOLOR] [-c=REGION] [-C=COMPLEXIONSCORE] [-d=DIFFUSIONTYPE] [-E=ENCODEPOLICY] [-f=FINDTYPE] [-h=HEIGHT]
                  [-l=LOOPMODE] [-m=PATH] [-n=MACRONO] [-o=PATH] [-p=COLORS] [-q=QUALITYMODE] [-r=RESAMPLINGTYPE] [-s=SELECTTYPE] [-t=PALETTETYPE] [-w=WIDTH] [<files>...]
Convert an image to Sixel stream
      [<files>...]
  -7, --7bit-mode         generate a sixel image for 7 bit terminals or printers
  -8, --8bit-mode         generate a sixel image for 7 bit terminals or printers
  -b, --builtin-palette=BUILTINPALETTE
                          select built-in palette type. valid values: null
  -B, --bgcolor=BGCOLOR   specify background color. BGCOLOR is represented by the following syntax. #rgb, #rrggbb, #rrrgggbbb, #rrrrggggbbbb, rgb:r/g/b, rgb:rr/gg/bb,
                            rgb:rrr/ggg/bbb. rgb:rrrr/gggg/bbbb
  -c, --crop=REGION       crop source image to fit the specified geometry. REGION should  be formatted as '%dx%d+%d+%d'
  -C, --complexion-score=COMPLEXIONSCORE
                          specify an number argument for the score of complexion correction. COMPLEXIONSCORE must be 1 or more.
  -d, --diffusion=DIFFUSIONTYPE
                          choose diffusion method which used with -p option (color reduction). valid values: AUTO, NONE, FS, ATKINSON, JAJUNI, STUCKI, BURKES, A_DITHER,
                            X_DITHER
  -D, --pipe-mode         [[deprecated]] read source images from  stdin continuously
  -e, --monochrome        output monochrome sixel image. this option assumes the terminal background color is black
  -E, --encode-policy=ENCODEPOLICY
                          select encoding policy. valid values: null
  -f, --find-largest=FINDTYPE
                          choose method for finding the largest dimension of median cut boxes for splitting, make sense only when -p option (color reduction) is
                            specified. valid values: null
  -g, --ignore-delay      render GIF animation without delay
  -h, --height=HEIGHT     resize image to specified height. HEIGHT is represented by the following syntax. valid values: AUTO, <number>%, <number>, <number>px
  -H, --help              show this help
  -i, --invert            assume the terminal background color is white, make sense only when -e is given
  -I, --high-color        output 15bpp sixel image
  -k, --insecure          allow to connect to SSL sites without certs (enabled only when configured with --with-libcurl)
  -l, --loop-control=LOOPMODE
                          select loop control mode for GIF animation. valid values: null
  -m, --mapfile=PATH      transform image colors to match this set of colorsspecify map
  -n, --macro-number=MACRONO
                          specify a number argument for DECDMAC and make terminal memorize SIXEL image. No image is shown if this option is specified.
  -o, --outfile=PATH      specify output file name. (default:stdout)
  -p, --colors=COLORS     specify number of colors to reduce the image to (default=256)
  -P, --penetrate         penetrate GNU Screen using DCS
  -q, --quality=QUALITYMODE
                          select quality of color. valid values: null
  -r, --resampling=RESAMPLINGTYPE
                          choose resampling filter used with -w or -h option (scaling). valid values: null
  -R, --gri-limit         limit arguments of DECGRI('!') to 255
  -s, --select-color=SELECTTYPE
                          choose the method for selecting representative color from each median-cut box, make sense only when -p option (color reduction) is specified.
                            valid values: null
  -S, --static            render animated GIF as a static image
  -t, --palette-type=PALETTETYPE
                          select palette color space type. valid values: null
  -u, --use-macro         use DECDMAC and DEVINVM sequences to optimize GIF animation rendering
  -v, --verbose           show debugging info
  -w, --width=WIDTH       resize image to specified width. WIDTH is represented by the following syntax. valid values: AUTO, <number>%, <number>, <number>px

```
