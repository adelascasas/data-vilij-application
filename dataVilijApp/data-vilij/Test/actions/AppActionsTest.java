package actions;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppActionsTest {

    /**
     * This method tests valid configurations that pertain to each kind of
     * algorithm type. In this case the algorithm types have configurations that
     * are represented by String arrays with the same name and a boolean value
     * The first number string in the array represents the max iterations, the second
     * string the update intervals and the third that only pertains to Clusterer algorithms
     * represents the number of clusters
     */
    @Test
    public void TestValidConfigurations(){
        String[]KMeansClusterer = {"10","2","3"};
        String[]RandomClusterer = {"20","4","2"};
        String[]RandomClassifier = {"30","3"};
        assertTrue(handleConfiRequest(KMeansClusterer,false));
        assertTrue(handleConfiRequest(RandomClusterer,true));
        assertTrue(handleConfiRequest(RandomClassifier,false));
    }

    /**
     * This method tests out boundary values for each field in the
     * configuration. The KMeansClusterer String[] represents the KMeansClusterer algorithm
     * which has a maxiterations of one which is the lowest value the number of maxiterations
     * can be. The RandomClusterer String array which represents the RandomClusterer algorithm
     * has a number of clusters value of 2 which is one of the boundaries of the value since that
     * value can only be between 2 and 4. The RandomClassifier String array  has a update Interval
     * value of one which is the lowest that value can be. All of these boundary values are valid
     *The continuous option can either be false or true
     */
    @Test
    public void TestBoundaryConfigurations(){
        String[]KMeansClusterer = {"1","2","3"};
        String[]RandomClusterer = {"20","4","2"};
        String[]RandomClassifier = {"30","1"};
        assertTrue(handleConfiRequest(KMeansClusterer,false));
        assertTrue(handleConfiRequest(RandomClusterer,true));
        assertTrue(handleConfiRequest(RandomClassifier,false));
    }

    /**
     * This kind of algorithm represents the invalid inputs of the configurations that you would
     * get from a configuration window textareas. The KMeansClusterer has Strings that don't pertain to valid number values
     * so the configuration is incorrect and the handleconfirequest will throw a NumberFormatException. The RandomCLusterer
     * String array contains a number of clusters value of 29 which is invalid since that value can only be between
     * 2 and 4. The RandomClassifier has a update interval value of 0 which is invalid since the lowest that value can be is one
     */
    @Test(expected = NumberFormatException.class)
    public void TestInvalidConfigurations(){
        String[]KMeansClusterer = {"hello","hi","no"};
        String[]RandomClusterer = {"20","4","29"};
        String[]RandomClassifier = {"30","0"};
        handleConfiRequest(KMeansClusterer,false);
        assertFalse(handleConfiRequest(RandomClusterer,true));
        assertFalse(handleConfiRequest(RandomClassifier,false));
    }

    /**
     * This method is a representation of the handleconfiRequest in the AppActions class. This part of
     * the method checks whether the inputted values for the configuration that pertains to each kind of algorithm
     * is valid.
     *
     * @param confi This String array represents the max iterations, update interval, and number of clusters
     *  that would be taken as Strings from the configuration window textAreas.
     * @param continuous This boolean value represents whether the algorithm wants to be continuous or non continuous
     * @return return true if the configuration that is inputted is valid for the specified algorithm
     */
    private boolean handleConfiRequest(String[] confi,boolean continuous) {
        String maXiterations = confi[0];
        String updIntervals = confi[1];
        String clusters = "";
        if (confi.length == 3) {
            clusters = confi[2];
        }
        if (Integer.parseInt(maXiterations) <= 0) {
            return false;
        }
        if (Integer.parseInt(updIntervals) <= 0) {
            return false;
        }
        if (confi.length == 3) {
            if (Integer.parseInt(clusters) < 2 || Integer.parseInt(clusters)>4) {
                return false;
            }
        }
        return true;
    }




}