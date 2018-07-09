package dataprocessors;

import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import java.io.*;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppDataTest {
    File file;


    /**
     * This method tests out the saving aspect of the application
     * It calls a method that saves a valid tsd formatted file and
     * the loads up the line that was just saved from that file
     * The loaded line is then compared to the line that was saved
     * to see if saving was a success
     */
    @Test
    public void TestsaveData(){
        String valid = "@1921:Margaret_Gorman\tnull\t61,108";
        file = new File(Paths.get("resources","data","Junittest.tsd").toAbsolutePath().toString());
        saveData(valid);
        assertTrue(valid.equals(loadData()));
    }

    /**
     * This method takes in a tsd formatted line and saves it to a file
     * This method represents the saveData method in the AppData class and
     * only incorporates the saving feature of the method
     * @param valid
     */
    private void saveData(String valid) {
        try {
            FileWriter writer = new FileWriter(file,false);
            writer.write(valid);
            writer.close();
        }catch (IOException e){}
    }

    /**
     * This method is used to acquire the tsd formatted line that has just been saved
     * in order to check if the line was saved successfully
     * @return a String that has been read from a file that
     * contains a single saved tsd formatted line
     */
    private String loadData(){
        String readLine = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            readLine = reader.readLine();
        }catch (IOException e){}
   //     if (file.exists() && file.isFile())
   //     {
    //        file.delete();
     //   }
      //  try {
      //      file.createNewFile();
       // }catch (IOException e){}
        return readLine;
    }
}