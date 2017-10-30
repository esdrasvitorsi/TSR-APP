package m.tri.trafficsignrecognition.tsr.system;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.util.TimingLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import m.tri.trafficsignrecognition.MainActivity;

/**
 * Created by evitorsi on 10/27/2017.
 */
public class TSRSystem {

    // Pixels of the input image (frame)
    private int []inputFramePixels;

    // Input frame width
    private int inputFrameWidth;

    // Input frame height
    private int inputFrameHeight;

    // Pixels of the binary image
    private int []binaryImgPixels;

    // Pixels of the erosion image
    private int [] erosionImgPixels;

    // Pixels of the dilation image
    private int [] dilationImgPixels;

    public TSRSystem()
    {

    }

    static public void saveBmp(File fileName, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*******************************************************************************
     * Function Name  : savePixelsArrayAsBmp()
     * Description    : Save a pixel array as bmp
     *******************************************************************************/
    private void savePixelArrayAsBmp(File imgFile,int[] pixelArray,int imgWidth, int imgHeight)
    {
        // Create a new reference to the binary image.
        Bitmap outputImg = Bitmap.createBitmap(imgWidth,imgHeight,Bitmap.Config.ARGB_8888);

        for(int j = 0; j < imgHeight; j++)
        {
            for(int i = 0; i < imgWidth; i++)
            {
                outputImg.setPixel(i,j,pixelArray[i+j*imgWidth]);
            }
        }

        // Save the bmp image
        saveBmp(imgFile, outputImg);
    }

    /*******************************************************************************
     * Function Name  : saveBinaryImgPixels()
     * Description    : Save the binary image pixel array as bmp
     *******************************************************************************/
    public void saveBinaryImgPixels()
    {
        savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/BinaryImgPixels.png"), binaryImgPixels, inputFrameWidth, inputFrameHeight);
    }

    /*******************************************************************************
     * Function Name  : saveErosionImgPixelsAsBmp()
     * Description    : Save the erosion image pixel array as bmp
     *******************************************************************************/
    public void saveErosionImgPixelsAsBmp()
    {
        savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/ErosionImgPixels.png"),erosionImgPixels,inputFrameWidth,inputFrameHeight);
    }

    /*******************************************************************************
     * Function Name  : saveDilationImgPixelsAsBmp()
     * Description    : Save the erosion image pixel array as bmp
     *******************************************************************************/
    public void saveDilationImgPixelsAsBmp()
    {
        savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/DilationImgPixels.png"), dilationImgPixels, inputFrameWidth, inputFrameHeight);
    }

    /*******************************************************************************
     * Function Name  : saveDilationImgPixelsAsBmp()
     * Description    : Save the erosion image pixel array as bmp
     *******************************************************************************/
    public void saveImgWithInterestRegionsAsBmp(RectangleData[] rectangle)
    {
        Bitmap outputImg = Bitmap.createBitmap(inputFrameWidth,inputFrameHeight, Bitmap.Config.ARGB_8888);

        for(int j = 0; j < inputFrameHeight; j++)
        {
            for(int i = 0; i < inputFrameWidth; i++)
            {
                outputImg.setPixel(i,j,inputFramePixels[i+j*inputFrameWidth]);
            }
        }

        Canvas canvas = new Canvas(outputImg);
        Paint paint = new Paint();
        canvas.setBitmap(outputImg);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        System.out.println("RectangleData.getCount() = " + RectangleData.getCount());

        for(int i = 1; i < RectangleData.getCount(); i++)
        {
            if(((rectangle[i].xMax - rectangle[i].xMin) > 25) && ((rectangle[i].yMax - rectangle[i].yMin) > 25))
            {
                canvas.drawRect((float)(rectangle[i].xMin - 1), (float)(inputFrameHeight - rectangle[i].yMax - 2), (float)(rectangle[i].xMax + 1), (float)(inputFrameHeight - rectangle[i].yMin),paint);
            }
        }

        saveBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/ImgWithInterestRegions.png"), outputImg);
    }

    /*******************************************************************************
     * Function Name  : saveInputFramePixelsAsBmp()
     * Description    : Save the input frame pixel array as bmp
     *******************************************************************************/
    public void saveInputFramePixelsAsBmp()
    {
        savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/InputFramePixel.png"), inputFramePixels, inputFrameWidth, inputFrameHeight);
    }

    /*******************************************************************************
     * Function Name  : loadInputImageFromBmp()
     * Description    : Store the input image as a int vector ARGB
     *******************************************************************************/
    public void loadInputImageFromBmp(File fileName)
    {
        // Read the bmp image
        Bitmap bmp = BitmapFactory.decodeFile(fileName.getAbsolutePath());

        // Get the image width
        inputFrameWidth = bmp.getWidth();

        // Get the image height
        inputFrameHeight = bmp.getHeight();

        // Image pixels int array
        inputFramePixels = new int[inputFrameWidth*inputFrameHeight];

        // Put the image pixels inside a int array
        for(int j = 0; j < inputFrameHeight; j++)
        {
            for(int i = 0; i < inputFrameWidth; i++)
            {
                inputFramePixels[i + j*inputFrameWidth] = bmp.getPixel(i,j);
            }
        }
    }

    /*******************************************************************************
     * Function Name  : findInterestRegions()
     * Description    : Find regions of interest in the input image, that is, find
     regions that can be a traffic sign.
     *******************************************************************************/
    public RectangleData[] findInterestRegions()
    {
        // Auxiliary variables
        int red;
        int green;
        int blue;
        int whitePixel = 0xFFFFFFFF;
        int blackPixel = 0xFF000000;
        int hue = 0;

        // Create a new reference to the binary image.
        binaryImgPixels = new int[inputFrameWidth*inputFrameHeight];

        for(int j = 0; j < inputFrameHeight; j++)
        {
            int offset = j*inputFrameWidth;
            for(int i = 0; i < inputFrameWidth; i++)
            {
                int index = i + j*inputFrameWidth;

                if(j == (inputFrameHeight-1) || j == 0 || i == 0 || i == (inputFrameWidth-1))
                {
                    binaryImgPixels[index] = blackPixel;
                    continue;
                }

                int pixelColor = inputFramePixels[index];

                red = (char)((pixelColor >> 16) & 0xff);
                green = (char)((pixelColor >>  8) & 0xff);
                blue = (char)((pixelColor      ) & 0xff);

                if(red == green && green == blue)
                {
                    hue = 0;
                }
                else if(red >= green && red >= blue)
                {
                    hue = (((green - blue)*60)/(Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue))))%360;

                    if(hue < 0) hue = hue + 360;
                }
                else if(green >= red && green >= blue)
                {
                    hue = (((blue - red)*60)/(Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)))) + 120;
                }
                else if(blue >= red && blue >= green)
                {
                    hue = (((red - green)*60)/(Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)))) + 240;
                }

                float s = 1.0f - (3.0f*(float)(Math.min(red, Math.min(green, blue)))*(1.0f/(float)(red + green + blue)));

                if((hue <= 10 || hue >= 300) && (s> 0.08 && s < 1.0))
                {
                    // Set the pixels of the interest regions to white.
                    binaryImgPixels[index] =  whitePixel;
                }
                else
                {
                    // Set to black the pixels that aren't in the interest regions.
                    binaryImgPixels[index] =  blackPixel;
                }
            }
        }

        // Apply the erosion filter
        erosion();

        // Apply the dilation filter
        dilation();

        // Apply labeling
        return labeling();
    }

    /*******************************************************************************
     * Function Name  : erosion()
     * Description    : Perform the erosion mophological filtering
     *******************************************************************************/
    private void erosion()
    {
        // Auxiliary variables
        char red;
        int pixelLeft;
        int pixelUp;
        int pixelRight;
        int pixelBottom;
        int whitePixel = 0xFFFFFFFF;
        int blackPixel = 0xFF000000;

        // Target image width
        int w = inputFrameWidth;

        // Target image height
        int h = inputFrameHeight;

        // Create a new reference to the erosion image.
        erosionImgPixels = new int[inputFrameWidth*inputFrameHeight];

        for(int j = 0; j < h; j++)
        {
            for(int i = 0; i < w; i++)
            {
                if(j == (h-1) || j == 0 || i == 0 || i == (w-1))
                {
                    erosionImgPixels[i + j*inputFrameWidth] = blackPixel;
                    continue;
                }

                // Pixel left
                if(i > 0)
                {
                    int pixelColor = binaryImgPixels[i-1 + j*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelLeft = red;
                }
                else
                {
                    pixelLeft = 0;
                }

                // Pixel right
                if(i < w)
                {
                    int pixelColor = binaryImgPixels[i+1 + j*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelRight = red;
                }
                else
                {
                    pixelRight = 0;
                }

                // Pixel up
                if(j > 0)
                {
                    int pixelColor = binaryImgPixels[i + (j-1)*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelUp = red;
                }
                else
                {
                    pixelUp = 0;
                }

                // Pixel bottom
                if(j < h)
                {
                    int pixelColor = binaryImgPixels[i+1 + (j+1)*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelBottom = red;
                }
                else
                {
                    pixelBottom = 0;
                }

                if(pixelLeft == 255 && pixelRight == 255 && pixelUp == 255 && pixelBottom == 255)
                {
                    // Set the pixel to white
                    erosionImgPixels[i + j*inputFrameWidth] =  whitePixel;
                }
                else
                {
                    // Set the pixel to black
                    erosionImgPixels[i + j*inputFrameWidth] =  blackPixel;
                }
            }
        }
    }

    /*******************************************************************************
     * Function Name  : dilation()
     * Description    : Perform the erosion mophological filtering
     *******************************************************************************/
    private void dilation()
    {
        // Auxiliary variables
        char red;
        int pixelLeft;
        int pixelUp;
        int pixelRight;
        int pixelBottom;
        int whitePixel = 0xFFFFFFFF;
        int blackPixel = 0xFF000000;

        // Target image width
        int w = inputFrameWidth;

        // Target image height
        int h = inputFrameHeight;

        // Create a new reference to the erosion image.
        dilationImgPixels = new int[inputFrameWidth*inputFrameHeight];

        for(int j = 0; j < h; j++)
        {
            for(int i = 0; i < w; i++)
            {
                if(j == (h-1) || j == 0 || i == 0 || i == (w-1))
                {
                    dilationImgPixels[i + j*inputFrameWidth] =  blackPixel;
                    continue;
                }

                // Pixel left
                if(i > 0)
                {
                    int pixelColor = erosionImgPixels[i-1 + j*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelLeft = red;
                }
                else
                {
                    pixelLeft = 0;
                }

                // Pixel right
                if(i < w)
                {
                    int pixelColor = erosionImgPixels[i+1 + j*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelRight = red;
                }
                else
                {
                    pixelRight = 0;
                }

                // Pixel up
                if(j > 0)
                {
                    int pixelColor = erosionImgPixels[i + (j-1)*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelUp = red;
                }
                else
                {
                    pixelUp = 0;
                }

                // Pixel bottom
                if(j < h)
                {
                    int pixelColor = erosionImgPixels[i+1 + (j+1)*inputFrameWidth];
                    red = (char)((pixelColor >> 16) & 0xff);
                    pixelBottom = red;
                }
                else
                {
                    pixelBottom = 0;
                }

                if(pixelLeft == 255 || pixelRight == 255 || pixelUp == 255 || pixelBottom == 255)
                {
                    // Set the pixel to white
                    dilationImgPixels[i + j*inputFrameWidth] =  whitePixel;
                }
                else
                {
                    // Set the pixel to black
                    dilationImgPixels[i + j*inputFrameWidth] =  blackPixel;
                }
            }
        }
    }

    /*******************************************************************************
     * Function Name  : labeling()
     * Description    : Label the image to determine rectangles that represents the
     areas of interest.
     *******************************************************************************/
    public RectangleData[] labeling()
    {
        // Labels' vector
        int[] label = new int[1000];

        // Rectangles' vector
        RectangleData[] rectangle = new RectangleData[1000];

        RectangleData.resetCount();

        // Image width
        int w = inputFrameWidth;

        // Image height
        int h = inputFrameHeight;

        // Auxiliary variables
        int red;
        int green;
        int blue;

        // Neighbors pixels
        int pixelLeft;
        int pixelBottomLeft;
        int pixelBottom;
        int pixelRight;
        int pixelBottomRight;

        int pixelColor;

        // Label count
        int labelCount = 1;

        for(int j = h-1; j > 0; j--)
        {
            for(int i = 1; i < w-1; i++)
            {
                pixelColor = dilationImgPixels[i+j*w];
                red = (char)((pixelColor >> 16) & 0xff);
                green = (char)((pixelColor >>  8) & 0xff);
                blue = (char)((pixelColor      ) & 0xff);

                // If we have found an active pixel...
                if(red == 255 && green == 255 && blue == 255)
                {
                    // Check if the pixel's neighbors have labels.

                    // Pixel Left
                    pixelColor = dilationImgPixels[i-1+j*w];
                    red = (char)((pixelColor >> 16) & 0xff);
                    green = (char)((pixelColor >>  8) & 0xff);
                    blue = (char)((pixelColor      ) & 0xff);
                    if(red == 255 && green == 255 && blue == 255) pixelLeft = 0;
                    else pixelLeft = (blue << 16) + (green << 8) + red;

                    // Pixel BottomLeft
                    pixelColor = dilationImgPixels[i-1+(j+1)*w];
                    red = (char)((pixelColor >> 16) & 0xff);
                    green = (char)((pixelColor >>  8) & 0xff);
                    blue = (char)((pixelColor      ) & 0xff);
                    if(red == 255 && green == 255 && blue == 255) pixelBottomLeft = 0;
                    else pixelBottomLeft = (blue << 16) + (green << 8) + red;

                    // Pixel Bottom
                    pixelColor = dilationImgPixels[i+(j+1)*w];
                    red = (char)((pixelColor >> 16) & 0xff);
                    green = (char)((pixelColor >>  8) & 0xff);
                    blue = (char)((pixelColor      ) & 0xff);
                    if(red == 255 && green == 255 && blue == 255) pixelBottom = 0;
                    else pixelBottom = (blue << 16) + (green << 8) + red;

                    // Pixel BottomRight
                    pixelColor = dilationImgPixels[i+1+(j+1)*w];
                    red = (char)((pixelColor >> 16) & 0xff);
                    green = (char)((pixelColor >>  8) & 0xff);
                    blue = (char)((pixelColor      ) & 0xff);
                    if(red == 255 && green == 255 && blue == 255) pixelBottomRight = 0;
                    else pixelBottomRight = (blue << 16) + (green << 8) + red;

                    // Verify if the neighbors pixels have already been labeld
                    if(pixelLeft != 0 || pixelBottomLeft != 0 || pixelBottom != 0 || pixelBottomRight != 0)
                    {
                        int[] num = new int[4];
                        num[0] = pixelLeft;
                        num[1] = pixelBottomLeft;
                        num[2] = pixelBottom;
                        num[3] = pixelBottomRight;

                        if(pixelLeft == 0) num[0] = 0x0FFFFFFF;
                        if(pixelBottomLeft == 0) num[1] = 0x0FFFFFFF;
                        if(pixelBottom == 0) num[2] = 0x0FFFFFFF;
                        if(pixelBottomRight == 0) num[3] = 0x0FFFFFFF;

                        // The pixel has one or more previously labeled neighbors having the same label. In this case, we label the
                        // current pixel to the lowers label.
                        int minLabel = Math.min(num[0], Math.min(num[1], Math.min(num[2], num[3])));

                        // Label the pixel
                        dilationImgPixels[i+j*w] = (0xFF << 24) + ((minLabel & 0xFF) << 16) + (((minLabel >> 8) & 0xFF) << 8) + ((minLabel >> 16) & 0xFF);

                        // Update the rectangle.
                        if(i > rectangle[label[minLabel]].xMax)
                        {
                            rectangle[label[minLabel]].xMax = i;
                        }
                        else if(i < rectangle[label[minLabel]].xMin)
                        {
                            rectangle[label[minLabel]].xMin = i;
                        }

                        int y = h-j-1;

                        if(y > rectangle[label[minLabel]].yMax)
                        {
                            rectangle[label[minLabel]].yMax = y;
                        }
                        else if(y < rectangle[label[minLabel]].yMin)
                        {
                            rectangle[label[minLabel]].yMin = y;
                        }

                        // If the neighbors pixels already have lebel, we check the three spacial case that may indicate that
                        // the pixel is part of two diferent lables.
                        if((pixelBottomLeft != 0 && pixelBottomRight != 0) || (pixelLeft != 0 && pixelBottomRight != 0) || (pixelLeft != 0 && pixelBottomLeft != 0 && pixelBottomRight != 0))
                        {
                            // We have found a case where the actual pixel is part of two diferent lables. We set the current pixel to the lowest label and update the rectangle's coordinate.
                            Arrays.sort(num);

                            for(int k = 0; k < 4; k++)
                            {
                                if(num[k] != minLabel && num[k] < 10000000)
                                {
                                    // Update the rectangle.
                                    if(rectangle[label[num[k]]].xMax > rectangle[label[minLabel]].xMax)
                                    {
                                        rectangle[label[minLabel]].xMax = rectangle[label[num[k]]].xMax;
                                    }
                                    else if(rectangle[label[num[k]]].xMin < rectangle[label[minLabel]].xMin)
                                    {
                                        rectangle[label[minLabel]].xMin = rectangle[label[num[k]]].xMin;
                                    }

                                    if(rectangle[label[num[k]]].yMax > rectangle[label[minLabel]].yMax)
                                    {
                                        rectangle[label[minLabel]].yMax = rectangle[label[num[k]]].yMax;
                                    }
                                    else if(rectangle[label[num[k]]].yMin < rectangle[label[minLabel]].yMin)
                                    {
                                        rectangle[label[minLabel]].yMin = rectangle[label[num[k]]].yMin;
                                    }

                                    // Assign the rectangle of the lowest label
                                    label[num[k]] = label[minLabel];
                                }
                            }
                        }
                    }
                    else
                    {
                        // Label the pixel
                        dilationImgPixels[i+j*w] = (0xFF << 24) + ((labelCount & 0xFF) << 16) + (((labelCount >> 8) & 0xFF) << 8) + ((labelCount >> 16) & 0xFF);

                        // Assign a label
                        label[labelCount] = labelCount;

                        // Assign a rectangle
                        rectangle[labelCount] = new RectangleData();
                        int y = h-j-1;
                        rectangle[labelCount].xMin = i;
                        rectangle[labelCount].yMin = y;
                        rectangle[labelCount].xMax = 0;
                        rectangle[labelCount].yMax = 0;

                        // Update label counter
                        labelCount++;

                        if(labelCount == 1000)
                        {
                            //printf("Label overflow. Giving up \r\n");
                            System.out.println("Label overflow. Giving up");
                            return rectangle;
                        }
                    }
                }
            }
        }

        int []indexValidROI = new int[labelCount];
        int indexValidROICount = 0;

        for(int i = 1; i < labelCount; i++)
        {
            if(((rectangle[i].xMax - rectangle[i].xMin) > 25) && ((rectangle[i].yMax - rectangle[i].yMin) > 25))
            {
                indexValidROI[indexValidROICount] = i;
                indexValidROICount++;
            }
        }

        // Filter the size of the ROI
        RectangleData.resetCount();

        RectangleData[] roi = new RectangleData[indexValidROICount];

        for(int i = 0; i < indexValidROICount; i++)
        {
            roi[i] = new RectangleData(rectangle[indexValidROI[i]].xMin,rectangle[indexValidROI[i]].yMin,rectangle[indexValidROI[i]].xMax,rectangle[indexValidROI[i]].yMax);
        }

        return roi;
    }

    /*******************************************************************************
     * Function Name  : recognize()
     * Description    : Try to recognize a traffic sign within a region of interest
     *******************************************************************************/
    public void recognizeSignWithinRIO(RectangleData[] roi)
    {
        for(int r = 1; r < RectangleData.count; r++)
        {
            int roiWidth = roi[r].xMax-roi[r].xMin + 1;
            int roiHeight = roi[r].yMax - roi[r].yMin +1;

            int roiYmin = inputFrameHeight - roi[r].yMax - 1;
            int roiYmax = inputFrameHeight - roi[r].yMin;
            int roiXmin = roi[r].xMin;
            int roiXmax = roi[r].xMax;
            int indexRoiPixels = 0;
            int p =0 ,q = 0;

            int[] roiPixels = new int[(roiWidth)*(roiHeight)];

            int[] roiResizedPixels = new int[100*100];

            // Get the pixels of the region of interest
            for(int j = roiYmin ; j < roiYmax; j++, q++)
            {
                p = 0;
                for(int i = roi[r].xMin; i < roi[r].xMax; i++, p++)
                {
                    roiPixels[p+q*roiWidth] = inputFramePixels[i+j*inputFrameWidth];
                }
            }

            // DEBUG: Save the region of interest as bmp
            savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/ROI_" + r + ".png"), roiPixels, roiWidth, roiHeight);

            roiResizedPixels = resizeROI(roiPixels,roiWidth,roiHeight,100,100);

            // DEBUG: Save the region of interest as bmp
            savePixelArrayAsBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/ROI_SCALED" + r + ".png"), roiResizedPixels, 100, 100);
        }
    }

    /*******************************************************************************
     * Function Name  : resizeROI()
     * Description    : Resize the ROI
     *******************************************************************************/
    public int[] resizeROI(int[] pixels,int w1,int h1,int w2,int h2) {
        int[] temp = new int[w2*h2] ;
        // EDIT: added +1 to account for an early rounding problem
        int x_ratio = (int)((w1<<16)/w2) +1;
        int y_ratio = (int)((h1<<16)/h2) +1;
        //int x_ratio = (int)((w1<<16)/w2) ;
        //int y_ratio = (int)((h1<<16)/h2) ;
        int x2, y2 ;
        for (int i=0;i<h2;i++) {
            for (int j=0;j<w2;j++) {
                x2 = ((j*x_ratio)>>16) ;
                y2 = ((i*y_ratio)>>16) ;
                temp[(i*w2)+j] = pixels[(y2*w1)+x2] ;
            }
        }
        return temp ;
    }
}
