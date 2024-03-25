/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link WavFileSplitter}
 */
class TestWavFileSplitter {

	/**
	 * Tests splitting a real-world recording.
	 */
	@Test
	void testSplit() throws IOException, UnsupportedAudioFileException {
		File output = new File("./target/TestWavFileSplitter");
		clear(output);
		output.mkdirs();
		new WavFileSplitter(new File("./src/test/fixtures/test-alaw.wav")).setOutputDir(output).run();
		
		Assertions.assertEquals(3, output.listFiles(f -> f.isFile()).length);
	}

	private void clear(File output) throws IOException {
		if (output.isDirectory()) {
			Files.walkFileTree(output.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return super.visitFile(file, attrs);
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return super.postVisitDirectory(dir, exc);
				}
			});
		} else {
			Files.deleteIfExists(output.toPath());
		}
	}

}
