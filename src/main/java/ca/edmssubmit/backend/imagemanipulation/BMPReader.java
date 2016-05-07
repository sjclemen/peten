package ca.edmssubmit.backend.imagemanipulation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPImage;

public class BMPReader {
	public static BMPImage readBmp(File path) throws IOException {
		FileInputStream fis = new FileInputStream(path);
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(fis);
			BMPImage image = BMPDecoder.readExt(bis);
			return image;
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
			try {
				fis.close();
			} catch (IOException e) {
			}
		}
	}
}
