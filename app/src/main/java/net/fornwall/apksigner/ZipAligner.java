package net.fornwall.apksigner;

import net.fornwall.apksigner.zipio.ZioEntry;
import net.fornwall.apksigner.zipio.ZipInput;
import net.fornwall.apksigner.zipio.ZipOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ZipAligner {
	public static void alignZip(String inputZipFilename, String outputZipFilename) throws IOException, GeneralSecurityException {
		File inFile = new File(inputZipFilename).getCanonicalFile();
		File outFile = new File(outputZipFilename).getCanonicalFile();
		if (inFile.equals(outFile)) {
			throw new IllegalArgumentException("Input and output files are the same");
		}

		try (ZipInput input = new ZipInput(inputZipFilename)) {
			try (ZipOutput zipOutput = new ZipOutput(new FileOutputStream(outputZipFilename))) {
				for (ZioEntry inEntry : input.entries.values()) {
					zipOutput.write(inEntry);
				}
			}
		}
	}

}