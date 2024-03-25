package org.mjsip.examples;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link AudioApp}.
 */
class TestAudioApp {
	
	@Test
	void testSendReceive() throws IOException {
		File rx = File.createTempFile("rx-audio", ".wav");
		
		AudioApp.main(new String[] {
			"-r" , "5000",
			"-s", "localhost:5000",
			"-i", new File("./src/test/fixtures/tone-ulaw-8000khz.wav").getAbsolutePath(),
			"-o", rx.getAbsolutePath(),
			"--stopWhenTransmitted"
		});
		
		assertTrue(rx.length() > 10000);
	}

}
