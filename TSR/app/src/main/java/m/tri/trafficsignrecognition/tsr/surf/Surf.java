package m.tri.trafficsignrecognition.tsr.surf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.os.Environment;
import android.util.TimingLogger;

import java.io.File;

import m.tri.trafficsignrecognition.activity.PhotoDetectActivity;
import m.tri.trafficsignrecognition.tsr.system.TSRSystem;

import m.tri.trafficsignrecognition.tsr.kmeans.Kmeans;

/**
 * Created by evitorsi on 9/8/2017.
 */


public class Surf {

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    // Threshold
    private static final double thresh = 1.0000e-04;

    // Store the width of the image
    int imgWidth = 0;

    // Store the height of the image
    int imgHeight = 0;

    // Store the pixels of the input image
    int[] inputImage;

    // Store the reference to the gray scale image
    ImgMatrixFormat grayImage;

    // Store the reference to the integral image
    ImgMatrixFormat integralImage;

    // Hessian matrix
    double[][] H = new double[3][3];

    // Derivative matrix
    double[] D = new double[3];

    // Offset from interpolation
    double[] Of = new double[3];

    // Interest points. We create a 500 empty position to store the interest point.
    // If we need more space, more memory will be allocated latter.
    IptsData[] ipts = new IptsData[500];

    // Number of interest points
    int np;

    //! lookup table for 2d gaussian (sigma = 2.5) where (0,0) is top left and (6,6) is bottom right
    final double[][] gauss25 = {
            {0.02546481,	0.02350698,	0.01849125,	0.01239505,	0.00708017,	0.00344629,	0.00142946},
            {0.02350698,	0.02169968,	0.01706957,	0.01144208,	0.00653582,	0.00318132,	0.00131956},
            {0.01849125,	0.01706957,	0.01342740,	0.00900066,	0.00514126,	0.00250252,	0.00103800},
            {0.01239505,	0.01144208,	0.00900066,	0.00603332,	0.00344629,	0.00167749,	0.00069579},
            {0.00708017,	0.00653582,	0.00514126,	0.00344629,	0.00196855,	0.00095820,	0.00039744},
            {0.00344629,	0.00318132,	0.00250252,	0.00167749,	0.00095820,	0.00046640,	0.00019346},
            {0.00142946,	0.00131956,	0.00103800,	0.00069579,	0.00039744,	0.00019346,	0.00008024}
    };

    final double pi = 3.14159;

    PhotoDetectActivity photoDetectActivity;

    // Number of time that SURF has been running in a row
    static int count = 0;


    public Surf()
    {

    }

    public void extractFeatures(int[] inputImage, int imgWidth, int imgHeight) {

        TimingLogger timings = new TimingLogger("SurfExecutionTimeTag", "methodA");

        this.photoDetectActivity = photoDetectActivity;

        // Target image for SURF
        this.inputImage = inputImage;

        // Size of target image
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;

        // Gray image object
        grayImage = new ImgMatrixFormat(imgWidth,imgHeight);

        // Integral image object
        integralImage = new ImgMatrixFormat(imgWidth,imgHeight);

        // Convert the input image into gray scale
        timings.addSplit("Calcutate gray image");
        Surf_ColorToGray(grayImage, 0.2989, 0.5870, 0.1140);

        // Calculate the integral image of the input image
        timings.addSplit("Calcutate integral image");
        Surf_IntegralImage(integralImage, grayImage);

        // Get the interest points.
        timings.addSplit("Get interest points");
        FastHessian_getIpoints(1.0000e-04, 5, 2, integralImage);

        // Get descriptor for the interest points.
        timings.addSplit("Get descriptos");
        Surf_getDescriptors();

        // Plot the interest points over the image.
        timings.addSplit("Plot points");
        Surf_plotIpts();

        /*
        for(int p = 0; p < 64; p++)
        {
            System.out.println("ipts[50].descriptor[" + p + "] = " + ipts[50].descriptor[p]);
        }

        // Debug
        System.out.println("integralImage[100][100] = " + grayImage.ImgMatrix[100][100]);
        System.out.println("integralImage[100][100] = " + integralImage.ImgMatrix[100][100]);
        */

        //Kmeans Kmeans_obj = new Kmeans();
//
////        //TSRSystem.saveBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/InputImageFromSurf.png"), inputImage);
//
//        //timings.addSplit("Find interest regions");
//
//        String sdCardPath = "storage/sdcard1/TCC-TSR-2017/SURF implementation/pare3.bmp";
//        File f = new File(sdCardPath);
//        Bitmap bmpSD = BitmapFactory.decodeFile(f.getAbsolutePath());
//
//        tsrSystem.findInterestRegions(bmpSD);
//

        count++;

        timings.dumpToLog();
    }

    /****************************************************************************************
     * Function Name  : InputImage_getPixel()
     * Description    : Get the value of a channel of the pixel at position (x,y).
     : The image to be accessed is the input image.

     : if channel = RED : The function returns the red component of the pixel.
     : if channel = GREEN : The function returns the green component of the pixel.
     : if channel = BLUE : The function returns the blue component of the pixel.
     ****************************************************************************************/
    private int InputImage_getPixel(int x,int y,int channel)
    {
        // Get the color components from the original image
        int pixelColor = inputImage[x + y*imgWidth];

        int red = (pixelColor >> 16) & 0xff;
        int green = (pixelColor >>  8) & 0xff;
        int blue = (pixelColor      ) & 0xff;

        // Get the desired color component from the original image
        if(channel == RED)
        {
            return red;
        }
        else if(channel == GREEN)
        {
            return green;
        }
        else
        {
            return blue;
        }
    }

