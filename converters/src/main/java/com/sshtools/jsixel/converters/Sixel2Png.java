package com.sshtools.jsixel.converters;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.sshtools.jsixel.lib.Decoder;
import com.sshtools.jsixel.lib.LibSixel;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jsixel2png", description = "convert DEC SIXEL images into PNG format images", usageHelpAutoWidth = true)
public class Sixel2Png implements Callable<Integer> {

	@Option(names = { "-o",
			"--output" }, paramLabel = "PATH", description = "specify output file")
	private Optional<Path> output;

	@Option(names = { "-i",
			"--input" }, paramLabel = "PATH", description = "specify input file")
	private Optional<Path> input;

	@Option(names = { "-V", "--version" }, description = "show version info", versionHelp = true)
	private boolean version;

	@Option(names = { "-H", "--help" }, description = "show this help", usageHelp = true)
	private boolean help;

	public static void main(String[] args) throws Exception {
		var cmd = new Sixel2Png();
		System.exit(new CommandLine(cmd).setCaseInsensitiveEnumValuesAllowed(true).execute(args));
	}

	@Override
	public Integer call() throws Exception {
		try(var dec = new Decoder()) {
			input.ifPresent(i -> dec.opt(LibSixel.SIXEL_OPTFLAG_INPUT, i.toString()));
			output.ifPresent(o -> dec.opt(LibSixel.SIXEL_OPTFLAG_OUTPUT, o.toString()));
			dec.decode();
		}
		return 0;
	}
}
