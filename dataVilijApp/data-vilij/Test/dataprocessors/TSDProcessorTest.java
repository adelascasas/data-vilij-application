package dataprocessors;

import javafx.geometry.Point2D;
import org.junit.Test;
import vilij.propertymanager.PropertyManager;

import java.lang.ref.PhantomReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static settings.AppPropertyTypes.*;

public class TSDProcessorTest {

    /**
     *This method implements the processString method with the input Strings
     * that represent instances of a proper TSD format, where the processString
     *  method will return true
     *
     */
    @Test
    public void processValidString() {
        String valid1 = "@1921:Margaret_Gorman\tnull\t61,108";
        String valid2 = "@1926:Norma_Smallwood\tlabel8\t64,118";
       assertTrue(processString(valid1));
       assertTrue(processString(valid2));
    }

    /**
     * This method implements the processString method with the input Strings that represent
     * instances of a proper TSD format that incorporates boundary values
     * The String boundary1 instance represents a boundary value where the instance name of the
     * tsd formatted line(first part of the tab separated line) will only be valid if it at least
     * contains the @ character
     * The String boundary2 instance represents a boundary value where the label name of the tsd formatted
     * line(second part of the tab separated line) will only be valid if it at least contains one character
     */
    @Test
    public void processBoundariesString(){
        String boundary1 = "@\tlabel1\t0,0";
        String boundary2 = "@1\ta\t5,6";
       assertTrue( processString(boundary1));
        assertTrue(processString(boundary2));
    }

    /**
     * This method implements the processString method with the input Strings
     * that represent invalid instances of a TSD format,where the processString
     * method will return false
     * The String invalid1 represents a instance of a TSD file line where the third part of the tab separated
     * format contains a comma but they do not contain valid numbers
     * The String invalid2 represents a instance of a TSD file line where the instance name of the TSD format line
     * does not start with the @ character
     * The String invalid3 represents a instance of a TSD file line where the TSD format line
     * is only separated by two tabs while the proper format contains three tabs
     * The String invalid4 represents a instance of a TSD file where the coordinates of the TSD formatted line
     * (third part of the tab separated line) only contains a number where it should actually contain only two numbers separated by
     * a comma
     *
     */
    @Test
    public void processInvalidString(){
        String invalid1 = "@2\tlabel1\they,hi";
        String invalid2 = "1\tlabel1\t5,6";
        String invalid3 = "@1\t5,6";
        String invalid4 = "@\tlabel1\t2";
        assertFalse(processString(invalid1));
        assertFalse(processString(invalid2));
        assertFalse(processString(invalid3));
        assertFalse(processString(invalid4));
    }

    /**
     *
     * @param tsdString
     * @return true when the String is a valid tsd String, false when the String is not a
     * valid tsdString
     * This method is a representation of the processString method in TSDProcessor, with the only change that
     * the method is boolean instead of void in order for the method to justify whether the String is valid
     */

    private boolean processString(String tsdString){
        String[] check = tsdString.split("\t");
        if(check.length != 3){
            return false;
        }
        if(check[0].charAt(0)!='@'){
            return false;
        }
        String[] coordinate = check[2].split(",");
        if(coordinate.length != 2){
            return false;
        }
        try {
            Integer.parseInt(coordinate[0]);
            Integer.parseInt(coordinate[1]);
        }catch (NumberFormatException e){return false;}
            return true;
    }
}