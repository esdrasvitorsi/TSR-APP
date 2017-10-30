package m.tri.trafficsignrecognition.tsr.kmeans;

/**
 * Created by evitorsi on 10/21/2017.
 */
public class Cluster_data {

    // Centroid of the cluster
    public double[] centroid;

    // Tag for the points that belongs to the cluster
    public int[] points;

    // Number of points in the cluster
    public int numPoints;

    // Pointer to the bottom left cluster
    public Cluster_data[] bottomClusters;

    // Number of bottom clusters
    public int numBottomClusters;

    public Cluster_data() {
    }

    public void initPoints(int n, int[] data)
    {
        points = new int[n];

        for(int i = 0; i < n; i++)
        {
            points[i] = data[i];
        }

        numPoints = n;
    }
}


