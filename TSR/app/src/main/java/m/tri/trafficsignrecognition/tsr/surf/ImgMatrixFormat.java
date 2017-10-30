package m.tri.trafficsignrecognition.tsr.surf;

/**
 * Created by evitorsi on 9/8/2017.
 */
public class ImgMatrixFormat {

    public double[][] ImgMatrix;

    // Image width
    public int width;

    // Image height
    public int height;

    public ImgMatrixFormat(int imgWidth, int imgHeight)
    {
        width = imgWidth;
        height = imgHeight;
        ImgMatrix = new double[imgWidth][imgHeight];
    }
}
