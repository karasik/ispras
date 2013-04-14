package application;

import models.LDA;

/**
 * Author: mi
 * Date: 3/18/13
 * Time: 9:48 PM
 */
public class Main {
    public static void main(String[] args) throws Exception {
        LDA m = new LDA("data/NewYorkPro.data", 10);

        /*
        m.performInference();

        int[] themes = m.getThemeAssignment();
        String[] documents = m.getDocuments();
        String[] themeTop = m.themeTop(5);
        */
    }
}