    /*******************************************************************************
     * Function Name  : SurfC_ColorToGray()
     * Description    : Convert the image into gray scale, taking into account specific
     : weights for each color. The weight value should be between
     : 0 and 1. The function returns a reference to a matrix where
     : the gray image was stored.
     *******************************************************************************/
    private void Surf_ColorToGray(ImgMatrixFormat img, double weightRed, double weightGreen, double weightBlue)
    {
        // Auxiliar variables
        int i, j;

        for(i = 0; i < imgWidth; i++)
        {
            for(j = 0; j < imgHeight; j++)
            {

                img.ImgMatrix[i][j] = ( ((weightRed * (double)InputImage_getPixel(i,j,RED))) +
                        ((weightGreen * (double)InputImage_getPixel(i,j,GREEN))) +
                        ((weightBlue * (double)InputImage_getPixel(i,j,BLUE))));
            }
        }
    }

    /*******************************************************************************
     * Function Name  : Surf_IntegralImage()
     * Description    : Calculate the integral image of a gray scale image
     *******************************************************************************/
    void Surf_IntegralImage(ImgMatrixFormat integralImg, ImgMatrixFormat grayImg)
    {
        // Auxiliar variables
        int i,j;

        // Initiate the element at (0,0) of the integral image.
        integralImage.ImgMatrix[0][0] = grayImg.ImgMatrix[0][0]/255.0;

        // Calculate the integral of the first line(y=0). The values from the scale image is normalized (e.g. divided by 255).
        for(i = 1; i < imgWidth; i++)
        {
            integralImage.ImgMatrix[i][0] = integralImage.ImgMatrix[i-1][0] + (grayImg.ImgMatrix[i][0])/255;
        }

        // Calculate the integral of the first column(y=0). The values from the scale image is normalized (e.g. divided by 255).
        for(j = 1; j < imgHeight; j++)
        {
            integralImage.ImgMatrix[0][j] = integralImage.ImgMatrix[0][j-1] + (grayImg.ImgMatrix[0][j])/255;
        }

        // Calculate the remaining integral of the input image. The values from the scale image is normalized (e.g. divided by 255).
        for(j = 1; j < imgHeight; j++)
        {
            for(i = 1; i < imgWidth; i++)
            {
                integralImage.ImgMatrix[i][j] = integralImage.ImgMatrix[i-1][j] + integralImage.ImgMatrix[i][j-1] -
                        integralImage.ImgMatrix[i-1][j-1] + (grayImg.ImgMatrix[i][j])/255;
            }
        }
    }

    /*******************************************************************************
     * Function Name  : BoxIntegral()
     * Description    : Evaluate the box integral. This function basically
     *				   calculate the area of the rectangle which points are
     *				   a = (r,c), b = (r,c+cols), c = (r + rows,c) and d = (r + rows, c + cols).
     *
     *	Returns:
     *
     *		The area of rectangle abcd
     *
     *******************************************************************************/
    double BoxIntegral(ImgMatrixFormat img, int row, int col, int rows, int cols)
    {
        // Get the corner coordinates of the box integral
        int y1 = Math.min(row, img.height) - 1;
        int x1 = Math.min(col, img.width) - 1;
        int y2 = Math.min(row + rows, img.height) - 1;
        int x2 = Math.min(col + cols, img.width) - 1;

        // Corners of the rectangle
        double A = 0;
        double B = 0;
        double C = 0;
        double D = 0;

        // Area of the rectangle
        double areaRectangle = 0;

        // Evaluate the integral image
        if(x1 >= 0 && y1 >= 0)
        {
            A = img.ImgMatrix[x1][y1];
        }
        if(x2 >= 0 && y1 >= 0)
        {
            B = img.ImgMatrix[x2][y1];
        }
        if(x1 >= 0 && y2 >= 0)
        {
            C = img.ImgMatrix[x1][y2];
        }
        if(x2 >= 0 && y2 >= 0)
        {
            D = img.ImgMatrix[x2][y2];
        }

        // Calculate the area of the rectangle
        areaRectangle = A - B - C + D;

        if(areaRectangle > 0)
        {
            return areaRectangle;
        }
        else
        {
            return 0;
        }

    }

