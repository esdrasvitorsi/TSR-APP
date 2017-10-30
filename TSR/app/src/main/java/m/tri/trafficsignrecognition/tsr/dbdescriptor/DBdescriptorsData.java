package m.tri.trafficsignrecognition.tsr.dbdescriptor;

/**
 * Created by evitorsi on 10/20/2017.
 */
public class DBdescriptorsData {

    // Points to the interest point in the database
    public double[] data;

    // Size
    public int size;

    // Rows
    public int rows;

    // Colunms
    public int cols;

    public DBdescriptorsData(int size,int rows)
    {
        data = new double[size];
        this.size = size;
        this.rows = rows;
    }

    public void setData(int index, double data)
    {
        this.data[index] = data;
    }
}
