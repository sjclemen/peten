package ca.edmssubmit.backend.imagemanipulation;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPHYS;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

public class PNGWriter {
	public static void writePNG(File outFile, BufferedImage bufferedImage, long xppm, long yppm) {
		ColorModel colourModel = bufferedImage.getColorModel();
		
		// we only support RGB, RGBA and byte-indexed input
		// everything else gets force-converted to RGBA
		int bufferedImageType = bufferedImage.getType();
		boolean forceRGBA;
		int bpp;
		switch (bufferedImageType) {
			case BufferedImage.TYPE_INT_ARGB:
				forceRGBA = true;
				bpp = 32;
				break;
			case BufferedImage.TYPE_INT_RGB:
				forceRGBA = false;
				bpp = 24;
				break;
			case BufferedImage.TYPE_BYTE_INDEXED:
				forceRGBA = false;
				bpp = 8;
				break;
			default:
				forceRGBA = true;
				bpp = 32;
				break;
		}

		ImageInfo pngImageInfo = new ImageInfo(
				bufferedImage.getWidth(),
				bufferedImage.getHeight(),
				8,
				(bpp == 32),
				false, // we don't support greyscale
				bufferedImageType == BufferedImage.TYPE_BYTE_INDEXED);
		PngWriter pngWriter = new PngWriter(outFile, pngImageInfo);
		try {
			pngWriter.setCompLevel(6);
			
			// deal w/indexed PNGs
			if (bufferedImageType == BufferedImage.TYPE_BYTE_INDEXED) {
				IndexColorModel indexColourModel = (IndexColorModel)colourModel;
				int paletteSize = indexColourModel.getMapSize();
				PngChunkPLTE pngPaletteChunk = pngWriter.getMetadata().createPLTEChunk();
				pngPaletteChunk.setNentries(paletteSize);
				for (int i = 0; i < paletteSize; i++) {
					pngPaletteChunk.setEntry(i,
							indexColourModel.getRed(i),
							indexColourModel.getGreen(i),
							indexColourModel.getBlue(i));
				}
				if (colourModel.hasAlpha()) {
					PngChunkTRNS transparentChunk = pngWriter.getMetadata().createTRNSChunk();
					int[] alphaChannel = new int[paletteSize];
					for (int i = 0; i < paletteSize; i++) {
						alphaChannel[i] = indexColourModel.getAlpha(i);
					}
					transparentChunk.setPalletteAlpha(alphaChannel);
				}
			}
			
			// write physics information
			PngChunkPHYS physics = new PngChunkPHYS(pngImageInfo);
			physics.setPixelsxUnitX(xppm);
			physics.setPixelsxUnitY(yppm);
			pngWriter.queueChunk(physics);
			
			// write data/idat
			// this array is not the right size but i'm too lazy to fix it right now
			int[] rowData = new int[bufferedImage.getWidth() * bpp / 8];
			for (int row = 0; row < bufferedImage.getHeight(); row++) {
				if (forceRGBA) {
					ImageLineInt imgLine = new ImageLineInt(pngImageInfo);
					bufferedImage.getRGB(0, row, bufferedImage.getWidth(), 1, rowData, 0, bufferedImage.getWidth());
					for (int i = 0; i < bufferedImage.getWidth(); i++) {
						ImageLineHelper.setPixelRGBA8(imgLine, i, rowData[i]);
					}
					pngWriter.writeRow(imgLine);		
				} else {
					bufferedImage.getRaster().getPixels(0, row, bufferedImage.getWidth(), 1, rowData);
					pngWriter.writeRowInt(rowData);
				}
				
			}
			
			pngWriter.end();
		} catch (RuntimeException e) {
			pngWriter.close();
			throw e;
		} catch (OutOfMemoryError oome) {
			pngWriter.close();
			throw oome;
		}
	}
}
