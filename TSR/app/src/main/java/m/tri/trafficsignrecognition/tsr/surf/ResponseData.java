package m.tri.trafficsignrecognition.tsr.surf;

/**
 * Created by evitorsi on 9/8/2017.
 */
public class ResponseData {

    // Responde map width
    public int width;

    // Responde map height
    public int height;

    // Step. This is used to determine the step that we will make during the
    // box filtering process. In other words, 'step' determines the pass during
    // the box filtering process. For example, if step is 1, we will calculate
    // the response for every pixel within the input image, that is, we will move
    // the mask (in our case the mask of the approximated second order gaussian)
    // pixel by pixel. However if step is '4' for example, we will compute the
    // response of a given pixel and then jump the nearest 4 neighbor of the image.
    // Therefore, the next pixel to be evaluate its response is 4 pixel away from
    // the previous one. This technic is quite import since as the box filter size grows,
    // the response of the close neighbor of a given pixel will be very similar. Hence,
    // we can save both space and computational time.
    public int step;

    // Filter size used to obain the reponse map
    public int filter;

    // Vector that contains the values of the response. This response is the
    // Hessian determinant( Det(H) = (Dxx .* Dyy - 0.81 * Dxy .* Dxy)
    public double[] response;

    // Vector that contains the values of the Laplacian.
    public int[] laplacian;

    public ResponseData(double width, double height, int s, double filter)
    {
        // The dimension of the response map layer.
        this.width = (int)Math.floor(width);
        this.height = (int)Math.floor(height);

        // Size of the filter which the reponse map layer is to be generated.
        this.filter = (int)Math.floor(filter);

        // Step
        this.step = s;

        // Response data, which consistis of the Hessian determinant of each pixel that was processed.
        response = new double[this.width*this.height];

        // Lapacian data.
        laplacian = new int[this.width*this.height];
    }
}
