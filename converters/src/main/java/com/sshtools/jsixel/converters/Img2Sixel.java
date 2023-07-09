package com.sshtools.jsixel.converters;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.sshtools.jsixel.lib.Encoder;
import com.sshtools.jsixel.lib.LibSixel;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "jimg2sixel", description = "converts various images into high quality DEC SIXEL image format", usageHelpAutoWidth = true)
public class Img2Sixel implements Callable<Integer> {

	public static void main(String[] args) throws Exception {
		var cmd = new Img2Sixel();
		System.exit(new CommandLine(cmd).setCaseInsensitiveEnumValuesAllowed(true).execute(args));
	}

	public enum DiffusionType {
		AUTO, NONE, FS, ATKINSON, JAJUNI, STUCKI, BURKES, A_DITHER, X_DITHER
	}

	public enum FindType {
		AUTO, NORM, LUM
	}

	public enum SelectType {
		AUTO, CENTER, AVERAGE, HISTOGRAM
	}

	public enum ResamplingType {
		NEAREST, GAUSSIAN, HANNING, BILINEAR, WELSH, BICUBIC, LANCZOS2, LANCZOS3, LANCZOS4
	}

	public enum QualityMode {
		AUTO, LOW, HIGH, FULL
	}

	public enum LoopMode {
		AUTO, FORCE, DISABLE
	}

	public enum PaletteType {
		AUTO, HLS, RGB
	}

	public enum BuiltInPalette {
		XTERM16, XTERM256, VT340MONO, VT340COLOR, GRAY1, GRAY2, GRAY3, GRAY4, GRAY8
	}

	public enum EncodePolicy {
		AUTO, FAST, SIZE
	}

	@Parameters(arity = "0..")
	private Path[] files;

	@Option(names = { "-o",
			"--outfile" }, paramLabel = "PATH", description = "specify output file name. (default:stdout)")
	private Optional<Path> outfile;

	@Option(names = { "-7", "--7bit-mode" }, description = "generate a sixel image for 7 bit terminals or printers")
	private boolean sevenBitMode;

	@Option(names = { "-8", "--8bit-mode" }, description = "generate a sixel image for 8 bit terminals or printers")
	private boolean eightBitMode;

	@Option(names = { "-R", "--gri-limit" }, description = "limit arguments of DECGRI('!') to 255")
	private boolean griLimit;

	@Option(names = { "-p",
			"--colors" }, paramLabel = "COLORS", description = "specify number of colors to reduce the image to (default=256)")
	private Optional<Integer> colors;

	@Option(names = { "-m",
			"--mapfile" }, paramLabel = "PATH", description = "transform image colors to match this set of colorsspecify map")
	private Optional<Path> mapfile;

	@Option(names = { "-e",
			"--monochrome" }, description = "output monochrome sixel image. this option assumes the terminal background color is black")
	private boolean monochrome;

	@Option(names = { "-k",
			"--insecure" }, description = "allow to connect to SSL sites without certs (enabled only when configured with --with-libcurl)")
	private boolean insecure;

	@Option(names = { "-i",
			"--invert" }, description = "assume the terminal background color is white, make sense only when -e is given")
	private boolean invert;

	@Option(names = { "-I", "--high-color" }, description = "output 15bpp sixel image")
	private boolean highColor;

	@Option(names = { "-u",
			"--use-macro" }, description = "use DECDMAC and DEVINVM sequences to optimize GIF animation rendering")
	private boolean useMacro;

	@Option(names = { "-n",
			"--macro-number" }, paramLabel = "MACRONO", description = "specify a number argument for DECDMAC and make terminal memorize SIXEL image. No image is shown if this option is specified.")
	private Optional<Integer> macroNumber;

	@Option(names = { "-C",
			"--complexion-score" }, paramLabel = "COMPLEXIONSCORE", description = "specify an number argument for the score of complexion correction. COMPLEXIONSCORE must be 1 or more.")
	private Optional<Integer> complexionScore;

	@Option(names = { "-g", "--ignore-delay" }, description = "render GIF animation without delay")
	private boolean ignoreDelay;

	@Option(names = { "-S", "--static" }, description = "render animated GIF as a static image")
	private boolean staticImage;

