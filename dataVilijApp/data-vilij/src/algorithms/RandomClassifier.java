package algorithms;
import actions.AppActions;
import dataprocessors.DataSet;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.Classifier;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

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

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {
        for (int i = 1; i <= maxIterations; i++) {
            if (i % updateInterval != 0 && i !=maxIterations) {
                continue;
            }
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);
            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            XYChart.Series<Number, Number> line_series = (AppUI.getChart().getData().get(AppUI.getChart().getData().size() - 1));
            Number newY1 = (-1 * (xCoefficient * line_series.getData().get(0).getXValue().doubleValue()) - constant) / yCoefficient;
            line_series.getData().get(0).setYValue(newY1);
            Number newY2 = (-1 * (xCoefficient * line_series.getData().get(1).getXValue().doubleValue()) - constant) / yCoefficient;
            line_series.getData().get(1).setYValue(newY2);
            line_series.getNode().setVisible(true);
            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
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


    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("sample-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    }
}