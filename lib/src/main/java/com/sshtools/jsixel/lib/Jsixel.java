package com.sshtools.jsixel.lib;

import java.nio.file.Files;

public class Jsixel {

	public static void main(String[] args) throws Exception {
		try(var in = Jsixel.class.getResource("jsixel.png").openStream()) {
			var tempFile = Files.createTempFile("sixel", ".sixel");
			try {
				try(var out = Files.newOutputStream(tempFile)) {
					in.transferTo(out);
				}
				try(var enc = new Encoder()) {
					enc.encode(tempFile);
				}
			}
			finally {
				Files.delete(tempFile);
			}
		}
	}
}
