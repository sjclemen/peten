package ca.edmssubmit.backend.messages;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import ca.edmssubmit.api.AttachTag;
import ca.edmssubmit.api.CreateDocument;
import ca.edmssubmit.api.CreateMetadata;
import ca.edmssubmit.api.Globals;
import ca.edmssubmit.api.json.CreateDocumentResponse;
import ca.edmssubmit.backend.imagemanipulation.BMPReader;
import ca.edmssubmit.backend.imagemanipulation.PDFWriter;
import ca.edmssubmit.backend.imagemanipulation.PNGWriter;
import ca.edmssubmit.backend.tasktypes.HttpRequestTask;
import net.sf.image4j.codec.bmp.BMPImage;
import net.sf.image4j.codec.bmp.InfoHeader;

/**
 * This class assumes all relevant validation has already been
 * done by the frontend.
 *
 */
public class SubmitFilesBEReq extends HttpRequestTask<SubmitFilesBEResp> {
	private final Map<String, String> extensionMap = ImmutableMap.of(
			".png", "image/png", 
			".pdf", "application/pdf",
			".jpg", "image/jpeg",
			".odt", "application/vnd.oasis.opendocument.text");
	
	private final static Logger logger = Logger.getLogger(SubmitFilesBEReq.class);
	private final String apiToken;
	private final int requestId;
	private final List<String> absolutePaths;
	private final String name;
	private final HashMap<Integer, String> tags;
	private final String source;
	
	public SubmitFilesBEReq(String apiToken, int requestId, List<String> absolutePaths,
			String name, Map<Integer, String> tags, String source) {
		this.apiToken = apiToken;
		this.requestId = requestId;
		this.absolutePaths = new ArrayList<String>(absolutePaths);
		this.name = name;
		this.tags = new HashMap<Integer, String>(tags);
		this.source = source;
	}
	
	@Override
	public SubmitFilesBEResp doCall() throws Exception {
		logger.debug(String.format("Began executing SubmitFilesBEReq api token %s, requestId %d, absolutePaths %s, name %s, tagCount %d, source %s", 
				apiToken, requestId, Globals.implodeString(absolutePaths), name, tags.size(), source));
		List<String> warnings = new ArrayList<String>();
		// step 1: compress, either as PNG or PDF
		String mimeType;
		File fileToSubmit = null;
		boolean fileIsGenerated;
		if (absolutePaths.size() == 1) {
			String extension = getExtensionWithDot(absolutePaths.get(0));
			if (extension.equals(".bmp")) {
				fileToSubmit = compressBmpToPng(absolutePaths.get(0));
				fileIsGenerated = true;
			} else {
				fileToSubmit = new File(absolutePaths.get(0));
				fileIsGenerated = false;
			}
			mimeType = extensionMap.get(getExtensionWithDot(fileToSubmit.getAbsolutePath()));
			if (mimeType == null) {
				throw new IllegalArgumentException("Extension of input file not recognized.");
			}
		} else {
			List<File> filesList = new ArrayList<File>();
			for (String absolutePath : absolutePaths) {
				filesList.add(new File(absolutePath));
			}
			try {
				fileIsGenerated = true;
				fileToSubmit = File.createTempFile("edmssubmit-", "pdf");
				mimeType = "application/pdf";
				PDFWriter.writePdf(fileToSubmit, filesList);
			} catch (IOException ie) {
				if (fileToSubmit != null) {
					fileToSubmit.delete();
				}
				throw ie;
			}
		}
		
		// step 2: upload.
		try {
			CreateDocumentResponse documentResponse = CreateDocument.createDocument(apiToken, name, fileToSubmit, mimeType, "", 1);
			// step 3: attach tags

			for (Entry<Integer, String> tag : tags.entrySet()) {
				try {
					AttachTag.attachTag(apiToken, documentResponse.id, tag.getKey());
				} catch (IOException ie) {
					warnings.add("Failed to create tag " + tag.getValue() + " because " + ie.getMessage());
					logger.warn("Failed to create tag", ie);
				}
			}
			
			// step 4: attach metadata
			try {
				CreateMetadata.createMetadata(apiToken, documentResponse.id, 2, source);
			} catch (IOException ie) {
				warnings.add("Failed to create metadata " + ie.getMessage());
				logger.warn("Failed to create metadata.", ie);
			}
			return new SubmitFilesBEResp(documentResponse.id, warnings);	
		} finally {
			if (fileIsGenerated) {
				fileToSubmit.delete();
			}
		}
	}
	
	private static String getExtensionWithDot(String onlyPath) {
		int lastZero = onlyPath.lastIndexOf('.');
		if (lastZero == -1 || lastZero == onlyPath.length()-1) {
			throw new IllegalArgumentException("File did not contain extension, cannot determine type");
		}
		String extension = onlyPath.substring(lastZero, onlyPath.length());
		return extension;
	}
	
	private static File compressBmpToPng(String absolutePath) throws IOException {
		File pngOut = null;
		try {
			long startTime = System.nanoTime();
			pngOut = File.createTempFile("edmssubmitpng-", ".png");
			BMPImage bmpImage = BMPReader.readBmp(new File(absolutePath));
			InfoHeader bmpInfoHeader = bmpImage.getInfoHeader();
			BufferedImage bmpBufferedImage = bmpImage.getImage();
			
			long bmpReadTime = System.nanoTime();
			
			PNGWriter.writePNG(pngOut, bmpBufferedImage, bmpInfoHeader.iXpixelsPerM, bmpInfoHeader.iYpixelsPerM);
			
			long pngWriteTime = System.nanoTime();

			logger.debug("Stats for " + absolutePath + " PNG WRITE: " + (pngWriteTime - bmpReadTime)/1000 + " BMP READ " + (bmpReadTime - startTime)/1000 + " PNG OUT WRITTEN TO " + pngOut.getName());
			
			return pngOut;
		} catch (IOException ie) {
			if (pngOut != null) {
				pngOut.delete();
			}
			throw ie;
		}
	}


}
