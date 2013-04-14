package auxiliary;

import java.util.List;

/**
 * Author: mi
 * Date: 4/3/13
 * Time: 2:01 AM
 */
public class Aux {

    public static String[] stringListToArray(List<String> l) {
        String[] a = new String[l.size()];
        for (int i=0; i<a.length; i++) {
            a[i] = l.get(i);
        }
        return a;
    }

    public static Integer[] intListToArray(List<Integer> l) {
        Integer[] a = new Integer[l.size()];
        for (int i=0; i<a.length; i++) {
            a[i] = l.get(i);
        }
        return a;
    }
}