    /*******************************************************************************
     * Function Name  : FastHessian_buildResponseLayer()
     * Description    : Evaluate the response of a layer of the response map.
     *				   Basically this response consists in evaluating the Hessian determinant,
     *				   which is given by (Dxx .* Dyy - 0.81 * Dxy .* Dxy). The Dxx, Dyy and Dxy
     *				   values are determined by evaluating the reponse of the image to the
     *				   approximated gaussian box filter. For this step, we take advantage of
     *				   the integral image to speed-up the calculation.
     *
     * Parameters:
     *
     *	responseLayer : Response layer that is to be calculated the response.
     *
     *
     *******************************************************************************/
    void FastHessian_buildResponseLayer(ResponseData responseLayer)
    {
        // Get the step for this layer
        int step = responseLayer.step;

        // Border for this filter
        int b = (responseLayer.filter - 1)/2;

        // Lobe for this filter (= (Filter size)/3)
        int l = (responseLayer.filter/3);

        // Filter size
        int w = responseLayer.filter;

        // Normalization factor
        double areaFilter = (double)(w*w);

        // Auxilixar variables to index the image (r=row,c=column)
        int r,c;

        // Auxiliar variables to scan the layer map dimensions
        int i, j;

        // Auxiliar variable o index the response result vector;
        int index = 0;

        // Store the response components
        double Dxx, Dyy, Dxy;

        // Evaluate the layer response
        for(i = 0; i < responseLayer.height; ++i)
        {
            for(j = 0; j < responseLayer.width; ++j)
            {
                // Get the image coordinates
                r = i * step;
                c = j * step;

                // Compute response components
                Dxx = BoxIntegral(integralImage,r-l+1,c-b,2*l-1,w) - BoxIntegral(integralImage,r-l+1,c-l/2,2*l-1,l)*3;

                Dyy = BoxIntegral(integralImage,r-b,c-l+1,w,2*l-1) - BoxIntegral(integralImage,r-l/2,c-l+1,l,2*l-1)*3;

                Dxy = BoxIntegral(integralImage,r-l,c+1,l,l) + BoxIntegral(integralImage,r+1,c-l,l,l) -
                    BoxIntegral(integralImage,r-l,c-l,l,l) - BoxIntegral(integralImage,r+1,c+1,l,l);


                //********* TEST *****
                //Dxy =  BoxIntegral(&integralImage,r+1,c+1,l,2*l-1);

                //Dxx = BoxIntegral(&integralImage,r-l+1,c-b,2*l-1,w);

                // printf("%.10f\n",Dyy);

                // ***** END TEST *****

                // Normalise the filter response

                //printf("Dyy = %.10f\n",Dyy);
                //printf("Dxx = %.10f\n",Dxx);
                //printf("Dxy = %.10f\n",Dxy);

                Dxx = Dxx/areaFilter;
                Dyy = Dyy/areaFilter;
                Dxy = Dxy/areaFilter;

                // Calculate the determinant of hessian response
                responseLayer.response[i * responseLayer.width + j] = Dxx * Dyy - 0.81 * Dxy * Dxy;

                // printf("Response: [%i] : %.10f\n",i * responseLayer->width + j + 1,responseLayer->response[i * responseLayer->width + j]);

                // Calculate the laplacian sign
                if(Dxx + Dyy >= 0)
                    responseLayer.laplacian[i * responseLayer.width + j] = 1;
                else
                    responseLayer.laplacian[i * responseLayer.width + j] = 0;

            }

        }

    }

    /*******************************************************************************
     * Function Name  : FastHessian_buildResponseMap()
     * Description    : Generate the response map layers
     *
     * Parameters:
     *
     *	width : Input image width.
     *
     *	height : Input image height.
     *
     *   init_sample: Refer to "responseMaap" structure on field "step" for more information about
     *	   this parameter.
     *
     * Returns:
     *
     *   responseMap : The reference to access the response maps.
     *
     *******************************************************************************/
    ResponseData[] FastHessian_buildResponseMap(double width, double height, int init_sample, int octaves)
    {
        // Calculate responses for the first 4 ( times octaves; for each octave, we calculate 4 response):
        // Oct1: 9,  15, 21, 27
        // Oct2: 15, 27, 39, 51
        // Oct3: 27, 51, 75, 99
        // Oct4: 51, 99, 147,195
        // Oct5: 99, 195,291,387


        // Obain the width and height of the first response map. They are obtained by dividing the original image
        // width and height by the initial sample (init_sample). The initial sample allow us to save computational
        // time by not evaluating the response of all pixels within the input image. Therefore, during
        // the filtering process, we move the mask init_sample to the left or the right. IN the case where
        // init_sample is 1, the response of all pixels of the image is evaluated. A typical value for
        // init_sample is 2.
        double w = ((double)imgWidth/(double)init_sample);
        double h = ((double)imgHeight/(double)init_sample);


        // Index variable
        int i = 0;

        // Get the step
        int s = init_sample;

        // Calculate the number of total response map layers that will be generated.
        int responseLayerNum = 4+2*(octaves-1);

        // Allocate memory to store the response maps
        ResponseData[] responseMap = new ResponseData[responseLayerNum];

        if(octaves >= 1)
        {
            // Create an empty response map layer for a filter size 9
            responseMap[i] = new ResponseData(w, h, s, 9);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 15
            responseMap[i] = new ResponseData(w, h, s, 15);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 21
            responseMap[i] = new ResponseData(w, h, s, 21);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 27
            responseMap[i] = new ResponseData(w, h, s, 27);
            // Move to the next response map layer;
            i++;
        }

        if(octaves >= 2)
        {
            // Create an empty response map layer for a filter size 39
            responseMap[i] = new ResponseData(w/2, h/2, s*2, 39);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 51
            responseMap[i] = new ResponseData(w/2, h/2, s*2, 51);
            // Move to the next response map layer;
            i++;
        }

        if(octaves >= 3)
        {
            // Create an empty response map layer for a filter size 75
            responseMap[i] = new ResponseData(w/4, h/4, s*4, 75);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 99
            responseMap[i] = new ResponseData(w/4, h/4, s*4, 99);
            // Move to the next response map layer;
            i++;
        }

        if(octaves >= 4)
        {
            // Create an empty response map layer for a filter size 147
            responseMap[i] = new ResponseData(w/8, h/8, s*8, 147);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 195
            responseMap[i] = new ResponseData(w/8, h/8, s*8, 195);
            // Move to the next response map layer;
            i++;
        }

        if(octaves >= 5)
        {
            // Create an empty response map layer for a filter size 291
            responseMap[i] = new ResponseData(w/16, h/16, s*16, 291);
            // Move to the next response map layer;
            i++;
            // Create an empty response map layer for a filter size 387
            responseMap[i] = new ResponseData(w/16, h/16, s*16, 387);
            // Move to the next response map layer;
            i++;
        }

        // Extract responses from the image
        for(i = 0; i < responseLayerNum; i++)
        {
            FastHessian_buildResponseLayer(responseMap[i]);
        }


        // ONLY FOR DEBUG
        return responseMap;
    }

