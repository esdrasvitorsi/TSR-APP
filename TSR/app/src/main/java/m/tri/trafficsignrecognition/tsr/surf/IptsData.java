package m.tri.trafficsignrecognition.tsr.surf;

/**
 * Created by evitorsi on 9/8/2017.
 */
public class IptsData {

    // X coordinate of the interest point
    public double x;

    // Y coordinate of the interest point
    public double y;

    // Scale of the interest point
    public double scale;

    // Laplacian of the interest point
    public int laplacian;

    // Orientation of the interest point
    public double orientation;

    // Description of the interest point
    public double[] descriptor = new double[64];

    // Number of interest point
    public int np;

    public IptsData()
    {

    }
}
