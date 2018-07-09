package dataprocessors;


import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import ui.AppUI;
import ui.Classifier;
import vilij.propertymanager.PropertyManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static settings.AppPropertyTypes.*;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    AtomicInteger linenumber = new AtomicInteger(0);
    private static String tab = "\t";

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = PropertyManager.getManager().getPropertyValue(AT_ERROR.name());

        public InvalidDataNameException(String name) {
            super(PropertyManager.getManager().getPropertyValue(INVALID_NAME.name()) + name + NAME_ERROR_MSG);
        }
    }
    public static class InvalidInputException extends Exception{

        public InvalidInputException(String line){
            super(PropertyManager.getManager().getPropertyValue(INVALID_INPUT.name()) + line);
        }
    }

    private static Map<String, String>  dataLabels;
    private static Map<String, Point2D> dataPoints;
    private static LinkedHashSet<String> labels;

    public TSDProcessor() {
        dataLabels = new LinkedHashMap<>();
        dataPoints = new LinkedHashMap<>();
        labels = new LinkedHashSet<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        linenumber = new AtomicInteger(0);
        AtomicInteger errorline = new AtomicInteger(0);
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder repeatName = new StringBuilder();
        Stream.of(tsdString.split(AppData.getnewline()))
              .map(line -> Arrays.asList(line.split(getTab())))
              .forEach(list -> {
                  try {
                      linenumber.incrementAndGet();
                      checkedline(list,errorline);
                      String name = checkedname(list.get(0),errorline);
                      if(dataLabels.containsKey(list.get(0))){
                          if(errorline.get()==0){
                              errorline.set(linenumber.get());
                          }
                          if(repeatName.length()==0){
                              repeatName.append(list.get(0));
                          }
                          throw new InvalidInputException(PropertyManager.getManager().getPropertyValue(REPEATED_NAME.name())+repeatName.toString()+PropertyManager.getManager().getPropertyValue(LINE_ERROR.name())+errorline);
                      }
                      String   label = list.get(1);
                      if(!label.equals(PropertyManager.getManager().getPropertyValue(NULL.name()))){
                      labels.add(list.get(1));}
                      String[] pair  = list.get(2).split(PropertyManager.getManager().getPropertyValue(COMMA.name()));
                     try {
                         Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                         dataLabels.put(name, label);
                         dataPoints.put(name, point);
                     }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
                         if(errorline.get()==0){
                             errorline.set(linenumber.get());
                         }
                         throw new InvalidInputException(PropertyManager.getManager().getPropertyValue(INVALID_COORDINATE.name())+PropertyManager.getManager().getPropertyValue(LINE_ERROR.name())+errorline);
                     }
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(PropertyManager.getManager().getPropertyValue(DISPLAYSYMBOL.name())).append(e.getMessage());
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());

    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        XYChart.Series<Number,Number> average_series = new XYChart.Series<>();
        Number averageYvalues = 0;
        Number maxXvalue =  dataPoints.values().iterator().next().getX();
        Number minXvalue =  dataPoints.values().iterator().next().getX();
        ArrayList<ArrayList<String>>keys = new ArrayList<>();
        Set<String> labels = new LinkedHashSet<>(dataLabels.values());
        for (String label : labels) {
            ArrayList<String>tempkeys = new ArrayList<>();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number,Number> data= new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(data);
                tempkeys.add(entry.getKey());
                });
                chart.getData().add(series);
            series.getNode().lookup(PropertyManager.getManager().getPropertyValue(CHART_SERIES.name())).setId(PropertyManager.getManager().getPropertyValue(CUSTOM_SERIES.name()));
            if(series.getData().get(0).getXValue().doubleValue()>maxXvalue.doubleValue()){
                maxXvalue = series.getData().get(0).getXValue().doubleValue();
            }
            else if(series.getData().get(0).getXValue().doubleValue()<minXvalue.doubleValue()){
                minXvalue = series.getData().get(0).getXValue().doubleValue();
            }
            for(int i=0;i<series.getData().size();i++) {
                averageYvalues = averageYvalues.doubleValue() + series.getData().get(i).getYValue().doubleValue();
                if(series.getData().get(i).getXValue().doubleValue()>maxXvalue.doubleValue()){
                    maxXvalue = series.getData().get(i).getXValue().doubleValue();
                }
                else if(series.getData().get(i).getXValue().doubleValue()<minXvalue.doubleValue()){
                    minXvalue = series.getData().get(i).getXValue().doubleValue();
                }
            }
            keys.add(tempkeys);
        }
        int i=0;
        int j = 0;
        for (XYChart.Series<Number, Number> series : chart.getData()) {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                data.getNode().setCursor(Cursor.CLOSED_HAND);
                     Tooltip.install(data.getNode(), new Tooltip(keys.get(j).get(i)));
                       i++;
            }
            j++;
            i=0;
        }
        if(AppUI.getChosen() instanceof Classifier) {
            average_series.getData().add(new XYChart.Data<>(maxXvalue.doubleValue(), (averageYvalues.doubleValue() / dataPoints.size())));
            average_series.getData().add(new XYChart.Data<>(minXvalue.doubleValue(), (averageYvalues.doubleValue() / dataPoints.size())));
            average_series.setName(PropertyManager.getManager().getPropertyValue(AVERAGE_LINE.name()));
            chart.getData().add(average_series);
            Tooltip.install(average_series.getNode(), new Tooltip(PropertyManager.getManager().getPropertyValue(AVERAGE_LINE.name())));
            average_series.getNode().setCursor(Cursor.CLOSED_HAND);
            for (XYChart.Data<Number, Number> data : average_series.getData()) {
                StackPane symbol = (StackPane) data.getNode();
                symbol.setVisible(false);
            }
            average_series.getNode().setVisible(false);
        }
        }

    public void clear() {
        dataPoints.clear();
        dataLabels.clear();
        labels.clear();
    }

    private void checkedline(List line,AtomicInteger errorline) throws InvalidInputException{
        if(line.size() != 3){
            if(errorline.get()==0){
                errorline.set(linenumber.get());
            }
            throw new InvalidInputException(PropertyManager.getManager().getPropertyValue(INVALID_LINE.name())+PropertyManager.getManager().getPropertyValue(LINE_ERROR.name())+errorline);
        }
    }

    private String checkedname(String name,AtomicInteger errorline) throws InvalidDataNameException {
        if (!name.startsWith(PropertyManager.getManager().getPropertyValue(AT_SYMBOL.name()))){
            if(errorline.get()==0){
                errorline.set(linenumber.get());
            }
            throw new InvalidDataNameException(name + PropertyManager.getManager().getPropertyValue(LINE_ERROR.name())+errorline);}
        return name;
    }

    public static String getTab(){
        return tab;
    }

    public Map<String,String> getDatalabels(){
        return dataLabels;
    }

    public LinkedHashSet<String> getLabels(){
        return labels;
    }

    void setData(Map<String,String> Labels, Map<String,Point2D> locations){
        AppUI.getChart().getData().clear();
        dataPoints.clear();
        dataLabels.clear();
        dataLabels.putAll(Labels);
        dataPoints.putAll(locations);
        toChartData(AppUI.getChart());
    }
}
