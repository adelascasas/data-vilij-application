package ui;

import actions.AppActions;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static settings.AppPropertyTypes.SCREENSHOT_ICON;
import static settings.AppPropertyTypes.SCREENSHOT_TOOLTIP;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    public ScatterChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        // TODO for homework 1
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconPath = "/gui/icons/"+ manager.getPropertyValue(SCREENSHOT_ICON.name());
        scrnshotButton = setToolbarButton(iconPath,manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()),true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        // TODO for homework
        textArea.clear();
        chart.getData().clear();
    }

    private void layout() {
        // TODO for homework 1
        NumberAxis Xaxis = new NumberAxis();
        NumberAxis Yaxis = new NumberAxis();
        chart = new ScatterChart<Number,Number>(Xaxis,Yaxis);
        chart.setTitle("Data Visualization");
        textArea = new TextArea();
        displayButton = new Button("Display");
        workspace = new HBox();
        Label datafile = new Label("Data File");
        datafile.setFont(new Font("Arial",15));
        VBox displayoption = new VBox(datafile,textArea,displayButton);
        super.workspace.getChildren().add(displayoption);
        super.workspace.getChildren().add(chart);
        super.appPane.getChildren().add(workspace);
    }

    private void setWorkspaceActions() {
        // TODO for homework 1
        displayButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event) {
               //  TODO deal with the no text case
               //TODO deal with the invalid format
               //TODO make method that checks valid input
               //TODO check for repeated points
               String[] Textinputs = textArea.getText().split("\\n");
                String[] Singleline;
                String[] points;
                for(int i=0;i<Textinputs.length;i++){
                   Singleline  = Textinputs[i].split("\\s");
                    points = Singleline[2].split(",");
                    XYChart.Series series = new XYChart.Series();
                    series.setName(Singleline[1]);
                    series.getData().add(new XYChart.Data(Integer.parseInt(points[0]),Integer.parseInt(points[1])));
                    chart.getData().addAll(series);

                }
                }
        });

        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppActions action = (AppActions) applicationTemplate.getActionComponent();
            }
        });

        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppActions action = (AppActions)applicationTemplate.getActionComponent();
                action.handleNewRequest();
            }
        });

        getTextArea().textProperty().addListener((observable, oldValue, newValue) -> {
            hasNewText = !(textArea.getText().isEmpty());
            if(hasNewText){
                    newButton.setDisable(false);
                }
             else{
                newButton.setDisable(true);
            }
        });    }

    public TextArea getTextArea(){
        return this.textArea;
    }
}
