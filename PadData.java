package data;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PadData extends Emotiv{

    public static Object focus = null, engagement = null, excite = null, stress = null, relaxed = null, interest = null;
    private ArrayList<JSONArray> rawData = new ArrayList<JSONArray>();
    private ArrayList<Object[][]> vectorTable = new ArrayList<Object[][]>();
    private ArrayList<Object[][]> timedVTable = new ArrayList<Object[][]>();    //separate table for time lapse function
    private ArrayList<BigDecimal[]> PADvals = new ArrayList<BigDecimal[]>();    //PAD vals per data point
    private static ArrayList<BigDecimal[]> minPADList = new ArrayList<BigDecimal[]>(); //list of cumulated PAD vals per minute
    public static BigDecimal[] PAD = new BigDecimal[3];
    public static BigDecimal[] timedPAD = new BigDecimal[3];
    private static BigDecimal[] finalPAD = new BigDecimal[]{BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0)};  //stores cumulative PAD value
    private static BigDecimal[] minPAD = new BigDecimal[]{BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0)};   //cumulated PAD vals for one minute, added to minPADList
    private Vector table, timedTable;
    private static String PADZone;
    private static int minCount = 0, Time = 1;
    private boolean minPassed = false; //tracks if a minute has passed

    int item = 0;
    BigDecimal P = BigDecimal.valueOf(0), A = BigDecimal.valueOf(0), D = BigDecimal.valueOf(0);
    BigDecimal p = BigDecimal.valueOf(0), a = BigDecimal.valueOf(0), d = BigDecimal.valueOf(0);  //different pad for time lapse

    private static BigDecimal minP = BigDecimal.valueOf(0), minA = BigDecimal.valueOf(0), minD = BigDecimal.valueOf(0);
    private static BigDecimal timedP = BigDecimal.valueOf(0), timedA = BigDecimal.valueOf(0), timedD = BigDecimal.valueOf(0);

    public PadData(JSONArray array) {
        super("data");
        rawData.add(new JSONArray(array));
        this.focus = array.get(1);
        this.engagement = array.get(3);
        this.excite = array.get(5);
        this.stress = array.get(8);
        this.relaxed = array.get(10);
        this.interest = array.get(12);


        table = new Vector(array);
        vectorTable.add(table.vectors);
        for (Object[][] i : vectorTable){

            for (Object[] n: i) {
                item = 0;
                for (Object temp: n){
                    //iterates through each array to calculate PAD
                    if (item == 0 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        P = P.add(num);
                    }
                    else if (item == 1 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        A = A.add(num);
                    }
                    else if (item == 2 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        D = D.add(num);
                    }
                    item++;
                }
                calculateZone();
            }
            minCount++;
            minP = minP.add(P);
            minA = minA.add(A);
            minD = minD.add(D);
            finalPAD[0] = finalPAD[0].add(P);
            finalPAD[1] = finalPAD[1].add(A);
            finalPAD[2] = finalPAD[2].add(D);
            if (minCount % 6 == 0) {

                minPAD = new BigDecimal[]{minP,minA,minD};
                addMinPAD(minPAD);
                minP = BigDecimal.valueOf(0);
                minA = BigDecimal.valueOf(0);
                minD = BigDecimal.valueOf(0);
                minPassed = true;
                calculateZone();
            }
            if (minPassed) {
                PadData.Time+=1;
                minPassed = false;
            }
            PAD = new BigDecimal[]{P, A, D};

            addPADvals(PAD);
            P = BigDecimal.valueOf(0);
            A = BigDecimal.valueOf(0);
            D = BigDecimal.valueOf(0);
            calculateZone();
            }
 //       displayPAD();
    }

    //GETTERS
    public ArrayList<JSONArray> getRawData() {
        return this.rawData;
    }
    public ArrayList<Object[][]> getVectorTable() {
        return this.vectorTable;
    }
    public BigDecimal[] getPAD() {
        return this.PAD;
    }
    public ArrayList<BigDecimal[]> getPADvals() {
        return this.PADvals;
    }
    public BigDecimal[] getMinPAD() { return this.minPAD;}
