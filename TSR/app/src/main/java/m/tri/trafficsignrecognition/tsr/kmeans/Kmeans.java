package m.tri.trafficsignrecognition.tsr.kmeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import m.tri.trafficsignrecognition.tsr.dbdescriptor.DBdescriptorsData;
import m.tri.trafficsignrecognition.tsr.dbdescriptor.*;

/**
 * Created by evitorsi on 10/20/2017.
 */
public class Kmeans{

    private static final double DBL_MAX = 1.7976931348623158e+308; /* max value */

    // Define the lablels for the traffic signs
    private static final int SPEED_20_MPH_SIGN = 0;
    private static final int BUMP_SIGN  = 1;
    private static final int CROSSWALK_SIGN = 2;
    private static final int DIVIDED_ROAD_DOWN_SIGN = 3;
    private static final int DIVIDED_ROAD_UP_SIGN = 4;
    private static final int DO_NOT_ENTER_SIGN = 5;
    private static final int DO_NOT_PASS_SIGN = 6;
    private static final int HILL_DOWN_SIGN = 7;
    private static final int KEEP_LEFT_SIGN = 8;
    private static final int KEEP_RIGHT_SIGN = 9;
    private static final int LANE_ADDED_LEFT_SIGN = 10;
    private static final int LANE_ADDED_RIGHT_SIGN = 11;
    private static final int LANE_MERGE_LEFT_SIGN = 12;
    private static final int LANE_MERGE_RIGHT_SIGN = 13;
    private static final int LEFT_TURN_ONLY_SIGN = 14;
    private static final int NO_TRUCK_SIGN = 15;
    private static final int NO_TURN_LEFT_SIGN = 16;
    private static final int NO_TURN_RIGHT_SIGN = 17;
    private static final int NO_U_TURN_SIGN = 18;
    private static final int ONE_WAY_LEFT_SIGN = 19;
    private static final int ONE_WAY_RIGTH_SIGN = 20;
    private static final int RIGHT_TURN_ONLY_SIGN = 21;
    private static final int STOP_AHEAD_SIGN = 22;
    private static final int SCHOOL_CROSSING_SIGN = 23;
    private static final int SPEED_LIMIT_20_SIGN = 24;
    private static final int SPEED_LIMIT_30_SIGN = 25;
    private static final int SPEED_LIMIT_45_SIGN = 26;
    private static final int SPEED_LIMIT_55_SIGN = 27;
    private static final int SLIPPERY_WHEN_WET_SIGN = 28;
    private static final int STOP_SIGN = 29;
    private static final int TWO_WAY_LEFT_TURN_ONLY_SIGN = 30;
    private static final int TOW_ZONE_SIGN = 31;
    private static final int WINDING_LEFT_SIGN = 32;
    private static final int WINDING_RIGHT_SIGN = 33;
    private static final int YIELD_SIGN = 34;

    // Number of traffic sign in the database
    private static final int DB_NUM_TRAFFIC_SIGN = 35;

    // Dimension of the Kmeans points
    private static final int KMEANS_DIM_POINTS = 64;

    // Number of points used in the Kmeans algorithm
    private static final int KMEANS_NUM_POINTS = 1728;

    // Number of levels of the Kmeans tree
    private static final int KMEANS_NUM_LEVELS = 6; // IMPORTANTE! No codigo, devido a um bug desconhecido mas aparentemente ligado com alocacao de memoria, é usado KMEANS_NUM_LEVELS - 1

    // Number of clusters
    private static final int KMEANS_NUM_CLUSTERS = 2;

    // Minimum error allowed while building the clusters
    private static final double KMEANS_MIN_ERROR = 1e-4;

    public int[] labels;
    //public double[][] centroids;

    // K-means data points vector. It basically stores the all interest points, including a lablel
    // for linking the interest point with the correspoding traffic sign.
    double[][] Kmeans_data_points;

    // K-means data points auxiliary vector. This vector is used to build the clusters for each level.
    double[][] Kmeans_data_points_aux;

    // K-means centroid auxiliary vector. This vector is used to build the clusters for each level.
    double[][] Kmeans_centroids;

    // K-means levels.
    Kmean_level_data[] Kmeans_level = new Kmean_level_data[KMEANS_NUM_LEVELS + 1];