    /*******************************************************************************
     * Function Name  : FastHessian_getLaplacian()
     * Description    : Get the laplacian of a response layer
     *
     *******************************************************************************/
    int FastHessian_getLaplacian(int row,int column, ResponseData a, ResponseData b)
    {
        int scale;

        scale = a.width/b.width;

        return a.laplacian[(scale * row) * a.width + (scale * column)];

    }

    /*******************************************************************************
     * Function Name  : FastHessian_solveLinSystem()
     * Description    : Solve the linear system the offset
     *
     *******************************************************************************/
    void FastHessian_solveLinSystem()
    {
        // Auxiliar variables
        int i;
        double temp;
        double pivot;
        double m;

        // Initialize the Offset vector
        Of[0] = 0;
        Of[1] = 0;
        Of[2] = 0;

        // Pick the element that has the greater magnitude to be the pivot

        if(Math.abs(H[0][0]) >= Math.abs(H[1][0]))
        {
            if(Math.abs(H[0][0]) >= Math.abs(H[2][0]))
            {
            }
            else
            {
                for(i = 0; i <= 2; i++)
                {
                    temp = H[0][i];
                    H[0][i] = H[2][i];
                    H[2][i] = temp;
                }
                temp = D[0];
                D[0] = D[2];
                D[2] = temp;
            }
        }
        else
        {
            if(Math.abs(H[1][0]) >= Math.abs(H[2][0]))
            {
                for(i = 0; i <= 2; i++)
                {
                    temp = H[0][i];
                    H[0][i] = H[1][i];
                    H[1][i] = temp;
                }
                temp = D[0];
                D[0] = D[1];
                D[1] = temp;
            }
            else
            {
                for(i = 0; i <= 2; i++)
                {
                    temp = H[0][i];
                    H[0][i] = H[2][i];
                    H[2][i] = temp;
                }
                temp = D[0];
                D[0] = D[2];
                D[2] = temp;
            }
        }

        // Consider the first element of the matrix as pivot
        pivot = H[0][0];
        // Divide the first line of the matrix H and D by the pivot
        H[0][1] = H[0][1]/pivot;
        H[0][2] = H[0][2]/pivot;
        D[0] = D[0]/pivot;

        // Eliminate the first element of the second line of the system
        H[0][0] = 1;
        m = H[1][0];
        H[1][0] = H[1][0] - m*H[0][0];
        H[1][1] = H[1][1] - m*H[0][1];
        H[1][2] = H[1][2] - m*H[0][2];
        D[1] = D[1] - m*D[0];

        // Eliminate the fisrt element of the third line of the system
        m = H[2][0];
        H[2][0] = H[2][0] - m*H[0][0];
        H[2][1] = H[2][1] - m*H[0][1];
        H[2][2] = H[2][2] - m*H[0][2];
        D[2] = D[2] - m*D[0];

        // Divide the first line of the matrix H and D by the pivot
        pivot = H[1][1];
        H[1][2] = H[1][2]/pivot;
        D[1] = D[1]/pivot;
        H[1][1] = 1;

        // Eliminate the second element of the third line of the system
        m = H[2][1];
        H[2][1] = H[2][1] - m*H[1][1];
        H[2][2] = H[2][2] - m*H[1][2];
        D[2] = D[2] - m*D[1];

        // Calculate Of(2)
        Of[2] = D[2]/H[2][2];

        // Calculate Of(1)
        Of[1] = D[1] - H[1][2]*Of[2];

        // Calculate Of(0)
        Of[0] = D[0] - H[0][1]*Of[1] - H[0][2]*Of[2];
    }

    /*******************************************************************************
     * Function Name  : FastHessian_BuildDerivative()
     * Description    : Build the derivative matrix D = [dx dy ds]
     *
     *******************************************************************************/
    double[] FastHessian_BuildDerivative(int r, int c, ResponseData t, ResponseData m, ResponseData b)
    {
        // Evaluate dx
        D[0] = (FastHessian_getResponse(r,c+1,m,t) - FastHessian_getResponse(r,c-1,m,t))/2;

        // Evaluate dy
        D[1] = (FastHessian_getResponse(r+1,c,m,t) - FastHessian_getResponse(r-1,c,m,t))/2;

        // Evaluate dz
        D[2] = (FastHessian_getResponse(r,c,t,t) - FastHessian_getResponse(r,c,b,t))/2;

        return D;
    }

