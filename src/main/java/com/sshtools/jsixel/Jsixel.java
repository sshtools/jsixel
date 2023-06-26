package com.sshtools.jsixel;

import java.nio.channels.Channels;

import com.sshtools.jsixel.converter.Converter;
import com.sshtools.jsixel.image.PNGDecoder;
import com.sun.jna.Platform;

public class Jsixel {

	public static void main(String[] args) throws Exception {
		System.out.println("Resource path: " + Platform.RESOURCE_PREFIX);
		try(var in = Jsixel.class.getResource("jsixel.png").openStream()) {
			new Converter(new PNGDecoder(in)).write(Channels.newChannel(System.out));
		}
	}
}