	@Option(names = { "-d",
			"--diffusion" }, paramLabel = "DIFFUSIONTYPE", description = "choose diffusion method which used with -p option (color reduction). valid values: ${COMPLETION-CANDIDATES}")
	private Optional<DiffusionType> diffusion;

	@Option(names = { "-f",
			"--find-largest" }, paramLabel = "FINDTYPE", description = "choose method for finding the largest dimension of median cut boxes for splitting, make sense only when -p option (color reduction) is specified. valid values: ${COMPLETION-CANDIDATES")
	private Optional<FindType> findLargest;

	@Option(names = { "-s",
			"--select-color" }, paramLabel = "SELECTTYPE", description = "choose the method for selecting representative color from each median-cut box, make sense only when -p option (color reduction) is specified. valid values: ${COMPLETION-CANDIDATES")
	private Optional<SelectType> selectColor;

	@Option(names = { "-c",
			"--crop" }, paramLabel = "REGION", description = "crop source image to fit the specified geometry. REGION should  be formatted as '%%dx%%d+%%d+%%d'")
	private Optional<String> crop;

	@Option(names = { "-w",
			"--width" }, paramLabel = "WIDTH", description = "resize image to specified width. WIDTH is represented by the following syntax. valid values: AUTO, <number>%%, <number>, <number>px")
	private Optional<String> width;

	@Option(names = { "-h",
			"--height" }, paramLabel = "HEIGHT", description = "resize image to specified height. HEIGHT is represented by the following syntax. valid values: AUTO, <number>%%, <number>, <number>px")
	private Optional<String> height;

	@Option(names = { "-r",
			"--resampling" }, paramLabel = "RESAMPLINGTYPE", description = "choose resampling filter used with -w or -h option (scaling). valid values: ${COMPLETION-CANDIDATES}")
	private Optional<ResamplingType> resampling;

	@Option(names = { "-q",
			"--quality" }, paramLabel = "QUALITYMODE", description = "select quality of color. valid values: ${COMPLETION-CANDIDATES}")
	private Optional<QualityMode> quality;

	@Option(names = { "-l",
			"--loop-control" }, paramLabel = "LOOPMODE", description = "select loop control mode for GIF animation. valid values: ${COMPLETION-CANDIDATES}")
	private Optional<LoopMode> loopControl;

	@Option(names = { "-t",
			"--palette-type" }, paramLabel = "PALETTETYPE", description = "select palette color space type. valid values: ${COMPLETION-CANDIDATES}")
	private Optional<PaletteType> paletteType;

	@Option(names = { "-b",
			"--builtin-palette" }, paramLabel = "BUILTINPALETTE", description = "select built-in palette type. valid values: ${COMPLETION-CANDIDATES}")
	private Optional<BuiltInPalette> builtinPalette;

	@Option(names = { "-E",
			"--encode-policy" }, paramLabel = "ENCODEPOLICY", description = "select encoding policy. valid values: ${COMPLETION-CANDIDATES}")
	private Optional<EncodePolicy> encodePolicy;

	@Option(names = { "-B",
			"--bgcolor" }, paramLabel = "BGCOLOR", description = "specify background color. BGCOLOR is represented by the following syntax. #rgb, #rrggbb, #rrrgggbbb, #rrrrggggbbbb, rgb:r/g/b, rgb:rr/gg/bb, rgb:rrr/ggg/bbb. rgb:rrrr/gggg/bbbb")
	private Optional<String> bgColor;

	@Option(names = { "-P", "--penetrate" }, description = "penetrate GNU Screen using DCS")
	private boolean penetrate;

	@Option(names = { "-D", "--pipe-mode" }, description = "[[deprecated]] read source images from  stdin continuously")
	private boolean pipeMode;

	@Option(names = { "-v", "--verbose" }, description = "show debugging info")
	private boolean verbose;

	@Option(names = { "-V", "--version" }, description = "show version info", versionHelp = true)
	private boolean version;

	@Option(names = { "-H", "--help" }, description = "show this help", usageHelp = true)
	private boolean help;