//    public Vector getTable() {
//        return this.table;
//    }
    public static String getPADZone() {
        return PADZone;
    }
    public static BigDecimal[] getFinalPAD() {
        return finalPAD;
    }

    //ADDERS
    public void addPADvals(BigDecimal[] pad) {
        this.PADvals.add(pad);
    }
    public void addMinPAD(BigDecimal[] minPad) {
        PadData.minPADList.add(minPad);
    }


    //Getters for Affective States
    public static Object getFocus() {return focus;}
    public static Object getEngage() {
        return engagement;
    }
    public static Object getExcite() {
        return excite;
    }
    public static Object getStress() {
        return stress;
    }
    public static Object getRelaxed() {
        return relaxed;
    }
    public static Object getInterest() {
        return interest;
    }
    public void displayPAD() {
        for (BigDecimal[] pad: PADvals) {
            System.out.println("[ " + pad[0] + ", " + pad[1] +", " + pad[2] + " ]");
        }
    }
    public void reportDisconnect(JSONArray array) {
        if (array.get(1).equals(null) && array.get(3).equals(null) && array.get(5).equals(null) && array.get(8).equals(null) && array.get(10).equals(null) && array.get(12).equals(null)){
             System.out.println("Error: Headset is Disconnected");
        }
    }

    public void calculateZone() {
        if (P.compareTo(BigDecimal.valueOf(0)) > 0 && A.compareTo(BigDecimal.valueOf(0)) > 0 && D.compareTo(BigDecimal.valueOf(0)) > 0){
            this.PADZone = "Engagement";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) > 0 && A.compareTo(BigDecimal.valueOf(0)) > 0 && D.compareTo(BigDecimal.valueOf(0)) < 0){
            this.PADZone = "Excitement";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) < 0 && A.compareTo(BigDecimal.valueOf(0)) > 0 && D.compareTo(BigDecimal.valueOf(0)) < 0){
            this.PADZone = "Frustration";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) < 0 && A.compareTo(BigDecimal.valueOf(0)) > 0 && D.compareTo(BigDecimal.valueOf(0)) > 0){
            this.PADZone = "Disagreement/Hostility";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) < 0 && A.compareTo(BigDecimal.valueOf(0)) < 0 && D.compareTo(BigDecimal.valueOf(0)) < 0){
            this.PADZone = "Boredom";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) < 0 && A.compareTo(BigDecimal.valueOf(0)) < 0 && D.compareTo(BigDecimal.valueOf(0)) > 0){
            this.PADZone = "Disdain";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) > 0 && A.compareTo(BigDecimal.valueOf(0)) < 0 && D.compareTo(BigDecimal.valueOf(0)) > 0){
            this.PADZone = "Meditation";
        }
        else if (P.compareTo(BigDecimal.valueOf(0)) > 0 && A.compareTo(BigDecimal.valueOf(0)) < 0 && D.compareTo(BigDecimal.valueOf(0)) > 0){
            this.PADZone = "Docility";
        }
    }

    public void timeLapse(int min, JSONArray array) {
        int dataCount = 0, minCount = 0;
        timedTable = new Vector(array);
        timedVTable.add(timedTable.vectors);

        System.out.println("Time Lapse for " + min + " minute(s) has started.");
        for (Object[][] i : timedVTable){

            for (Object[] n: i) {
                item = 0;
                for (Object temp: n){
                    if (item == 0 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        p = p.add(num);
                    }
                    else if (item == 1 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        a = a.add(num);
                    }
                    else if (item == 2 && temp instanceof BigDecimal) {
                        BigDecimal num = (BigDecimal) temp;
                        d = d.add(num);
                    }
                    item++;
                }
            }
            dataCount++;
            timedP = timedP.add(p);
            timedA = timedA.add(a);
            timedD = timedD.add(d);
            if (dataCount % 6 == 0) {
                minCount++;
                if (minCount == min) {
                    System.out.println("Final PAD of Time Lapse: " + "[" + timedP+ ", " + timedA + ", " + timedD + "]");
                    break;
                }
            }

        }
    }


    public String toString() {
        return "PAD Zone: " + getPADZone() + "\n" +
                "Current PAD: " + "[" + this.getPAD()[0] + ", " +this.getPAD()[1] + ", " + this.getPAD()[2]  + "]" + "\n" +
                "Cumulative PAD for Minute " + PadData.Time + ": [" + this.getMinPAD()[0]  + ", " +this.getMinPAD()[1] + ", " + this.getMinPAD()[2] + "]" + "\n" +
                "Final PAD: " + "[" + this.getFinalPAD()[0] + ", " + this.getFinalPAD()[1] + ", " + this.getFinalPAD()[2] + "]" + "\n";
    }


}
