package ui;
import dataprocessors.DataSet;
import javafx.concurrent.Service;
import actions.AppActions;
import dataprocessors.AppData;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static settings.AppPropertyTypes.*;
/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;
    private static boolean isSaved;
    @SuppressWarnings("FieldCanBeLocal")
    private static Button scrnshotButton; // toolbar button to take a screenshot of the data
    private static LineChart<Number, Number> chart;          // the chart where data will be displayed
    private static TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private static Button playButton;
    private VBox displayoption;
    private static Classifier chosenClass;
    private static ArrayList<Clusterer> chosenClusts = new ArrayList<>();
    private static Algorithm chosen;
    private Label complete;
    private static AtomicBoolean stopped = new AtomicBoolean(false);
    private Service<Void> backgroundthread;

    public static LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        try {
            getAlgorithms();
        }catch (IOException | ClassNotFoundException e){}
        complete = new Label(applicationTemplate.manager.getPropertyValue(RUN_COMPLETE.name()));
        playButton = new Button();
        playButton.setId(applicationTemplate.manager.getPropertyValue(PLAYBUTTON.name()));
        playButton.setDisable(true);
    }

    private void getAlgorithms() throws IOException,ClassNotFoundException{
        Path p1 = Paths.get(applicationTemplate.manager.getPropertyValue(ALGORITHM_FOLDER.name()));
        File file = new File(p1.toAbsolutePath().toString());
        Class<?> ClusterClass = Class.forName(applicationTemplate.manager.getPropertyValue(CLUST_CLASS.name()));
        Class<?> ClassiferClass = Class.forName(applicationTemplate.manager.getPropertyValue(CLASSI_CLASS.name()));
        File[] files = file.listFiles();
        ClassLoader classLoader1 = ClassLoader.getSystemClassLoader();
        Class<?> cls1;
        for(File file1 :files){
            cls1 = classLoader1.loadClass(applicationTemplate.manager.getPropertyValue(ALGORITHM_PATH.name())+file1.getName().replaceFirst("[.][^.]+$", ""));
            try {
                if (ClusterClass.isAssignableFrom(cls1)) {
                  Constructor<?> constructor =  cls1.getConstructor(DataSet.class,int.class,int.class,int.class,boolean.class,AppData.class);
                    chosenClusts.add((Clusterer) constructor.newInstance(null,0,0,0,false,(AppData)applicationTemplate.getDataComponent()));
                }
                else if(ClassiferClass.isAssignableFrom(cls1)){
                   Constructor constructor =  cls1.getConstructor(DataSet.class,int.class,int.class,boolean.class);
                    chosenClass = ((Classifier) constructor.newInstance(null,0,0,false));
                }
            }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconPath = manager.getPropertyValue(SNAPSHOT_PATH.name()) + manager.getPropertyValue(SCREENSHOT_ICON.name());
        scrnshotButton = setToolbarButton(iconPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), false);
        saveButton.setDisable(true);
        loadButton.setDisable(false);
        scrnshotButton.setDisable(true);
        toolBar.getItems().add(scrnshotButton);
        setIsSaved(false);
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
        getPrimaryScene().getStylesheets().add(applicationTemplate.manager.getPropertyValue(STYLE_SHEET.name()));
        try {
            setWorkspaceActions();
        } catch (IOException e) {
        }
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    private void layout() {
        NumberAxis Xaxis = new NumberAxis();
        NumberAxis Yaxis = new NumberAxis();
        chart = new LineChart<>(Xaxis, Yaxis);
        chart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_NAME.name()));
        textArea = new TextArea();
        workspace = new HBox();
        Label datafile = new Label(applicationTemplate.manager.getPropertyValue(TEXT_AREA.name()));
        newButton.setDisable(false);
        displayoption = new VBox();
       displayoption.getStyleClass().add(applicationTemplate.manager.getPropertyValue(VBOX.name()));
        displayoption.getChildren().addAll(datafile, textArea);
        Separator separator = new Separator();
        VBox chartBox = new VBox(chart,complete);
        complete.setVisible(false);
        chartBox.setId(applicationTemplate.manager.getPropertyValue(EXIT_REQUEST.name()));
        workspace.getChildren().addAll(displayoption,separator, chartBox);
        chart.setVisible(false);
        displayoption.setVisible(false);
        super.appPane.getChildren().add(workspace);
    }

    private void setWorkspaceActions() throws IOException {
        getTextArea().textProperty().addListener((observable, oldValue, newValue) -> {
            hasNewText = !(textArea.getText().isEmpty());
            if (hasNewText) {
                newButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
                newButton.setDisable(true);
                saveButton.setDisable(true);
            }
        });
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppData appData = (AppData) applicationTemplate.getDataComponent();
                try {
                    appData.loadData(textArea.getText());
                    appData.clear();
                    if (AppData.extralines.length() != 0) {
                        appData.loadData(AppData.filedata.toString() + AppData.extralines.toString());
                    }
                    appData.clear();
                    (applicationTemplate.getActionComponent()).handleSaveRequest();
                    saveButton.setDisable(isSaved);
                } catch (Exception e) {
                    appData.clear();
                    ErrorDialog errorDialog = ErrorDialog.getDialog();
                    errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()), e.getMessage());
                }
            }
        });

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppActions.getEndAlgo().set(false);
                complete.setVisible(false);
                saveButton.setDisable(true);
                chart.setVisible(true);
                AppActions.getIsRunning().set(true);
                ((AppActions) applicationTemplate.getActionComponent()).getEditToggle().setDisable(true);
                    try {
                        if (!chosen.tocontinue()) {
                            if (stopped.get()) {
                                playButton.setDisable(true);
                                scrnshotButton.setDisable(true);
                                stopped.set(false);
                                synchronized (AppActions.getIsRunning()){
                                    AppActions.getIsRunning().set(true);
                                    AppActions.getIsRunning().notifyAll();}
                                return;
                            }
                        }
                        playButton.setDisable(true);
                        scrnshotButton.setDisable(true);
                        chart.getData().clear();
                        if (((AppActions) (applicationTemplate.getActionComponent())).isLoaded()) {
                            if(chosen instanceof Clusterer){
                                Constructor constructor = chosen.getClass().getConstructor(DataSet.class,int.class,int.class,int.class,boolean.class,AppData.class);
                                chosen = (Clusterer)constructor.newInstance(DataSet.fromTSDFile(((AppActions)applicationTemplate.getActionComponent()).getDataFilePath()),chosen.getMaxIterations(),chosen.getUpdateInterval(),((Clusterer) chosen).getNumberOfClusters(),chosen.tocontinue(),(AppData)applicationTemplate.getDataComponent());
                            }
                                ((AppData) applicationTemplate.getDataComponent()).loadData(AppData.filedata.toString() + AppData.extralines.toString());
                        } else {
                            if(chosen instanceof Clusterer){
                                Constructor constructor = chosen.getClass().getConstructor(DataSet.class,int.class,int.class,int.class,boolean.class,AppData.class);
                                chosen = (Clusterer)constructor.newInstance(TexttoSet(),chosen.getMaxIterations(),chosen.getUpdateInterval(),((Clusterer) chosen).getNumberOfClusters(),chosen.tocontinue(),(AppData)applicationTemplate.getDataComponent());
                            }
                                ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
                        }
                        ((AppData) applicationTemplate.getDataComponent()).displayData();
                        backgroundthread = new Service<Void>() {
                            @Override
                            protected Task<Void> createTask() {
                                return new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        chosen.run();
                                        return null;
                                    }
                                };
                            }
                        };
                        backgroundthread.restart();
                        backgroundthread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent event) {
                                if(chosen.getMaxIterations() != 0){
                                playButton.setDisable(false);}
                                applicationTemplate.getDataComponent().clear();
                                scrnshotButton.setDisable(false);
                                AppActions.getIsRunning().set(false);
                                if(!((AppActions)applicationTemplate.getActionComponent()).isLoaded())    {
                                    saveButton.setDisable(false);}
                                if(chart.isVisible()){
                                    complete.setVisible(true);}
                                ((AppActions) applicationTemplate.getActionComponent()).getEditToggle().setDisable(false);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    }
                    });

        scrnshotButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
                } catch (IOException e) {
                }
            }
        });

        ((AppActions) applicationTemplate.getActionComponent()).getDoneToggle().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppData appData = (AppData) applicationTemplate.getDataComponent();
                try {
                    appData.loadData(textArea.getText());
                    ((AppData) applicationTemplate.getDataComponent()).DataMessage();
                    textArea.setDisable(true);
                    TypeOptions(((AppData) applicationTemplate.getDataComponent()).getProcessor().getLabels().size());
                    appData.clear();
                } catch (Exception e) {
                    appData.clear();
                    ErrorDialog errorDialog = ErrorDialog.getDialog();
                    errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()), e.getMessage());
                    ((AppActions) applicationTemplate.getActionComponent()).getEditToggle().setSelected(true);
                }
            }
        });

        ((AppActions) applicationTemplate.getActionComponent()).getEditToggle().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                displayoption.getChildren().remove(3, displayoption.getChildren().size());
                textArea.setDisable(false);
            }
        });
    }

    public static Button getscrnshotButton() {
        return scrnshotButton;
    }

    public TextArea getTextArea() {
        return AppUI.textArea;
    }

    public void setIsSaved(boolean b) {
        isSaved = b;
    }

    public boolean getIsSaved() {
        return isSaved;
    }

    public VBox getVBox() {
        return displayoption;
    }

    public void TypeOptions(int numOflabels) {
        ToggleGroup types = new ToggleGroup();
        Label algo = new Label(applicationTemplate.manager.getPropertyValue(ALGO.name()));
        ToggleButton classi = new ToggleButton(applicationTemplate.manager.getPropertyValue(CLASS.name()));
        ToggleButton cluster = new ToggleButton(applicationTemplate.manager.getPropertyValue(CLUST.name()));
        classi.getStyleClass().add(applicationTemplate.manager.getPropertyValue(BUTTONMANAGER.name()));
        cluster.getStyleClass().add(applicationTemplate.manager.getPropertyValue(BUTTONMANAGER.name()));
        classi.setToggleGroup(types);
        cluster.setToggleGroup(types);
        displayoption.getChildren().addAll(algo, classi, cluster);
        ToggleGroup algorithms = new ToggleGroup();
        if (numOflabels != 2) {
            classi.setDisable(true);
        }

        classi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Label classTitle = new Label(applicationTemplate.manager.getPropertyValue(CLASS.name()));
                classTitle.setId(applicationTemplate.manager.getPropertyValue(ALGOTITLES.name()));
                if (displayoption.getChildren().get(displayoption.getChildren().size() - 1) instanceof HBox || displayoption.getChildren().get(displayoption.getChildren().size() - 1) == playButton) {
                    //incorporate for loop later
                    displayoption.getChildren().remove(6, displayoption.getChildren().size());
                }
                displayoption.getChildren().add(classTitle);
                    HBox hBox = new HBox();
                    Button confi = new Button();
                    RadioButton radioButton1 = new RadioButton(chosenClass.getClass().getSimpleName());
                    radioButton1.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if( AppUI.getStopped().get() || AppActions.getIsRunning().get()){
                                AppActions.getEndAlgo().set(true);
                                AppUI.getStopped().set(false);
                                synchronized (AppActions.getIsRunning()){
                                    AppActions.getIsRunning().set(true);
                                    AppActions.getIsRunning().notifyAll();}
                            }
                        if(!(displayoption.getChildren().get(displayoption.getChildren().size()-1)==playButton)){
                            displayoption.getChildren().add(playButton);}
                            chosen = chosenClass;
                            if (chosenClass.getMaxIterations() == 0) {
                                    playButton.setDisable(true);
                                } else {
                                    playButton.setDisable(false);
                                }
                        }
                    });
                    confi.setId(applicationTemplate.manager.getPropertyValue(CONFI_BUTTON.name()));
                    confi.setOnAction(e -> ((AppActions) applicationTemplate.getActionComponent()).handleConfiRequest(chosenClass,radioButton1,0));
                    radioButton1.setToggleGroup(algorithms);
                    hBox.getChildren().addAll(radioButton1, confi);
                    hBox.setId(applicationTemplate.manager.getPropertyValue(ALGO_LIST.name()));
                    displayoption.getChildren().addAll(hBox);
            }
        });

        cluster.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Label clustTitle = new Label(applicationTemplate.manager.getPropertyValue(CLUST.name()));
                clustTitle.setId(applicationTemplate.manager.getPropertyValue(ALGOTITLES.name()));
                if (displayoption.getChildren().get(displayoption.getChildren().size() - 1) instanceof HBox || displayoption.getChildren().get(displayoption.getChildren().size() - 1) == playButton) {
                    //incorporate for loop later
                    displayoption.getChildren().remove(6, displayoption.getChildren().size());
                }
                displayoption.getChildren().add(clustTitle);
                for(int i=0;i<chosenClusts.size();i++) {
                    HBox hBox = new HBox();
                    RadioButton radioButton1 = new RadioButton(chosenClusts.get(i).getClass().getSimpleName());
                    Button confi = new Button();
                    int finalI = i;
                    radioButton1.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if( AppUI.getStopped().get() || AppActions.getIsRunning().get()){
                                AppActions.getEndAlgo().set(true);
                                AppUI.getStopped().set(false);
                                synchronized (AppActions.getIsRunning()){
                                    AppActions.getIsRunning().set(true);
                                    AppActions.getIsRunning().notifyAll();}
                            }
                            if(!(displayoption.getChildren().get(displayoption.getChildren().size()-1)==playButton)){
                                displayoption.getChildren().add(playButton);}
                                chosen = chosenClusts.get(finalI);
                            if (chosenClusts.get(finalI).getMaxIterations() == 0) {
                                playButton.setDisable(true);
                            } else {
                                playButton.setDisable(false);
                            }
                        }
                    });
                    confi.setId(applicationTemplate.manager.getPropertyValue(CONFI_BUTTON.name()));
                    confi.setOnAction(e -> ((AppActions) applicationTemplate.getActionComponent()).handleConfiRequest(chosenClusts.get(finalI), radioButton1,finalI));
                    hBox.getChildren().addAll(radioButton1, confi);
                    hBox.setId(applicationTemplate.manager.getPropertyValue(ALGO_LIST.name()));
                    radioButton1.setToggleGroup(algorithms);
                    displayoption.getChildren().add(hBox);
                }
            }
        });
    }

    public Button getsaveButton() {
        return saveButton;
    }

    public ArrayList<Clusterer> getChosenClust(){
        return chosenClusts;
    }

    public void setChosen(Algorithm algorithm){
         chosen = algorithm;
    }

    public void setChosenClass(Classifier classifier){
        chosenClass = classifier;
    }

    public static Button getPlayButton(){
        return playButton;
    }

    public Label getCompleteLabel(){
        return complete;
    }

    public static AtomicBoolean getStopped(){
        return stopped;
    }

    private DataSet TexttoSet(){
        String[] data = textArea.getText().split(AppData.getnewline());
        DataSet dataSet = new DataSet();
        for(String var:data){
            try {
                dataSet.addInstance(var);
            }catch (DataSet.InvalidDataNameException e){}
        }
        return dataSet;
    }

    public static Algorithm getChosen() {
        return chosen;
    }
}
