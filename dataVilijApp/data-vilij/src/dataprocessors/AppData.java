package dataprocessors;
import actions.AppActions;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static settings.AppPropertyTypes.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    public static StringBuffer filedata = new StringBuffer();
    public static StringBuffer extralines = new StringBuffer();
    private String error1;
    private String error2;
    private static String newline = "\n";
    private int numOfInst;
    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        try{
            clear();
            if(((AppActions) (applicationTemplate.getActionComponent())).isLoaded()){
                error1 = filedata.toString();
                error2 = extralines.toString();
            }
            String checkdata;
            filedata.setLength(0);
            extralines.setLength(0);
            int numOflines = 0;
            BufferedReader reader = new BufferedReader(new FileReader(dataFilePath.toFile()));
            while((checkdata = reader.readLine()) != null){
                if(numOflines>=10){
                   extralines.append(checkdata);
                   extralines.append(newline);
                }
                else{
                    filedata.append(checkdata);
                    filedata.append(newline);
                }
                numOflines++;
            }
            reader.close();
            loadData(filedata.toString()+extralines.toString());
            ((AppUI)applicationTemplate.getUIComponent()).setIsSaved(true);
            ((AppUI)applicationTemplate.getUIComponent()).getVBox().setVisible(true);
            ((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().remove(2, ((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().size());
            ((AppActions)applicationTemplate.getActionComponent()).setDataFilePath(dataFilePath);
            DataMessage();
            ((AppUI)applicationTemplate.getUIComponent()).TypeOptions(processor.getLabels().size());
            clear();
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setText(filedata.toString());
            ((AppActions)applicationTemplate.getActionComponent()).setLoaded(true);
        }catch (Exception e){
            filedata.setLength(0);
            extralines.setLength(0);
            clear();
            if(((AppActions) (applicationTemplate.getActionComponent())).isLoaded()){
                filedata.append(error1);
                extralines.append(error2);
            }
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()),e.getMessage());
        }
        }

    public void loadData(String dataString) throws Exception {
            processor.processString(dataString);
    }

    @Override
    public void saveData(Path dataFilePath) {
        File file = dataFilePath.toFile();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
        try {
            if(extralines.length()==0){
                loadData(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            }
            else{
                loadData(filedata.toString()+extralines.toString());
            }
            clear();
            try {
                FileWriter writer = new FileWriter(file);
                if (extralines.length() == 0) {
                    writer.write(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                } else {
                    writer.write(filedata.toString() + extralines.toString());
                }
                writer.close();
            } catch (NullPointerException e) {
            } catch (IOException e) {
            }
        }catch (Exception e){
            AppActions.loaderror = 1;
            clear();
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(applicationTemplate.manager.getPropertyValue(INVALID_FORMAT.name()), e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(AppUI.getChart());
    }

    public TSDProcessor getProcessor() {
        return processor;
    }

    public static String getnewline(){
        return newline;
    }

    public void DataMessage(){
        StringBuilder text = new StringBuilder();
        numOfInst = processor.getDatalabels().size();
        text.append(processor.getDatalabels().size() + applicationTemplate.manager.getPropertyValue(INSTOF.name())+ processor.getLabels().size() + applicationTemplate.manager.getPropertyValue(LABELS.name()));
        if(((AppUI)applicationTemplate.getUIComponent()).getIsSaved()){
        text.append(applicationTemplate.manager.getPropertyValue(DATAFROM.name())+newline+newline+((AppActions)applicationTemplate.getActionComponent()).getDataFilePath().toFile().getAbsolutePath()+applicationTemplate.manager.getPropertyValue(DECIMAL.name())+newline);}
        text.append(newline+applicationTemplate.manager.getPropertyValue(LABELSFROM.name())+newline);
        Iterator<String> iterator = processor.getLabels().iterator();
        for(int i=0;i<processor.getLabels().size();i++){
            text.append(applicationTemplate.manager.getPropertyValue(DASH.name())+iterator.next()+newline);
        }
        Label datainfo = new Label(text.toString());
        datainfo.setId(applicationTemplate.manager.getPropertyValue(DATAMESS.name()));
        ((AppUI)applicationTemplate.getUIComponent()).getVBox().getChildren().add(datainfo);
        ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
    }

    public void setDataSet(Map<String,String> labels,Map<String,Point2D>coordinates){
        processor.setData(labels,coordinates);
    }

    public int getNumOfInst(){
        return numOfInst;
    }
}
