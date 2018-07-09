package actions;
import algorithms.*;
import dataprocessors.AppData;
import dataprocessors.DataSet;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ui.*;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;
import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.LOAD_ERROR_TITLE;
import static vilij.settings.PropertyTypes.SAVE_ERROR_MSG;
import static vilij.settings.PropertyTypes.SAVE_ERROR_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {
    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;
    private ToggleButton edit;
    private ToggleButton done;
    private String data;

    /** Path to the data file currently active. */
    private Path dataFilePath;

    public static int loaderror;
    private static boolean loaded;
    private  final static AtomicBoolean isRunning = new AtomicBoolean(false);
    private final static AtomicBoolean endAlgo = new AtomicBoolean(false);

    public AppActions(ApplicationTemplate applicationTemplate) {
        loaded = false;
        this.applicationTemplate = applicationTemplate;
        edit = new ToggleButton(applicationTemplate.manager.getPropertyValue(EDIT.name()));
        done = new ToggleButton(applicationTemplate.manager.getPropertyValue(DONE.name()));
        edit.getStyleClass().add(applicationTemplate.manager.getPropertyValue(BUTTONMANAGER.name()));
        done.getStyleClass().add(applicationTemplate.manager.getPropertyValue(BUTTONMANAGER.name()));
    }

    @Override
    public void handleNewRequest() {
        if(isRunning.get()){
            RunningDialogue(3);
        }
        else if(AppUI.getStopped().get()){
            AppActions.getEndAlgo().set(true);
            AppUI.getStopped().set(false);
            synchronized (isRunning){
                isRunning.set(true);
                isRunning.notifyAll();}
            newrequest();
        }
        else{
            newrequest();
        }
        }

    public void newrequest(){
        if(AppUI.getStopped().get()){
            AppUI.getStopped().set(false);
        }
        try {
            boolean cancelled = true;
            if(((AppUI)applicationTemplate.getUIComponent()).getVBox().isVisible()){
                cancelled = promptToSave();
            }
            if(!cancelled){
                return;
            }
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().remove(2,((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().size());
            ((AppUI)applicationTemplate.getUIComponent()).getVBox().setVisible(true);
            HBox choose = new HBox(edit,done);
            ((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().add(choose);
            ToggleGroup editoptions = new ToggleGroup();
            edit.setToggleGroup(editoptions);
            done.setToggleGroup(editoptions);
            editoptions.selectedToggleProperty().addListener((observable, oldValue, newValue) ->{
                if(edit.isSelected()){edit.setDisable(true);}
                else {edit.setDisable(false);}
                if(done.isSelected()){
                    done.setDisable(true);}
                else{done.setDisable(false);}
            } );
            edit.setSelected(true);
            AppUI.getChart().setVisible(false);
            ((AppUI) applicationTemplate.getUIComponent()).getCompleteLabel().setVisible(false);
            setLoaded(false);
        }catch(IOException e){
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),applicationTemplate.manager.getPropertyValue(SAVE_ERROR_MSG.name()) + applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.name()));
        }
    }

    @Override
    public void handleSaveRequest() {
        FileChooser fileChooser = new FileChooser();
        try {
            data = Paths.get(applicationTemplate.manager.getPropertyValue(DECIMAL.name()),applicationTemplate.manager.getPropertyValue(HW.name()),applicationTemplate.manager.getPropertyValue(VILIJ.name()),applicationTemplate.manager.getPropertyValue(RESOURCES.name()),applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name())).toString();
            fileChooser.setInitialDirectory(new File(data));
        }catch (Exception e){
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
        }
        if(((AppUI)applicationTemplate.getUIComponent()).getIsSaved()){
            (applicationTemplate.getDataComponent()).saveData(dataFilePath);
        }
        else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
            File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            try {
                FileWriter writer = new FileWriter(file);
                writer.write(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                writer.close();
                dataFilePath = Paths.get(file.getPath());
                ((AppUI) applicationTemplate.getUIComponent()).setIsSaved(true);
            } catch (NullPointerException e) {
            } catch (IOException e) {
            }
        }
        }

    @Override
    public void handleLoadRequest() {
        if(isRunning.get()){
            RunningDialogue(2);
        }
        else if(AppUI.getStopped().get()){
            AppActions.getEndAlgo().set(true);
                AppUI.getStopped().set(false);
            synchronized (isRunning){
                isRunning.set(true);
                isRunning.notifyAll();}
                loadrequest();
        }
        else{
            loadrequest();
        }
    }

    public void loadrequest(){
        if(AppUI.getStopped().get()){
            AppUI.getStopped().set(false);
        }
        FileChooser fileChooser = new FileChooser();
        String data = Paths.get(applicationTemplate.manager.getPropertyValue(DECIMAL.name()),applicationTemplate.manager.getPropertyValue(HW.name()),applicationTemplate.manager.getPropertyValue(VILIJ.name()),applicationTemplate.manager.getPropertyValue(RESOURCES.name()),applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name())).toString();
        try {
            fileChooser.setInitialDirectory(new File(data));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));

        }catch (Exception e){
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(applicationTemplate.manager.getPropertyValue(LOAD_ERROR_TITLE.name()),applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
        try{
            File file = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            (applicationTemplate.getDataComponent()).loadData(file.toPath());
            ((AppUI)applicationTemplate.getUIComponent()).getsaveButton().setDisable(true);
            AppUI.getChart().setVisible(false);
            ((AppUI) applicationTemplate.getUIComponent()).getCompleteLabel().setVisible(false);
        }catch (NullPointerException e){ }
    }

    @Override
    public void handleExitRequest() {
        if(!((AppUI)applicationTemplate.getUIComponent()).getVBox().isVisible()){
            applicationTemplate.getUIComponent().getPrimaryWindow().close();
            return;
        }
        if(isRunning.get()){
            RunningDialogue(1);
        }
        else if(!(isLoaded()) && !((AppUI)applicationTemplate.getUIComponent()).getIsSaved()){
            ConfirmationDialog confirmationDialog = ConfirmationDialog.getDialog();
            confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()),applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
            if(confirmationDialog.getSelectedOption()== ConfirmationDialog.Option.CANCEL || confirmationDialog.getSelectedOption()==null){
                return;
            }
            else if(confirmationDialog.getSelectedOption()==ConfirmationDialog.Option.NO){
                AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                appUi.getPrimaryWindow().close();
            }
            else{
                try {
                    ((AppData)applicationTemplate.getDataComponent()).loadData(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                    applicationTemplate.getDataComponent().clear();
                }catch (Exception e){
                applicationTemplate.getDataComponent().clear();
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()), e.getMessage());
                return;
                }
                FileChooser fileChooser = new FileChooser();
                String data = Paths.get(applicationTemplate.manager.getPropertyValue(DECIMAL.name()),applicationTemplate.manager.getPropertyValue(HW.name()),applicationTemplate.manager.getPropertyValue(VILIJ.name()),applicationTemplate.manager.getPropertyValue(RESOURCES.name()),applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name())).toString();
                try {
                    fileChooser.setInitialDirectory(new File(data));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
                }catch (Exception e){
                    ErrorDialog errorDialog = ErrorDialog.getDialog();
                    errorDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
                }
                try{
                File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                FileWriter writer = new FileWriter(file);
                writer.write(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                writer.close();
                AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                appUi.getPrimaryWindow().close();
                }catch (Exception e){}
            }
            }
        else{
        AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
        appUi.getPrimaryWindow().close();}

    }

    @Override
    public void handlePrintRequest() {
    }

    public void handleScreenshotRequest() throws IOException {
        FileChooser fileChooser = new FileChooser();
        data = Paths.get(applicationTemplate.manager.getPropertyValue(DECIMAL.name()),applicationTemplate.manager.getPropertyValue(HW.name()),applicationTemplate.manager.getPropertyValue(VILIJ.name()),applicationTemplate.manager.getPropertyValue(RESOURCES.name()),applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name())).toString();
        fileChooser.setInitialDirectory(new File(data));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(GRAPHICS_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(GRAPHICS_FILE_EXT.name())));
        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        WritableImage writableImage = AppUI.getChart().snapshot(new SnapshotParameters(),null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), applicationTemplate.manager.getPropertyValue(PIC_FORMAT.name()), file);
        } catch (IOException  | IllegalArgumentException e ) { }
        }



        public void handleConfiRequest(Algorithm algorithm, RadioButton button, int i) {
            if(AppActions.getIsRunning().get()){
                return;
            }
            if( AppUI.getStopped().get()){
            AppActions.getEndAlgo().set(true);
            AppUI.getStopped().set(false);
            synchronized (isRunning){
                isRunning.set(true);
                isRunning.notifyAll();}
            }
            Stage secondaryStage = new Stage();
            VBox algorun = new VBox();
            Label title = new Label(applicationTemplate.manager.getPropertyValue(ALGOCONFI.name()));
            TextArea maXiter = new TextArea();
            maXiter.setId(applicationTemplate.manager.getPropertyValue(SAMPLE.name()));
            Label iterTitle = new Label(applicationTemplate.manager.getPropertyValue(MAXITER.name()));
            HBox category1 = new HBox(iterTitle, maXiter);
            Label interTitle = new Label(applicationTemplate.manager.getPropertyValue(UPINTER.name()));
            TextArea updInter = new TextArea();
            updInter.setId(applicationTemplate.manager.getPropertyValue(SAMPLE.name()));
            HBox category2 = new HBox(interTitle, updInter);
            CheckBox checkBox = new CheckBox();
            checkBox.setText(applicationTemplate.manager.getPropertyValue(CONTRUN.name()));
            Label labelnames = new Label(applicationTemplate.manager.getPropertyValue(NUMOFLABELS.name()));
            TextArea updLabels = new TextArea();
            updLabels.setId(applicationTemplate.manager.getPropertyValue(SAMPLE.name()));
            HBox category3 = new HBox();
            category3.getChildren().addAll(labelnames, updLabels);
            Button complete = new Button(applicationTemplate.manager.getPropertyValue(DONE.name()));
                try{
                    if(algorithm.getMaxIterations()>0 && algorithm.getUpdateInterval()>0) {
                        updInter.setText(algorithm.getUpdateInterval() + "");
                        maXiter.setText(algorithm.getMaxIterations() + "");
                        if (algorithm instanceof Clusterer) {
                            updLabels.setText(((Clusterer) algorithm).getNumberOfClusters() + "");
                        }
                        if (algorithm.tocontinue()) {
                            checkBox.setSelected(true);
                        } else {
                            checkBox.setSelected(false);
                        }
                    }
                }catch (NullPointerException e){}
                if(algorithm instanceof Clusterer) {
                    algorun.getChildren().addAll(title, category1, category2, category3, checkBox,complete);
                }
                else{
                    algorun.getChildren().addAll(title, category1, category2, checkBox,complete);
                }
            algorun.getStyleClass().add(applicationTemplate.manager.getPropertyValue(CONFI.name()));
            Scene scene = new Scene(algorun);
            scene.getStylesheets().add(applicationTemplate.manager.getPropertyValue(STYLE_SHEET.name()));
            secondaryStage.setScene(scene);
            secondaryStage.initModality(Modality.WINDOW_MODAL);
            secondaryStage.initOwner(applicationTemplate.getUIComponent().getPrimaryWindow());
            secondaryStage.show();
            AtomicInteger section1 = new AtomicInteger(1);
            AtomicInteger section2  = new AtomicInteger(1);
            AtomicInteger section3 = new AtomicInteger(1);
            AtomicBoolean section4 = new AtomicBoolean(false);
                complete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                                try {
                                    if (Integer.parseInt(updInter.getText())>0) {
                                        section1.set(Integer.parseInt(updInter.getText()));
                                    }
                                }catch (NumberFormatException e){}
                                try{
                                    if(Integer.parseInt(maXiter.getText())>0){
                                        section2.set(Integer.parseInt(maXiter.getText()));
                                    }
                                }catch (NumberFormatException e){ }

                                if(algorithm instanceof Clusterer) {
                                    try {
                                        if (Integer.parseInt(updLabels.getText()) > 0 && Integer.parseInt(updLabels.getText()) <= ((AppData)applicationTemplate.getDataComponent()).getNumOfInst() ) {
                                            section3.set(Integer.parseInt(updLabels.getText()));
                                        }
                                    } catch (NumberFormatException e) { }
                                }
                            if(checkBox.isSelected()){
                                    section4.set(true);
                            }
                             Constructor constructor;
                     try {
                         if (algorithm instanceof Classifier) {
                             constructor = algorithm.getClass().getConstructor(DataSet.class, int.class, int.class, boolean.class);
                             Classifier classifier = (Classifier) constructor.newInstance(new DataSet(), section2.get(), section1.get(), section4.get());
                             ((AppUI) applicationTemplate.getUIComponent()).setChosenClass(classifier);
                             if (button.isSelected()) {
                                 ((AppUI) applicationTemplate.getUIComponent()).setChosen(classifier);
                             }
                         } else {
                             constructor = algorithm.getClass().getConstructor(DataSet.class, int.class, int.class,int.class, boolean.class,AppData.class);
                              Clusterer clusterer = (Clusterer) constructor.newInstance(new DataSet(), section2.get(), section1.get(), section3.get(), section4.get(),((AppData)applicationTemplate.getDataComponent()));
                             ((AppUI) applicationTemplate.getUIComponent()).getChosenClust().set(i,clusterer );
                             if (button.isSelected()) {
                                 ((AppUI) applicationTemplate.getUIComponent()).setChosen(((AppUI) applicationTemplate.getUIComponent()).getChosenClust().get(i));
                             }
                         }
                     }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){}
                        if(button.isSelected()){
                            AppUI.getPlayButton().setDisable(false);
                        }
                            secondaryStage.close();
                    }
                });
            secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    secondaryStage.close();
                }
            });
            }



    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        FileChooser fileChooser = new FileChooser();
        boolean SaveOption = true;
        if(loaded){
            ((AppUI) applicationTemplate.getUIComponent()).setIsSaved(false);
            AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
            appUi.clear();
            AppUI.getscrnshotButton().setDisable(true);
            return SaveOption;
        }
        ConfirmationDialog confirmationDialog = ConfirmationDialog.getDialog();
        confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()),applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        if(confirmationDialog.getSelectedOption()== ConfirmationDialog.Option.CANCEL || confirmationDialog.getSelectedOption()==null){
            SaveOption = false;
        }
        else if(confirmationDialog.getSelectedOption()==ConfirmationDialog.Option.YES){
                AppData.filedata.setLength(0);
                AppData.extralines.setLength(0);
            AppUI.getscrnshotButton().setDisable(true);
            String data = Paths.get(applicationTemplate.manager.getPropertyValue(DECIMAL.name()),applicationTemplate.manager.getPropertyValue(HW.name()),applicationTemplate.manager.getPropertyValue(VILIJ.name()),applicationTemplate.manager.getPropertyValue(RESOURCES.name()),applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name())).toString();
            try {
                fileChooser.setInitialDirectory(new File(data));
            }catch (Exception e){
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
            }
            if(((AppUI)applicationTemplate.getUIComponent()).getIsSaved()){
                loaderror = 0;
                (applicationTemplate.getDataComponent()).saveData(dataFilePath);
                if(loaderror==0){
                    ((AppUI) applicationTemplate.getUIComponent()).setIsSaved(false);
                    AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                appUi.clear();
                    AppUI.getscrnshotButton().setDisable(true);
                }
            }
            else{
                AppData appData = (AppData) applicationTemplate.getDataComponent();
                try {
                    appData.loadData(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
                    appData.clear();
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
                    File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    try {
                        FileWriter writer = new FileWriter(file);
                        writer.write(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                        writer.close();
                        dataFilePath = Paths.get(file.getPath());
                        AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                        appUi.clear();
                    } catch (NullPointerException e) {
                        AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                        appUi.clear();
                    }
                    ((AppUI) applicationTemplate.getUIComponent()).setIsSaved(false);
                    AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                    appUi.clear();
                    AppUI.getscrnshotButton().setDisable(true);
                }catch (Exception e){
                    appData.clear();
                    ErrorDialog errorDialog = ErrorDialog.getDialog();
                    errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()), e.getMessage());
                }
                }
        }
        else if(confirmationDialog.getSelectedOption()==ConfirmationDialog.Option.NO){
                AppData.filedata.setLength(0);
                AppData.extralines.setLength(0);
            ((AppUI) applicationTemplate.getUIComponent()).setIsSaved(false);
            AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
            appUi.clear();
            }
        return SaveOption;
    }

    public ToggleButton getEditToggle(){
        return edit;
    }

    public ToggleButton getDoneToggle(){
        return done;
    }

    public Path getDataFilePath(){ return dataFilePath;}

    public void setDataFilePath(Path dataFilePath){this.dataFilePath = dataFilePath;}

    public void setLoaded(boolean b){
        loaded = b;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public static AtomicBoolean getIsRunning(){
        return isRunning;
    }

    public void RunningDialogue(int request){
        isRunning.set(false);
        Stage secondaryStage = new Stage();
        String labelname;
        if(request==1){
            labelname = applicationTemplate.manager.getPropertyValue(EXIT_WHILE_RUNNING_WARNING.name());
        }
        else{
        labelname = applicationTemplate.manager.getPropertyValue(ALGO_RUNNING.name());}
        Label runninglabel = new Label(labelname);
        VBox AlgoRun = new VBox(runninglabel);
        Button yes = new Button(ConfirmationDialog.Option.YES.name());
        Button no = new Button(ConfirmationDialog.Option.NO.name());
        HBox Options = new HBox(yes,no);
        Options.setId(applicationTemplate.manager.getPropertyValue(EXIT_REQUEST.name()));
        Options.getStyleClass().add(applicationTemplate.manager.getPropertyValue(BUTTONMANAGER.name()));
        yes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(request==1){
                AppUI appUi = (AppUI) applicationTemplate.getUIComponent();
                appUi.getPrimaryWindow().close();
                }
                else{
                    secondaryStage.close();
                    AppActions.getEndAlgo().set(true);
                    synchronized (isRunning){
                        isRunning.set(true);
                        isRunning.notifyAll();}
                    if(request==2){
                        loadrequest();
                    }
                    else{
                        newrequest();
                    }
                }
            }
        });
        no.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!AppUI.getStopped().get()){
                synchronized (isRunning){
                    isRunning.set(true);
                    isRunning.notifyAll();}}
                secondaryStage.close();
            }
        });
        secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                synchronized (isRunning){
                    isRunning.set(true);
                    isRunning.notifyAll();}
                secondaryStage.close();
            }
        });
        AlgoRun.getChildren().add(Options);
        Scene scene = new Scene(AlgoRun);
        scene.getStylesheets().add(applicationTemplate.manager.getPropertyValue(STYLE_SHEET.name()));
        secondaryStage.setScene(scene);
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(applicationTemplate.getUIComponent().getPrimaryWindow());
        secondaryStage.show();
    }

    public static AtomicBoolean getEndAlgo(){
        return endAlgo;
    }

}
