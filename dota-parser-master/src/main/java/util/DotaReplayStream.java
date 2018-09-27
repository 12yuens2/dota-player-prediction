package util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import skadistats.clarity.source.InputStreamSource;

public class DotaReplayStream extends InputStreamSource {

    private static CompressorInputStream getStreamForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        return new CompressorStreamFactory().createCompressorInputStream(bis);
    }
    
    private static InputStream getStreamFromFile(String filename, boolean isCompressed) throws FileNotFoundException, CompressorException {
    	if (isCompressed) {
    		return getStreamForCompressedFile(filename);
    	}
    	else {
    		return new BufferedInputStream(new FileInputStream(filename));
    	}
    }
	
	public DotaReplayStream(String filename, boolean isCompressed) throws FileNotFoundException, CompressorException {
		super(getStreamFromFile(filename, isCompressed));
	}
}