    /*******************************************************************************
     * Function Name  : FastHessian_BuildHessian()
     * Description    : Build the Hessian matrix H = [dxx dyx dsx; dxy dyy dsy; dxs dys dss]
     *
     *******************************************************************************/
    double[][] FastHessian_BuildHessian(int r, int c, ResponseData t, ResponseData m, ResponseData b)
    {
        // Auxiliar variables
        int i;

        // Response of the central pixel
        double v;
        v = FastHessian_getResponse(r,c,m,t);

        // Build the Hessian matrix
        H[0][0] = FastHessian_getResponse(r,c+1,m,t) + FastHessian_getResponse(r,c-1,m,t) -2*v;		// dxx
        H[0][1] = (FastHessian_getResponse(r+1,c+1,m,t) - FastHessian_getResponse(r+1,c-1,m,t) -	// dxy
                FastHessian_getResponse(r-1,c+1,m,t) + FastHessian_getResponse(r-1,c-1,m,t))/4;
        H[0][2] = (FastHessian_getResponse(r,c+1,t,t) - FastHessian_getResponse(r,c-1,t,t) -		// dxs
                FastHessian_getResponse(r,c+1,b,t) + FastHessian_getResponse(r,c-1,b,t))/4;
        H[1][0] = H[0][1]; // dxy
        H[1][1] = FastHessian_getResponse(r+1,c,m,t) + FastHessian_getResponse(r-1,c,m,t) - 2*v;	// dyy
        H[1][2] = (FastHessian_getResponse(r+1,c,t,t) - FastHessian_getResponse(r-1,c,t,t) -		// dys
                FastHessian_getResponse(r+1,c,b,t) + FastHessian_getResponse(r-1,c,b,t))/4;
        H[2][0] = H[0][2];	// dxs
        H[2][1] = H[1][2];	// dys
        H[2][2] = FastHessian_getResponse(r,c,t,t) + FastHessian_getResponse(r,c,b,t) - 2*v;		// dss

        return H;
    }

    /*******************************************************************************
     * Function Name  : FastHessian_interpolateExtremum()
     * Description    : Interpolate interest points
     *
     *******************************************************************************/
    void FastHessian_interpolateExtremum(int r, int c, ResponseData t, ResponseData m, ResponseData b,IptsData[] ipts)
    {
        // Step distance between filters
        int filterStep;

        // Build the derivative matrix
        D = FastHessian_BuildDerivative(r,c,t,m,b);

        // Build the Hessian matrix
        H = FastHessian_BuildHessian(r,c,t,m,b);

        // ******** TEST ONLY ***************
	/*
	if(np >= 304)
	{
	printf("Ips[%d]: \n ",np);
	printf("D[0] = dxx = %.10f\n",D[0]);
	printf("D[1] = dxx = %.10f\n",D[1]);
	printf("D[2] = dxx = %.10f\n\n",D[2]);

	printf("H[0][0] = dxx = %.10f\n",H[0][0]);
	printf("H[0][1] = dxy = %.10f\n",H[0][1]);
	printf("H[0][2] = dxs = %.10f\n",H[0][2]);
	printf("H[1][0] = dxx = %.10f\n",H[1][0]);
	printf("H[1][1] = dxy = %.10f\n",H[1][1]);
	printf("H[1][2] = dxs = %.10f\n",H[1][2]);
	printf("H[2][0] = dxx = %.10f\n",H[2][0]);
	printf("H[2][1] = dxy = %.10f\n",H[2][1]);
	printf("H[2][2] = dxs = %.10f\n\n",H[2][2]);

	}
	*/
        // ********* END ******************

        // Get the offsets from the interpolation
        // Solve the linear system Of = H\D
        FastHessian_solveLinSystem();

        // Multiply Of by -1 so that we obtain Of = -H\D
        Of[0] = Of[0]*(-1);
        Of[1] = Of[1]*(-1);
        Of[2] = Of[2]*(-1);

        // ******** TEST ONLY ***************
	/*
	if(np >= 304)
	{
		printf("Of[0] = %.10f\n",Of[0]);
		printf("Of[1] = %.10f\n",Of[1]);
		printf("Of[2] = %.10f\n",Of[2]);
		printf(" ");
	}
	*/
        // *********** END TEST **************

        // Get the step distance between filters
        filterStep = m.filter - b.filter;

        // If point is sufficiently close to the actual extremum
        if((Math.abs(Of[0]) < 0.5) && (Math.abs(Of[1]) < 0.5) && (Math.abs(Of[2]) < 0.5))
        {
            if(np >= 500)
            {
                // FastHessian_extendIptsBuffer(ipts,np);
                System.out.println("/n/n*** Critical Warning: ipts buffer full. Some interesting points is being lost.***/n/n");
                return;
            }
            ipts[np] = new IptsData();

            ipts[np].x = ((c + Of[0])*t.step);
            ipts[np].y =  ((r + Of[1])*t.step);
            ipts[np].scale = ((2.0/15.0)*(m.filter + Of[2]*filterStep));
            ipts[np].laplacian = FastHessian_getLaplacian(r,c,m,t);

//
//
//            // ******** TEST ONLY ***************
//            //if(np >= 304 && np < 350)
//            //{
            System.out.println("ipts[" + np + "].x = " + ipts[np].x);
            System.out.println("ipts[" + np + "].y = " + ipts[np].y);
            System.out.println("ipts[" + np + "].scale = " + ipts[np].scale);
            System.out.println("ipts[" + np + "].laplacian = " + ipts[np].laplacian);
//            //printf("Size of ipts = %d",sizeof(ipts)/sizeof(iptsData));
//            //}

            // ********* END ******************
            np++;
        }
    }