	@Override
	public Integer call() throws Exception {
		try (var enc = new Encoder()) {
			outfile.ifPresent(f -> enc.opt(LibSixel.SIXEL_OPTFLAG_OUTFILE, f.toString()));
			if (sevenBitMode)
				enc.opt(LibSixel.SIXEL_OPTFLAG_7BIT_MODE);
			else if (eightBitMode)
				enc.opt(LibSixel.SIXEL_OPTFLAG_8BIT_MODE);
			if (griLimit)
				enc.opt(LibSixel.SIXEL_OPTFLAG_HAS_GRI_ARG_LIMIT);
			colors.ifPresent(c -> enc.opt(LibSixel.SIXEL_OPTFLAG_COLORS, c));
			mapfile.ifPresent(f -> enc.opt(LibSixel.SIXEL_OPTFLAG_MAPFILE, f.toString()));
			if (monochrome)
				enc.opt(LibSixel.SIXEL_OPTFLAG_MONOCHROME);
			if (highColor)
				enc.opt(LibSixel.SIXEL_OPTFLAG_HIGH_COLOR);
			builtinPalette.ifPresent(p -> enc.opt(LibSixel.SIXEL_OPTFLAG_BUILTIN_PALETTE, p.name().toLowerCase()));
			diffusion.ifPresent(d -> enc.opt(LibSixel.SIXEL_OPTFLAG_DIFFUSION, d.name().toLowerCase()));
			findLargest.ifPresent(l -> enc.opt(LibSixel.SIXEL_OPTFLAG_FIND_LARGEST, l.name().toLowerCase()));
			selectColor.ifPresent(c -> enc.opt(LibSixel.SIXEL_OPTFLAG_SELECT_COLOR, c.name().toLowerCase()));
			crop.ifPresent(r -> enc.opt(LibSixel.SIXEL_OPTFLAG_CROP, r));
			width.ifPresent(w -> enc.opt(LibSixel.SIXEL_OPTFLAG_WIDTH, w));
			height.ifPresent(h -> enc.opt(LibSixel.SIXEL_OPTFLAG_HEIGHT, h));
			resampling.ifPresent(r -> enc.opt(LibSixel.SIXEL_OPTFLAG_RESAMPLING, r.name().toLowerCase()));
			quality.ifPresent(r -> enc.opt(LibSixel.SIXEL_OPTFLAG_RESAMPLING, r.name().toLowerCase()));
			paletteType.ifPresent(p -> enc.opt(LibSixel.SIXEL_OPTFLAG_PALETTE_TYPE, p.name().toLowerCase()));
			if (insecure)
				enc.opt(LibSixel.SIXEL_OPTFLAG_INSECURE);
			if (invert)
				enc.opt(LibSixel.SIXEL_OPTFLAG_INVERT);
			loopControl.ifPresent(l -> enc.opt(LibSixel.SIXEL_OPTFLAG_LOOPMODE, l.name().toLowerCase()));
			if (useMacro)
				enc.opt(LibSixel.SIXEL_OPTFLAG_USE_MACRO);
			if(ignoreDelay)
				enc.opt(LibSixel.SIXEL_OPTFLAG_IGNORE_DELAY);
			if(verbose)
				enc.opt(LibSixel.SIXEL_OPTFLAG_VERBOSE);
			if(staticImage)
				enc.opt(LibSixel.SIXEL_OPTFLAG_STATIC);
			macroNumber.ifPresent(m -> enc.opt(LibSixel.SIXEL_OPTFLAG_MACRO_NUMBER, m));
			if(penetrate)
				enc.opt(LibSixel.SIXEL_OPTFLAG_PENETRATE);
			encodePolicy.ifPresent(e -> enc.opt(LibSixel.SIXEL_OPTFLAG_ENCODE_POLICY, e.name().toLowerCase()));
			bgColor.ifPresent(b -> enc.opt(LibSixel.SIXEL_OPTFLAG_BGCOLOR, b));
			complexionScore.ifPresent(c -> enc.opt(LibSixel.SIXEL_OPTFLAG_COMPLEXION_SCORE, c));
			if(pipeMode)
				enc.opt(LibSixel.SIXEL_OPTFLAG_PIPE_MODE);

			if (files.length == 0) {
				enc.encode();
			} else {
				for (var file : files) {
					enc.encode(file);
				}
			}
		}
		return null;
	}
}