    // Declare the database to store the interest point of the training images.
    DBdescriptorsData[] DBdescriptor = new DBdescriptorsData[DB_NUM_TRAFFIC_SIGN];

    // Constructor
    public Kmeans()
    {
        int i,j;
        int []c;
        int []Kmeans_data_label_aux;
        int level;
        int cluster_index,cluster_bottom;

        // Load the database
        loadDB();

        // Load the interest points from database
        loadDataFromDB(1);

        // Initialize the Kmeans data points
        Kmeans_initializeDataPoint(KMEANS_NUM_POINTS);

        // Initiate the Kmeans' levels
        Kmeans_initLevels(KMEANS_NUM_LEVELS);

        // Initialize the Kmeans data label for the fisrt level
        Kmeans_data_label_aux = new int[KMEANS_NUM_POINTS];
        for(i = 0; i < KMEANS_NUM_POINTS; i++)
        {
            Kmeans_data_label_aux[i] = i;
        }

        // Display the desired interest point. Debug function.
        //Kmeans_displayIPts(0, KMEANS_DIM_POINTS, Kmeans_data_points_aux);
        //Kmeans_displayIPts(1, KMEANS_DIM_POINTS, Kmeans_data_points_aux);
        //Kmeans_displayIPts(2, KMEANS_DIM_POINTS, Kmeans_data_points_aux);


        // Allocate memory for the first level
        // Kmeans_allocateMemoryLevels(0,2);

//        // *********************** Build the Root Clusters ************************
//
        // Allocate memory for the level 0
        Kmeans_allocateMemoryCluster(0);

        // Initiate the cluster at level 0
        Kmeans_level[0].clusters.get(0).points = Kmeans_data_label_aux;
        Kmeans_level[0].clusters.get(0).numPoints = KMEANS_NUM_POINTS;

        // Go over the levels
        for(level = 0; level < KMEANS_NUM_LEVELS-1; level++)
        {
            System.out.println("At level = " + level);

            // Go over the clusters in each level
            cluster_index = 0;
            for(j = 0; j < Kmeans_level[level].numClusters; j++)
            {
                System.out.println("At cluster = " + j);

                // Store data points of the current cluster in the Kmeans aux vector
                Kmeans_updateDataPoints(Kmeans_level[level].clusters.get(j).points, Kmeans_level[level].clusters.get(j).numPoints);

                // Allocate memory for centroids
                Kmeans_allocateMemoryCentroid(0, 2, 64);

                System.out.println("Kmeans_data_points[0][0] = " + Kmeans_data_points[0][0]);
                System.out.println("Kmeans_data_points[0][1] = " + Kmeans_data_points[0][1]);

                System.out.println("Kmeans_data_points_aux[0][0] = " + Kmeans_data_points_aux[0][0]);
                System.out.println("Kmeans_data_points_aux[0][1] = " + Kmeans_data_points_aux[0][1]);

                // Apply the Kmeans algorithm to the cluster
                c = runKmeansOnCluster(Kmeans_data_points_aux,Kmeans_level[level].clusters.get(j).numPoints,KMEANS_DIM_POINTS, KMEANS_NUM_CLUSTERS, KMEANS_MIN_ERROR);

                System.out.println("runKmeansOnCluster ok");

                cluster_bottom = 0;
                for(i = 0; i < KMEANS_NUM_CLUSTERS; i++)
                {
                    // Check if the Cluster has at least one point
                    if(Kmeans_getNumPointsCluster(i,c,Kmeans_level[level].clusters.get(j).numPoints) != 0)
                    {
                        // Allocate memory for the Cluster for the next level
                        Kmeans_allocateMemoryCluster(level + 1);

                        // Get the points that belongs for each cluster
                        Kmeans_getPointsCluster(cluster_index,level+1,i,c,Kmeans_level[level].clusters.get(j).points,Kmeans_level[level].clusters.get(j).numPoints);

                        // Get the centroid of the cluster
                        Kmeans_level[level+1].clusters.get(cluster_index).centroid = Kmeans_centroids[i];

                        //Kmeans_level[level].clusters.get(j).bottomClusters.add(new Cluster_data());

                        // START DEBUG
					/*
						// Display the centroid of the Root Clusters
						printf("Centroid Cluster [%d] = [ ",i);
						for(int u = 0; u < dim; u++)
						{
							printf("%f, ",Kmeans_centroids[i][u]);
						}
						printf("] \n\r");
					*/

                        // END DEBUG

                        // Update the cluster index
                        cluster_index++;

                        // Update the number of bottom clusters that is associated with the current cluster
                        cluster_bottom++;

                    }
                }

                if(cluster_bottom != 0)
                {

                    // Allocate memory for the bottomClusters vector
                    Kmeans_level[level].clusters.get(j).bottomClusters = new Cluster_data[cluster_bottom];

                    // Update the number of bottom clusters
                    Kmeans_level[level].clusters.get(j).numBottomClusters = cluster_bottom;

                    // Assign the clusters from the current level to the previous level

                    for(i = 0; i < cluster_bottom; i++)
                    {
                        Kmeans_level[level].clusters.get(j).bottomClusters[i] =  Kmeans_level[level+1].clusters.get(cluster_index - cluster_bottom + i); /* i */

                        // DEBUG
                        //printf("Kmeans_level[%d].clusters[%d].bottomClusters[%d] = Kmeans_level[%d].clusters[%d] \r\n",level,j,i,level+1,cluster_index - cluster_bottom + i);
                    }

                }
            }
        }

        System.out.println("Level 0: Cluster 0 = %d points " + Kmeans_level[0].clusters.get(0).numPoints);
        System.out.println("Level 1: Cluster 0 = %d points " + Kmeans_level[1].clusters.get(0).numPoints);
        System.out.println("Level 2: Cluster 1 = %d points " + Kmeans_level[1].clusters.get(1).numPoints);

        System.out.println("Linking Levels. Level 1 Cluster 0 numPoint via Level 0 Cluster 0 = %d " + Kmeans_level[0].clusters.get(0).bottomClusters[0].numPoints);
        System.out.println("Linking Levels. Level 1 Cluster 1 numPoint via Level 0 Cluster 0 = %d " + Kmeans_level[0].clusters.get(0).bottomClusters[0].numPoints);
        System.out.println("Linking Levels. Level 2 Cluster 0 numPoint via Level 1 Cluster 0 = %d " + Kmeans_level[1].clusters.get(0).bottomClusters[0].numPoints);
        System.out.println("Linking Levels. Level 2 Cluster 1 numPoint via Level 1 Cluster 0 = %d " + Kmeans_level[1].clusters.get(0).bottomClusters[1].numPoints);
        System.out.println("Linking Levels. Level 2 Cluster 2 numPoint via Level 1 Cluster 1 = %d " + Kmeans_level[1].clusters.get(1).bottomClusters[0].numPoints);
        //System.out.println("Linking Levels. Level 2 Cluster 3 numPoint via Level 1 Cluster 1 = %d " + Kmeans_level[1].clusters.get(1).bottomClusters[1].numPoints);

        System.out.println("Cluster 2 number of clusters = %d " + Kmeans_level[1].numClusters);

        // Display the centroids
        System.out.println("Level 1 Cluster 0 Centroid = [ ");
        for (i = 0; i < KMEANS_DIM_POINTS; i++) {
            System.out.println(Kmeans_level[1].clusters.get(0).centroid[i]);
        }
        System.out.println(" ]");

        Cluster_data cluster_aux2;

        cluster_aux2 = Kmeans_level[0].clusters.get(0);

        System.out.println("cluster_aux2.numBottomClusters = " + cluster_aux2.numBottomClusters);
        System.out.println("cluster_aux2.bottomClusters[0].numPoints = " + cluster_aux2.bottomClusters[0].numPoints);
        System.out.println("cluster_aux2.bottomClusters[1].numPoints = " + cluster_aux2.bottomClusters[1].numPoints);
        System.out.println("cluster_aux2.bottomClusters[1].points[1] = " + cluster_aux2.bottomClusters[1].centroid[10]);

        // Display the centroids
        System.out.println("Level 1 buttom Cluster 0 Centroid = [ ");
        for (i = 0; i < KMEANS_DIM_POINTS; i++) {
            System.out.println(Kmeans_level[1].clusters.get(0).bottomClusters[1].centroid[i]);
        }
        System.out.println(" ]");

        System.out.println("Leaving Kmeans");
    }

