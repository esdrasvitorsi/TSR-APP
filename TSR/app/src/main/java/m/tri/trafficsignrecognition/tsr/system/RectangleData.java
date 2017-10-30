package m.tri.trafficsignrecognition.tsr.system;

/**
 * Created by evitorsi on 10/29/2017.
 */
public class RectangleData {

    // Xmin
    int xMin;

    // Ymin
    int yMin;

    // Xmax
    int xMax;

    // Ymax
    int yMax;

    static int count = 0;

    public RectangleData()
    {
        count++;
    }

    public RectangleData(int xMin, int yMin, int xMax, int yMax)
    {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;

        count++;
    }

    static public int getCount()
    {
        return count;
    }

    static public void resetCount()
    {
        count = 0;
    }
}