    /*******************************************************************************
     * Function Name  : FastHessian_getResponse()
     * Description    : Get the hessian determinant response of a response layer
     *
     *******************************************************************************/
    double FastHessian_getResponse(int row,int column, ResponseData a,ResponseData b)
    {
        int scale;

        scale = a.width/b.width;

        return a.response[(scale * row) * a.width + (scale * column)];
    }

    /*******************************************************************************
     * Function Name  : FastHessian_isExtremum()
     * Description    : Verify if a target pixel is extrema by analyzing its 26 neighbors
     *				   (9 on the top layer, 9 on the bottom layer, and 8 on its surroundins)
     *
     *******************************************************************************/
    boolean FastHessian_isExtremum(int r, int c, ResponseData t, ResponseData m, ResponseData b)
    {
        int layerBorder;
        double candidate;
        int rr,cc;

        // Bounds check
        layerBorder = (t.filter + 1)/(2 * t.step);

        if(r <= layerBorder || r >= t.height - layerBorder || c <= layerBorder || c >= t.width - layerBorder)
        {
            return false;
        }

        // Check the candidate point in the middle layer is above thresh
        candidate = FastHessian_getResponse(r,c,m,t);
        if(candidate < thresh)
            return false;

        for(rr = -1; rr <=1; ++rr)
        {
            for(cc = -1; cc <=1; ++cc)
            {
                // If any response in 3x3x3 is greater than candidate, so it is not maximum
                if(FastHessian_getResponse(r + rr,c + cc,t,t)>= candidate ||
                        ((rr != 0 || cc != 0) && (FastHessian_getResponse(r+rr,c+cc,m,t)>= candidate)) ||
                        FastHessian_getResponse(r+rr,c+cc,b,t)>= candidate)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /*******************************************************************************
     * Function Name  : FastHessian_getIpoints()
     * Description    : Get the interesting points of the input image
     *
     * Parameters:
     *
     *	thresh : Threshould used as a criteria to select extrema during the search for
     *		     extrema process. Only pixels with intensity above the threshould will be
     *		     considered a candidate for extrema point.
     *
     *	octave : Number of octaves to be generated
     *
     *   init_sample: Refer to "responseMaap" structure on field "step" for more information about
     *				 this parameter.
     *
     *
     *   img : Integral image
     *
     * Returns:
     *
     *	Returns the interesting points.
     *
     *******************************************************************************/
    void FastHessian_getIpoints(double thresh,int octaves,int init_sample,ImgMatrixFormat img)
    {
        // filter index map (5 octaves and 4 intervals)
        final int filter_map [][] = {{0,1,2,3}, {1,3,4,5}, {3,5,6,7}, {5,7,8,9}, {7,9,10,11}};

        // Number of interest points
        np = 0;

        // Variables used to store temporarily the bottom (b), top (t) and middle (m) response layer
        ResponseData b,t,m;

        // Auxiliar index variables
        int o,i,r,c;

        int testNumP = 0;

        // Generate empty the response maps.
        ResponseData[] responseMap = FastHessian_buildResponseMap(imgWidth,imgHeight,init_sample,octaves);

        /*
        t = responseMap[filter_map[0][2]];

        for(int k = 0; k < t.height; k++)
        {
            for(int p = 0; p < t.width; p++)
            {
                int index = k * t.height + p + 1;

                System.out.println("Response : [" + index + "] = " + t.response[k * t.height + p]);
            }
        }
        */

        // Find the maxima accros scale and space
        for(o = 0; o < octaves; ++o)
        {
            for(i = 0; i < 2; ++i)
            {
                b = responseMap[filter_map[o][i]];
                m = responseMap[filter_map[o][i+1]];
                t = responseMap[filter_map[o][i+2]];

                // ****** TEST ONLY *********
                //FastHessian_isExtremum(19,76,&t,&m,&b);

                // **************************

			/*
			for(int k = 0; k < t.height; k++)
			{
				for(int p = 0; p < t.width; p++)
				{
					printf("Response : [%d] = %.8f \n",k*t.height + p + 1,t.response[k*b.height + p]);
				}
			}
			*/

                // loop over middle response layer at density of the most
                // sparse layer (always top), to find maxima across scale and space
                for(r = 0; r < t.height; ++r)
                {
                    for(c = 0; c < t.width; ++c)
                    {
                        if(FastHessian_isExtremum(r,c,t,m,b))
                        {
                            testNumP++;
                            FastHessian_interpolateExtremum(r,c,t,m,b,ipts);

                            //printf("Extremum found at r = %d and c = %d\n",r,c);
                            //printf("Extremum value = %.8f \n\n",m.response[r*t.height + c]);
                            //printf("Extremum [%d] (r = %d   c = % d )found at index [%d]\n",testNumP,r,c,r*t.height + c+1);
                        }
                    }
                }
                //printf("P = %d\n",testNumP);
                //testNumP = 0;

            }
        }
        System.out.println("P = " + testNumP);
        System.out.println("np = " + np);
    }

    /*******************************************************************************
     * Function Name  :  Surf_HaarX()
     * Description    : Calculate Haar wavelet responses in x direction
     *******************************************************************************/
    double Surf_HaarX(ImgMatrixFormat img,int row, int column, int s)
    {
        return BoxIntegral(img, row-s/2, column, s, s/2)
                -1 * BoxIntegral(img, row-s/2, column-s/2, s, s/2);
    }

    /*******************************************************************************
     * Function Name  :  Surf_HaarY()
     * Description    : Calculate Haar wavelet responses in y direction
     *******************************************************************************/
    double Surf_HaarY(ImgMatrixFormat img, int row, int column, int s)
    {
        return BoxIntegral(img, row, column-s/2, s/2, s)
                -1 * BoxIntegral(img, row-s/2, column-s/2, s/2, s);
    }

    /*******************************************************************************
     * Function Name  :  SurfC_getAngle()
     * Description    :  Get the angle from the +ve x-axis of the vector given by (X Y)
     *******************************************************************************/
    double SurfC_getAngle(float X, float Y)
    {
        if(X > 0 && Y >= 0)
            return Math.atan(Y/X);

        if(X < 0 && Y >= 0)
            return pi - Math.atan(-Y / X);

        if(X < 0 && Y < 0)
            return pi + Math.atan(Y / X);

        if(X > 0 && Y < 0)
            return 2*pi - Math.atan(-Y / X);

        return 0;
    }

    /*******************************************************************************
     * Function Name  : Surf_getOrientation()
     * Description    : Get the orientation of the interest point
     *******************************************************************************/
    double Surf_getOrientation(int index)
    {
        IptsData ipt = ipts[index];
        double gauss = 0.f;
        double scale = ipt.scale;
        final int s = (int)Math.round(scale);
        final int r = (int)Math.round(ipt.y);
        final int c = (int)Math.round(ipt.x);
        double[] resX = new double[109];
        double[] resY = new double[109];
        double[] Ang = new double[109];
        final int id[] = {6,5,4,3,2,1,0,1,2,3,4,5,6};

        int idx = 0;

        // calculate haar responses for points within radius of 6*scale
        for(int i = -6; i <= 6; ++i)
        {
            for(int j = -6; j <= 6; ++j)
            {
                if(i*i + j*j < 36)
                {
                    gauss = (double)(gauss25[id[i+6]][id[j+6]]);
                    resX[idx] = gauss * Surf_HaarX(integralImage,r+j*s, c+i*s, 4*s);
                    resY[idx] = gauss * Surf_HaarY(integralImage,r+j*s, c+i*s, 4*s);
                    Ang[idx] = SurfC_getAngle((float)resX[idx], (float)resY[idx]);
                    ++idx;
                }
            }
        }

        // calculate the dominant direction
        double sumX=0.0;
        double sumY=0.0;
        double max=0.0;
        double orientation = 0.0;
        double ang1=0.0;
        double ang2=0.0;
        double ang;

        // loop slides pi/3 window around feature point
        for(ang1 = 0; ang1 < 2*pi;  ang1+=0.15f) {
            ang2 = ( ang1+pi/3.0f > 2*pi ? ang1-5.0f*pi/3.0f : ang1+pi/3.0f);
            sumX = sumY = 0.f;
            for(int k = 0; k < 109; ++k)
            {
                // get angle from the x-axis of the sample point
                ang = Ang[k];

                // determine whether the point is within the window
                if (ang1 < ang2 && ang1 < ang && ang < ang2)
                {
                    sumX+=resX[k];
                    sumY+=resY[k];
                }
                else if (ang2 < ang1 &&
                        ((ang > 0 && ang < ang2) || (ang > ang1 && ang < 2*pi) ))
                {
                    sumX+=resX[k];
                    sumY+=resY[k];
                }
            }

            // if the vector produced from this window is longer than all
            // previous vectors then this forms the new dominant direction
            if (sumX*sumX + sumY*sumY > max)
            {
                // store largest orientation
                max = sumX*sumX + sumY*sumY;
                orientation = SurfC_getAngle((float)sumX, (float)sumY);
            }
        }

        // assign orientation of the dominant response vector
        ipt.orientation = orientation;

        // ONLY FOR TEST
        return 0;
    }

    /*******************************************************************************
     * Function Name  : gaussian_int()
     * Description    : Calculate the value of the 2d gaussian at x,y
     *******************************************************************************/
    double gaussian_int(int x, int y, double sig)
    {
        return (1.0/(2.0*pi*sig*sig)) * Math.exp(-(x * x + y * y) / (2.0 * sig * sig));
    }

    /*******************************************************************************
     * Function Name  : gaussian_int()
     * Description    : Calculate the value of the 2d gaussian at x,y
     *******************************************************************************/
    double gaussian_float(double x, double y, double sig)
    {
        return (1.0/(2.0*pi*sig*sig)) * Math.exp(-(x * x + y * y) / (2.0 * sig * sig));
    }

    /*******************************************************************************
     * Function Name  : Surf_getDescriptor()
     * Description    : Get the modified descriptor. See Agrawal ECCV 08
     *                  Modified descriptor contributed by Pablo Fernandez
     *******************************************************************************/
    void Surf_getDescriptor(boolean bUpright,int index)
    {
        int y, x, sample_x, sample_y, count=0;
        int i = 0, ix = 0, j = 0, jx = 0, xs = 0, ys = 0;
        double [] desc;
        double scale, dx, dy, mdx, mdy, co, si;
        double gauss_s1 = 0.f, gauss_s2 = 0.f;
        double rx = 0.f, ry = 0.f, rrx = 0.f, rry = 0.f, len = 0.f;
        double cx = -0.5f, cy = 0.f; //Subregion centers for the 4x4 gaussian weighting

        IptsData ipt = ipts[index];
        scale = ipt.scale;
        x = (int)Math.round(ipt.x);
        y = (int)Math.round(ipt.y);
        desc = ipt.descriptor;

        if (bUpright)
        {
            co = 1;
            si = 0;
        }
        else
        {
            co = Math.cos(ipt.orientation);
            si =  Math.sin(ipt.orientation);
        }

        i = -8;

        //Calculate descriptor for this interest point
        while(i < 12)
        {
            j = -8;
            i = i-4;

            cx += 1.f;
            cy = -0.5f;

            while(j < 12)
            {
                dx=dy=mdx=mdy=0.f;
                cy += 1.f;

                j = j - 4;

                ix = i + 5;
                jx = j + 5;

                xs = (int)Math.round(x + ( -jx*scale*si + ix*scale*co));
                ys = (int)Math.round(y + (jx * scale * co + ix * scale * si));

                for (int k = i; k < i + 9; ++k)
                {
                    for (int l = j; l < j + 9; ++l) {
                        //Get coords of sample point on the rotated axis
                        sample_x = (int) Math.round(x + (-l*scale*si + k*scale*co));
                        sample_y = (int)Math.round(y + ( l*scale*co + k*scale*si));

                        //Get the gaussian weighted x and y responses
                        gauss_s1 = gaussian_int(xs - sample_x, ys - sample_y, 2.5f * scale);
                        rx = Surf_HaarX(integralImage, sample_y, sample_x, (int) (2 * Math.round(scale)));
                        ry = Surf_HaarY(integralImage,sample_y, sample_x, (int)(2*Math.round(scale)));

                        //Get the gaussian weighted x and y responses on rotated axis
                        rrx = gauss_s1*(-rx*si + ry*co);
                        rry = gauss_s1*(rx*co + ry*si);

                        dx += rrx;
                        dy += rry;
                        mdx += Math.abs(rrx);
                        mdy += Math.abs(rry);
                    }
                }

                //Add the values to the descriptor vector
                gauss_s2 = gaussian_float(cx-2.0f,cy-2.0f,1.5f);

                desc[count++] = dx*gauss_s2;

                // printf("\n Ipts[0].Descriptor[%d] = %.10f \n",count - 1,desc[count - 1]);

                desc[count++] = dy*gauss_s2;

                // printf("\n Ipts[0].Descriptor[%d] = %.10f \n",count - 1,desc[count - 1]);

                desc[count++] = mdx*gauss_s2;

                // printf("\n Ipts[0].Descriptor[%d] = %.10f \n",count - 1,desc[count - 1]);

                desc[count++] = mdy*gauss_s2;

                //printf("\n Ipts[0].Descriptor[%d] = %.10f \n",count - 1,desc[count - 1]);

                len += (dx*dx + dy*dy + mdx*mdx + mdy*mdy) * gauss_s2*gauss_s2;

                j += 9;
            }
            i += 9;
        }

        //Convert to Unit Vector
        len = Math.sqrt(len);
        for(i = 0; i < 64; ++i)
            desc[i] /= len;

        //for(int i = 0; i < 64; i++)
        //{
        //	  printf("\n Ipts[0].Descriptor[%i] = %.10f \n",i,desc[i]);
        //}

    }

    /*******************************************************************************
     * Function Name  : Surf_getDescriptors()
     * Description    : Get the descriptors of the interest points
     *******************************************************************************/
    void Surf_getDescriptors()
    {
        // Check there are Ipoints to be described (np = number of interest points)
        if (np == 0) return;

        // Main SURF-64 loop assigns orientations and gets descriptors
        for (int i = 0; i < np; ++i)
        {
            // Assign Orientations and extract rotation invariant descriptors
            Surf_getOrientation(i);
            Surf_getDescriptor(false,i);
        }

        // FOR TESTING PURPOSE. Display the orientation of the interest points
        for(int i = 0; i < np; i++)
        {
            //System.out.println("IP.Orientation[" + i + "] = " + ipts[i].orientation);
        }
    }

    /*******************************************************************************
     * Function Name  : Surf_plotIpts()
     * Description    : Plot the interest points
     *******************************************************************************/
    void Surf_plotIpts()
    {
        Canvas canvas;

        Bitmap imageWithIpts = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(imageWithIpts);
        Paint paint = new Paint();

        for(int j = 0; j < imgHeight; j++ )
        {
            for(int i = 0; i < imgWidth; i++)
            {
                imageWithIpts.setPixel(i,j,inputImage[i+j*imgWidth]);
            }
        }

        canvas.setBitmap(imageWithIpts);

        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        for(int i = 0; i < np; i++)
        {
            canvas.drawCircle((int)ipts[i].x, (int)ipts[i].y, 2, paint);
        }

        TSRSystem.saveBmp(new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/Features_ROI_"+ count + ".png"),imageWithIpts);
    }

    /*******************************************************************************
     * Function Name  : resetCount()
     * Description    : Reset the surf counter that indicates the number of execution
     *                  in a row.
     *******************************************************************************/
    static public void resetCount()
    {
        count = 0;
    }
}