    private int[] runKmeansOnCluster(double[][] data, int n, int m, int k, double t) {

    /* output cluster label for each data point */
    labels = new int[n];

    int h, i, j;                            /* loop counters, of course :) */
    int[] counts = new int[k];              /* size of each cluster */
    double old_error, error = DBL_MAX;      /* sum of squared euclidean distance */
    //centroids = new double[k][m];

    double [][]c = new double[k][m];
    double [][]c1 = new double[k][m];       /* temp centroids */

    //assert(data && k > 0 && k <= n && m > 0 && t >= 0); /* for debugging */

    //** initialization */
    for (h = i = 0; i < k; h += n / k, i++) {
        /* pick k points as initial centroids */
        for (j = m; j-- > 0; c[i][j] = data[h][j]);
    }

    // ****************** DEBUG ***********************

        System.out.println("data[0][0] = " + data[0][0]);
        System.out.println("data[0][1] = " + data[0][1]);

    System.out.println("c[0].x = " + c[0][0]);
    System.out.println("c[0].y = " + c[0][1]);

    System.out.println("c[1].x = " + c[1][0]);
    System.out.println("c[1].y = " + c[1][1]);

    // ***********************************************
    // /* main loop */

    do {
    /* save error from last step */
    old_error = error;
    error = 0;

    /* clear old counts and temp centroids */
    for (i = 0; i < k; counts[i++] = 0) {
        for (j = 0; j < m; c1[i][j++] = 0);
    }

    for (h = 0; h < n; h++) {
        /* identify the closest cluster */
        double min_distance = DBL_MAX;
        for (i = 0; i < k; i++) {
            double distance = 0;
            for (j = m; j-- > 0; distance += Math.pow(data[h][j] - c[i][j], 2));
            if (distance < min_distance) {
                labels[h] = i;
                min_distance = distance;
            }
        }
        /* update size and temp centroid of the destination cluster */
        for (j = m; j-- > 0; c1[labels[h]][j] += data[h][j]);
        counts[labels[h]]++;
        /* update standard error */
        error += min_distance;
    }

    for (i = 0; i < k; i++) { /* update all centroids */
        for (j = 0; j < m; j++) {

            if(counts[i] > 0)
            {
                c[i][j] = c1[i][j] / counts[i];
            }
            else
            {
                c[i][j] = c1[i][j];
            }
        }
    }

    /* ********* Debug ********** */
    System.out.println("c[0].x = " + c[0][0]);
    System.out.println("c[0].y = " + c[0][1]);
    System.out.println("c[1].x = " + c[1][0]);
    System.out.println("c[1].y = " + c[1][1]);

    /* ************************** */

    } while (Math.abs(error - old_error) > t);

    for (i = 0; i < k; i++) {
        for (j = 0; j < m; j++) {

            Kmeans_centroids[i][j] = c[i][j];
        }
    }

    return labels;

    }

