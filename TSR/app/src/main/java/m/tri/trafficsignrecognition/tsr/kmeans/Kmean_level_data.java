package m.tri.trafficsignrecognition.tsr.kmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evitorsi on 10/21/2017.
 */
public class Kmean_level_data {
    // Centroids
    public double[][] centroids;

    // Clusters in the level
    public List<Cluster_data> clusters = new ArrayList<>();

    // Number of clusters in the level
    public int numClusters;

    public Kmean_level_data()
    {

    }
}
