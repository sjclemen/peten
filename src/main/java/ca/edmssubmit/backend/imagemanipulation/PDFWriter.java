package ca.edmssubmit.backend.imagemanipulation;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import net.sf.image4j.codec.bmp.BMPImage;
import net.sf.image4j.codec.bmp.InfoHeader;

public class PDFWriter {
	protected final static Logger logger = Logger.getLogger(PDFWriter.class);

    private static final double MM_PER_INCH = 2.834645669;
	
    // ONLY HANDLES BMP. for now.
	public static void writePdf(File output, List<File> input) throws IOException {
		PDDocument document = new PDDocument();
		
		long startTime = System.nanoTime();
		long bmpReadOverall = 0;
		long greyscaleConversionOverall = 0;
		long addPageOverall = 0;
		try {
			for (File inputFile : input) {
				long loopStart = System.nanoTime();
				BMPImage image = BMPReader.readBmp(inputFile);
				InfoHeader infoHeader = image.getInfoHeader();
		
				float width = (float) (((double)infoHeader.iWidth) / ((double)infoHeader.iXpixelsPerM) * 1000D * MM_PER_INCH);
				float height = (float) (((double)infoHeader.iHeight) / ((double)infoHeader.iXpixelsPerM) * 1000D * MM_PER_INCH);
				BufferedImage buffer = image.getImage();
				
				long bmpReadDone = System.nanoTime();
				
				if (buffer.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
					buffer = convertToGreyscale(buffer);
					image = null; // free up memory
				}
				
				long greyscaleConversion = System.nanoTime();

				PDPage pdPage = new PDPage(new PDRectangle(width, height));
				
				PDImageXObject pdImageX;
				if (buffer.getType() == BufferedImage.TYPE_BYTE_GRAY) {
					pdImageX = GreyscaleLosslessFactory.createFromImage(document, buffer);
				} else {
					pdImageX = LosslessFactory.createFromImage(document, buffer);
				}
				
				PDPageContentStream contentStream = null;
				try {
					contentStream = new PDPageContentStream(document, pdPage, false, true); // note we're overwriting everything this might cause problems
					contentStream.drawImage(pdImageX, 0, 0, width, height);
				} finally {
					if (contentStream != null) {
						contentStream.close();
					}
				}
		
				document.addPage(pdPage);
				long addPage = System.nanoTime();
				
				bmpReadOverall += (bmpReadDone - loopStart);
				greyscaleConversionOverall += (greyscaleConversion - bmpReadDone);
				addPageOverall += (addPage - greyscaleConversion);
			}
		} catch (IOException ie) {
			logger.error("Failed to create PDF", ie);
			document.close();
			throw ie;
		} catch (OutOfMemoryError oome) {
			document.close();
			logger.error("Ran out of memory while creating PDF", oome);
			throw oome;
		}
		
		long startPdfWrite = System.nanoTime();

		// Save the newly created document
		document.save(output);

		// finally make sure that the document is properly
		// closed.
		document.close();
		
		long pdfWrite = System.nanoTime();
		
		System.out.println("OVERALL TIME: " + (pdfWrite - startTime)/1000000 + " bmp read " + bmpReadOverall/1000000 + 
				" greyscale conversion " + greyscaleConversionOverall/1000000 + " add page " + addPageOverall/1000000 +
				" pdf write " + (pdfWrite - startPdfWrite)/1000000);

	}
	
	private static BufferedImage convertToGreyscale(BufferedImage in) {
		int imgWidth = in.getWidth();
		int imgHeight = in.getHeight();
		
		ColorModel colourModel = in.getColorModel();
		if (!(colourModel instanceof IndexColorModel)) {
			return in;
		}
		
		IndexColorModel indexColourModel = (IndexColorModel)colourModel;
		byte[] colourOne = new byte[indexColourModel.getMapSize()];
		byte[] colourTwo = new byte[indexColourModel.getMapSize()];
		Arrays.fill(colourOne, (byte)-1);
		indexColourModel.getAlphas(colourTwo);
		if (!Arrays.equals(colourOne, colourTwo)) {
			return in;
		}
		
		indexColourModel.getBlues(colourOne);
		indexColourModel.getReds(colourTwo);
		if (!Arrays.equals(colourOne, colourTwo)) {
			return in;
		}
		indexColourModel.getGreens(colourTwo);
		if (!Arrays.equals(colourOne, colourTwo)) {
			return in;
		}
		
		BufferedImage out = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
		DataBufferByte outBuffer = (DataBufferByte)out.getRaster().getDataBuffer();
		byte[] outData = outBuffer.getData();
		
		DataBufferByte inBuffer = (DataBufferByte)in.getRaster().getDataBuffer();
		byte[] inData = inBuffer.getData();
		
		boolean awesomePalette = true;
		for (int i = 0; i < indexColourModel.getMapSize(); i++) {
			if (colourOne[i] != (byte)i) {
				awesomePalette = false;
				break;
			}
		}
		
		if (awesomePalette) {
			System.arraycopy(inData, 0, outData, 0, inData.length);
			return out;
		}
		
		// TODO: test this.
		for (int y = 0; y < imgHeight; y++) {
			for (int x = 0; x < imgWidth; x++) {
				outData[x + imgWidth*y] = colourOne[inData[x + imgWidth*y] + 128];
			}
		}
		return out;
	}
}