    /*******************************************************************************
     * Function Name  : Kmeans_getPointsCluster
     * Description    : Get the label of the points that belongs to the cluster
     : Input: - i = index of the cluster
     - pointsPrevCluster = Label for the points of the
     previous Cluster
     - c : Label after Kmeans be executed
     *******************************************************************************/
    private void Kmeans_getPointsCluster(int cluster_index, int level_index,int i, int[] c, int[] pointsPrevCluster, int numPointsPrevCluster)
    {
        System.out.println("In Kmeans_getPointsCluster");

        // Auxiliary variable
        int k,j;
        int n;
        int[] pointsLabel;

        n = 0;
        // Get the number of points that belongs to the cluster i
        for(k = 0; k < numPointsPrevCluster; k++)
        {
            if(c[k] == i)
            {
                n++;
            }
        }

        // Allocate memory for the cluster points label
        pointsLabel = new int[n];

        // Get the label of the points that belongs to the cluster i
        j = 0;
        for(k = 0; k < numPointsPrevCluster; k++)
        {
            if(c[k] == i)
            {
                pointsLabel[j] = pointsPrevCluster[k];
                j++;
            }
        }

        //Kmeans_level[level_index].clusters.get(cluster_index)

        //Cluster.points = pointsLabel;
        //Cluster.numPoints = n;

        Kmeans_level[level_index].clusters.get(cluster_index).initPoints(n,pointsLabel);

        System.out.println("Kmeans_level[level_index].clusters.get(cluster_index).numPoints = " + Kmeans_level[level_index].clusters.get(cluster_index).numPoints);

        System.out.println("Leaving Kmeans_getPointsCluster");
    }

