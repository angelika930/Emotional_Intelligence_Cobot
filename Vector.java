package data;

import org.json.JSONArray;

import java.math.BigDecimal;

public class Vector {

    Object[] temp = new Object[3];
    Object[][] vectors = new Object[6][];
    int count = 0, vectorCount = 0, nullCount = 0;
    public Vector(JSONArray array) {
        for (Object o : array) {
            if ( (count == 1 || count == 3 || count == 5 || count == 8 || count == 10 || count == 12) && o.equals(null)) {
                temp = new Object[]{BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0)};    //if headset does not pick up data then value is set to 0
                nullCount++;
            }
            else if (count == 1) {     //Focus
                BigDecimal num = (BigDecimal) o;
                temp = new Object[]{o, num.negate(), o};
            }
            else if (count == 3 ) {    //Engagement
                temp = new Object[]{o, o, o};
            } else if (count == 5 && o instanceof BigDecimal) {  //Excitement
                BigDecimal num = (BigDecimal) o;
                temp = new Object[]{o, o, num.negate()};
            } else if (count == 8 && o instanceof BigDecimal) {  //Stress
                BigDecimal num = (BigDecimal) o;
                temp = new Object[]{num.negate(), o, num.negate()};
            } else if (count == 10 && o instanceof BigDecimal) {  //Relaxation
                BigDecimal num = (BigDecimal) o;
                temp = new Object[]{o, num.negate(), o};
            } else if (count == 12 && o instanceof BigDecimal) {  //Interest
                BigDecimal num = (BigDecimal) o;
                temp = new Object[]{o, o, num.negate()};
            } else {
                count++;
                continue;
            }
            vectors[vectorCount] = new Object[]{temp[0], temp[1], temp[2]};
            vectorCount++;
            count++;
        }
    }



}
