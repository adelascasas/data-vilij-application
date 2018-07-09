package algorithms;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import actions.AppActions;
import dataprocessors.AppData;
import dataprocessors.DataSet;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.Clusterer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private  final AppData    appData;
    private AtomicBoolean complete;


    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean tocontinue, AppData appData) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.appData = appData;
        complete = new AtomicBoolean(false);
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public void run() {
                int iteration = 1;
                complete.set(true);
                while(iteration <= maxIterations && complete.get()) {
                    initializeCentroids();
                    if (iteration % updateInterval != 0 && iteration != maxIterations) {
                        iteration++;
                        continue;
                    }
                    assignLabels();
                    Platform.runLater(new Runnable() {
                        @Override
                       public void run() {
                            appData.setDataSet(dataset.getLabels(),dataset.getLocations());
                        }
                    });
                    recomputeCentroids();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                    }
                    if ((iteration % updateInterval == 0 && !(tocontinue()) && iteration != maxIterations) || !AppActions.getIsRunning().get()) {
                        if ((iteration % updateInterval == 0 && !(tocontinue())) && iteration != maxIterations) {
                            AppUI.getPlayButton().setDisable(false);
                            AppActions.getIsRunning().set(false);
                            AppUI.getscrnshotButton().setDisable(false);
                            AppUI.getStopped().set(true);
                        }
                        synchronized (AppActions.getIsRunning()) {
                            while (!AppActions.getIsRunning().get()) {
                                try {
                                    AppActions.getIsRunning().wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                    if (AppActions.getEndAlgo().get()) {
                        return;
                    }
                    iteration++;
                }
    }

    //from my understanding this method assigns a center coordinate corresponding to a label or cluster from the dataset
    // and stores those coordinates as a List
    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();//will not allow duplicates and accepts null
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());//LIST of all of the instance names in dataset
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {//chosen set is first set to zero and loop completes when size of set is equal to size of numberOfClusters
            int i = r.nextInt(instanceNames.size());
            //random number i is based on 0 and numer of instances-1
            while (chosen.contains(instanceNames.get(i))) {
                i = i+1 >= instanceNames.size() ? r.nextInt(instanceNames.size()) : i+1;
            }
        //if it does go to the next instance name and check again
            chosen.add(instanceNames.get(i));//finally if the chosen Set does not contain this instance name form instanceNames List
        }//the instance name is added to the chosen
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        //centroids is a Point2D object, the stream brings back locations of the given instance names from the chosen Set
        //then the stream is accumulated into a list
    }

    private void assignLabels() {
        //getlocations brings back HashMaps with all the instance names as keys and coordinates as values
        // so in this stream for every  instance name and their location
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;//maximum value a double can represent
            int    minDistanceIndex = -1; //min index
            for (int i = 0; i < centroids.size(); i++) { //for loop for the centroids size or in other words the number of clusters
                double distance = computeDistance(centroids.get(i), location); //computeDistance computes the distance between two points
                //in this case the distance between the first centroid in the list and a location that pertains
                if (distance < minDistance) { //if the min distance is lower then the previous distance then
                    minDistance = distance;  // a new minDistance is set
                    minDistanceIndex = i; //
                }
            }
            //from my understanding in this loop all the centroid coordinates are being compared to the instanceName's Location
            //in that iteration and trying to find the least distnance between that given iteration's coordinate and the centroids
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
            //this is done to all instances in the get locations and in the getlabels hashmap labels are now replaced
            //with the index of the coordinate in the centroid list that had the lowest
            //so now each instance has the index of one the coordinate in the centroids list
        });
    }

    private void recomputeCentroids() {
        complete.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> { //intstream ranges from 0 to numberOfClusters-1, so for each cluster
            AtomicInteger clusterSize = new AtomicInteger(); //new atomic integer representing the size of cluster
            Point2D sum = dataset.getLabels() //stream will return some kind of Point 2D object
                    .entrySet() //obtain the label names
                    .stream() //stream of label names for each instance
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))//filter to return stream of index label names that match the given index
                    .map(entry -> dataset.getLocations().get(entry.getKey())) //turn the stream to obtain the 2D coordinates that pertain to the instance names with the same label
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                complete.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}