    /*******************************************************************************
     * Function Name  : Kmeans_getNumPointsCluster
     * Description    : Get the number of points that belongs to the Cluster
     : Input: - k: Cluster label
     - c: Vector that contains the points' label for the
     clusters
     - numPoints: Number of points that were used in the
     previous level.

     returns: - n: Number of points within the Cluster
     *******************************************************************************/
    private int Kmeans_getNumPointsCluster(int k, int[] c, int numPoints)
    {
        System.out.println("In Kmeans_getNumPointsCluster");

        // Auxiliary variable
        int i,n;
        n = 0;

        // Count the number of elements in the designed cluster
        for(i = 0; i < numPoints; i++)
        {
            if(c[i] == k)
            {
                n++;
            }
        }

        System.out.println("Leaving Kmeans_getNumPointsCluster");

        return n;
    }

    /*******************************************************************************
     * Function Name  : Kmeans_updateDataPoints
     * Description    : Update the Kmeans data point vector aux for another round
     : Input: - label: Label of the points that belongs to the
     target cluster
     - numPoints : Number of points of the cluster
     *******************************************************************************/
    private void Kmeans_updateDataPoints(int[] labels,int numPoints)
    {
        System.out.println("In Kmeans_updateDataPoints");

        // Auxiliary variables
        int i;

        for(i = 0; i < numPoints; i++)
        {
            Kmeans_data_points_aux[i] = Kmeans_data_points[labels[i]];
        }

        System.out.println("Leaving Kmeans_updateDataPoints");
    }

    /*******************************************************************************
     * Function Name  : Kmeans_allocateMemoryCentroid
     * Description    : Allocate memory for the centroids
     *******************************************************************************/
    private void Kmeans_allocateMemoryCentroid(int level, int k, int dim)
    {
        System.out.println("In Kmeans_allocateMemoryCentroid");

        // Auxiliary variable
        int i;

        Kmeans_centroids = new double[k][dim];

        System.out.println("Leaving Kmeans_allocateMemoryCentroid");

        // ******************** TO BE REMOVED ****************************

//        // Allocate memory for the number of centroids at the given level
//        Kmeans_level[level].centroids = (double**)calloc(k,sizeof(double*));
//
//        // Allocate memory for the dimension of the centroids
//        for(i = 0; i < k; i++)
//        {
//            Kmeans_level[level].centroids[i] = (double*)calloc(dim,sizeof(double));
//        }

        // ****************************************************************
    }

    /*******************************************************************************
     * Function Name  : Kmeans_allocateMemoryCluster
     * Description    : Allocate memory for the cluster
     : Input: - numPoints
     *******************************************************************************/
    private void Kmeans_allocateMemoryCluster(int level)
    {
        System.out.println("In Kmeans_allocateMemoryCluster");

        // Auxiliary variables
        int n,i;

        // Check if the number of Cluster at the level is 0. If so, new memory is
        // allocate for the cluster.
        if(Kmeans_level[level].numClusters == 0)
        {
            Kmeans_level[level].clusters.add(new Cluster_data());

            // Initiate the new Cluster
            Kmeans_level[level].clusters.get(0).bottomClusters = null;
            Kmeans_level[level].clusters.get(0).centroid = null;
            Kmeans_level[level].clusters.get(0).points = null;
            Kmeans_level[level].clusters.get(0).numPoints = 0;

            // Update the number of Clusters at the level
            Kmeans_level[level].numClusters++;
        }
        else
        {
            // Create an auxiliary cluster vector of such size that allocates the previous
            // clusters and one more
            n = Kmeans_level[level].numClusters + 1;
//
//            // Copy the previous clusters
//            for(i = 0; i < Kmeans_level[level].numClusters ; i++)
//            {
//               Cluster_aux[i] = Kmeans_level[level].clusters[i];
//            }

            Kmeans_level[level].clusters.add(new Cluster_data());

//            // Update the Level
            Kmeans_level[level].numClusters++;
//
//            // Initiate the new Cluster
            Kmeans_level[level].clusters.get(n-1).bottomClusters = null;
            Kmeans_level[level].clusters.get(n-1).centroid = null;
            Kmeans_level[level].clusters.get(n-1).points = null;
            Kmeans_level[level].clusters.get(n-1).numPoints = 0;

            // Deallocate the auxiliary cluster vector
            //free(Cluster_aux);
        }

        System.out.println("Leaving Kmeans_allocateMemoryCluster");
    }

