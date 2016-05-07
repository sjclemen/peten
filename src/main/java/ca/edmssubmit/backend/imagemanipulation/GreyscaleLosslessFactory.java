/*
 * THIS WORK IS DERIVED FROM PDFBOX 2.0 RC4 AND CORRECTS A BUG
 * IN THE HANDLING OF GREYSCALE IMAGES.
 * Copyright 2014 The Apache Software Foundation.
 * Copyright 2016 Stephen J Clement.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.edmssubmit.backend.imagemanipulation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Factory for creating a PDImageXObject containing a lossless compressed image.
 *
 * @author Tilman Hausherr
 */
public final class GreyscaleLosslessFactory
{
    private GreyscaleLosslessFactory()
    {
    }
    
    /**
     * Creates a new GREYSCALE lossless encoded Image XObject from a Buffered Image.
     *
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @return a new Image XObject
     * @throws IOException if something goes wrong
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image)
            throws IOException
    {
        int bpc;
        PDDeviceColorSpace deviceColorSpace;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int height = image.getHeight();
        int width = image.getWidth();

        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY && image.getColorModel().getPixelSize() == 8)
        {
            // grayscale images need one color per sample
            bpc = image.getColorModel().getPixelSize();
            deviceColorSpace = PDDeviceGray.INSTANCE;
            DataBufferByte backingByteBuffer = (DataBufferByte)image.getData().getDataBuffer();
            bos.write(backingByteBuffer.getData());
        }
        else
        {
            throw new IOException("Expected greyscale image, with no alpha channel.");
        }

        PDImageXObject pdImage = prepareImageXObject(document, bos.toByteArray(), 
                image.getWidth(), image.getHeight(), bpc, deviceColorSpace);

        if (image.getColorModel().hasAlpha()) {
        	throw new IOException("No alpha support.");
        }
        
        return pdImage;
    }


    /**
     * Create a PDImageXObject while making a decision whether not to 
     * compress, use Flate filter only, or Flate and LZW filters.
     * 
     * @param document The document.
     * @param byteArray array with data.
     * @param width the image width
     * @param height the image height
     * @param bitsPerComponent the bits per component
     * @param initColorSpace the color space
     * @return the newly created PDImageXObject with the data compressed.
     * @throws IOException 
     */
    private static PDImageXObject prepareImageXObject(PDDocument document, 
            byte [] byteArray, int width, int height, int bitsPerComponent, 
            PDColorSpace initColorSpace) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Filter filter = FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);
        filter.encode(new ByteArrayInputStream(byteArray), baos, new COSDictionary(), 0);

        ByteArrayInputStream encodedByteStream = new ByteArrayInputStream(baos.toByteArray());
        return new PDImageXObject(document, encodedByteStream, COSName.FLATE_DECODE, 
                width, height, bitsPerComponent, initColorSpace);
    }

}
