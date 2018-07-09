package algorithms;

import actions.AppActions;
import dataprocessors.AppData;
import dataprocessors.DataSet;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.Clusterer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {

    private DataSet dataset;
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private AppData appData;
    private static final Random RAND = new Random();


    public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean tocontinue, AppData appData) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.appData = appData;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    @Override
    public void run() {
        for(int i=1;i<=maxIterations;i++){
            if (i % updateInterval != 0 && i !=maxIterations) {
                continue;
            }
            RandomizeLabels();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    appData.setDataSet(dataset.getLabels(),dataset.getLocations());
                }
            });
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                break;
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            if ((i % updateInterval == 0 && !(tocontinue()) && i != maxIterations) || !AppActions.getIsRunning().get()) {
                if((i % updateInterval == 0 && !(tocontinue())) && i != maxIterations){
                    AppUI.getPlayButton().setDisable(false);
                    AppActions.getIsRunning().set(false);
                    AppUI.getscrnshotButton().setDisable(false);
                    AppUI.getStopped().set(true);
                }
                synchronized (AppActions.getIsRunning()){
                    while(!AppActions.getIsRunning().get()) {
                        try {
                            AppActions.getIsRunning().wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            if(AppActions.getEndAlgo().get()){
                return;
            }
        }
    }

    private void RandomizeLabels(){
        ArrayList<String> labels = new ArrayList<>();
        for(int i=1;i<=numberOfClusters;i++){
            labels.add("label "+i);
        }
        Random random = new Random();
        ArrayList<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        for(int i=0;i<dataset.getLabels().size();i++) {
            dataset.getLabels().put(instanceNames.get(i), labels.get(random.nextInt(numberOfClusters)));
        }
    }
}