    private void loadDB()
    {
        speed_20_mph_descriptors speed_20_mph_obj = new speed_20_mph_descriptors();

        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = new File("storage/sdcard1/TCC-TSR-2017/SURF implementation");

        //Get the text file
        File file = new File(sdcard,"speed_20_mph_descriptors2.txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            DBdescriptor[SPEED_20_MPH_SIGN] = new DBdescriptorsData(11776,184);

            int i = 0;

            while ((line = br.readLine()) != null) {
                //DBdescriptor[SPEED_20_MPH_SIGN].data[i] = 1.0;//Double.parseDouble(line);

                DBdescriptor[SPEED_20_MPH_SIGN].setData(i,Double.parseDouble(line));

                i++;
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            System.out.println("Not possible to read file =(");
        }

        //DBdescriptor[SPEED_20_MPH_SIGN].data = speed_20_mph_obj.data;
        //DBdescriptor[SPEED_20_MPH_SIGN].size = speed_20_mph_obj.size;


        System.out.println("DBdescriptor[SPEED_20_MPH_SIGN].data[0] = " + DBdescriptor[SPEED_20_MPH_SIGN].data[0]);
        System.out.println("DBdescriptor[SPEED_20_MPH_SIGN].data[1] = " +  DBdescriptor[SPEED_20_MPH_SIGN].data[1]);
        System.out.println("DBdescriptor[SPEED_20_MPH_SIGN].data[11776] = " +  DBdescriptor[SPEED_20_MPH_SIGN].data[11775]);

        System.out.println("Teste load db ");

    }

    private void Kmeans_initializeDataPoint(int numPoints)
    {
        // Auxiliary variables
        int i,j;

        // Allocate memory
        Kmeans_data_points_aux = new double[numPoints][KMEANS_DIM_POINTS];

        for(i = 0; i < numPoints; i++) {

            for (j = 0; j < KMEANS_DIM_POINTS; j++) {
                Kmeans_data_points_aux[i][j] = Kmeans_data_points[i][j];
            }
        }
    }

    /*******************************************************************************
     * Function Name  : Kmeans_initLevels
     * Description    : Initiate the Kmeans levels.
     : Input: - numLevels: Num of levels of the Kmeans tree
     *******************************************************************************/
    private void Kmeans_initLevels(int numLevels)
    {
        // Auxiliary variables
        int i;

        // Set the number of Clusters to 0
        for(i = 0; i < numLevels; i++)
        {
            Kmeans_level[i] = new Kmean_level_data();

            Kmeans_level[i].numClusters = 0;
        }
    }

    private void loadDataFromDB(int numTrafficSign)
    {
        // Auxiliary variable
        int i,j,index;
        int n;

        index = 0;

        if(numTrafficSign != 0)
        {
            // *****************  Alloc memory: ONLY FOR DESKTOP VERSION **********************
            // Get the number of interest points

            n = 11776;

            /*
            n = DBdescriptor[SPEED_20_MPH_SIGN].size + DBdescriptor[BUMP_SIGN].size + DBdescriptor[CROSSWALK_SIGN].size +
                DBdescriptor[DIVIDED_ROAD_DOWN_SIGN].size + DBdescriptor[DIVIDED_ROAD_UP_SIGN].size + DBdescriptor[DO_NOT_ENTER_SIGN].size +
                DBdescriptor[DO_NOT_PASS_SIGN].size + DBdescriptor[HILL_DOWN_SIGN].size + DBdescriptor[KEEP_LEFT_SIGN].size +
                DBdescriptor[KEEP_RIGHT_SIGN].size + DBdescriptor[LANE_ADDED_LEFT_SIGN].size + DBdescriptor[LANE_ADDED_RIGHT_SIGN].size +
                DBdescriptor[LANE_MERGE_LEFT_SIGN].size + DBdescriptor[LANE_MERGE_RIGHT_SIGN].size + DBdescriptor[LEFT_TURN_ONLY_SIGN].size +
                DBdescriptor[NO_TRUCK_SIGN].size + DBdescriptor[NO_TURN_LEFT_SIGN].size + DBdescriptor[NO_TURN_RIGHT_SIGN].size +
                DBdescriptor[NO_U_TURN_SIGN].size + DBdescriptor[ONE_WAY_LEFT_SIGN].size + DBdescriptor[ONE_WAY_RIGTH_SIGN].size +
                DBdescriptor[RIGHT_TURN_ONLY_SIGN].size + DBdescriptor[STOP_AHEAD_SIGN].size + DBdescriptor[SCHOOL_CROSSING_SIGN].size +
                DBdescriptor[SPEED_LIMIT_20_SIGN].size + DBdescriptor[SPEED_LIMIT_30_SIGN].size + DBdescriptor[SPEED_LIMIT_45_SIGN].size +
                DBdescriptor[SPEED_LIMIT_55_SIGN].size + DBdescriptor[SLIPPERY_WHEN_WET_SIGN].size + DBdescriptor[STOP_SIGN].size +
                DBdescriptor[TWO_WAY_LEFT_TURN_ONLY_SIGN].size + DBdescriptor[TOW_ZONE_SIGN].size + DBdescriptor[WINDING_LEFT_SIGN].size +
                DBdescriptor[WINDING_RIGHT_SIGN].size + DBdescriptor[YIELD_SIGN].size;
                */

            // Number of interest points to be loaded
            Kmeans_data_points = new double[n][KMEANS_DIM_POINTS + 1];

            // *********************************************************************************

            // Load the SPEED_20_MPH_SIGN interest points
            for(i = 0; i < DBdescriptor[SPEED_20_MPH_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[SPEED_20_MPH_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }

            System.out.println("Kmeans_data_points[0][0] = " + Kmeans_data_points[0][0]);
            System.out.println("Kmeans_data_points[0][1] = " + Kmeans_data_points[0][1]);

            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[BUMP_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[BUMP_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[CROSSWALK_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[CROSSWALK_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[DIVIDED_ROAD_DOWN_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[DIVIDED_ROAD_DOWN_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[DIVIDED_ROAD_UP_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[DIVIDED_ROAD_UP_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[DO_NOT_ENTER_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[DO_NOT_ENTER_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[DO_NOT_PASS_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[DO_NOT_PASS_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[HILL_DOWN_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[HILL_DOWN_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[KEEP_LEFT_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[KEEP_LEFT_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        if(numTrafficSign != 0)
        {
            // Load the BUMP_SIGN interest points
            for(i = 0; i < DBdescriptor[KEEP_RIGHT_SIGN].rows; i++,index++)
            {
                for(j = 0; j < KMEANS_DIM_POINTS; j++)
                {
                    Kmeans_data_points[index][j] = DBdescriptor[KEEP_RIGHT_SIGN].data[j + KMEANS_DIM_POINTS*i];
                }
            }
            numTrafficSign--;
        }
        else
        {
            return;
        }

        /*
        Kmeans_data_points = (double**)calloc(4,sizeof(double*));
        Kmeans_data_points[0] = (double*)calloc(3,sizeof(double));
        Kmeans_data_points[1] = (double*)calloc(3,sizeof(double));
        Kmeans_data_points[2] = (double*)calloc(3,sizeof(double));
        Kmeans_data_points[3] = (double*)calloc(3,sizeof(double));

        Kmeans_data_points[0][0] = 1;
        Kmeans_data_points[0][1] = 2;
        Kmeans_data_points[0][2] = 10;

        Kmeans_data_points[1][0] = 2;
        Kmeans_data_points[1][1] = 3;
        Kmeans_data_points[1][2] = 11;

        Kmeans_data_points[2][0] = 4;
        Kmeans_data_points[2][1] = 2;
        Kmeans_data_points[2][2] = 12;

        Kmeans_data_points[3][0] = 4;
        Kmeans_data_points[3][1] = 1;
        Kmeans_data_points[3][2] = 13;
        */
    }
}
