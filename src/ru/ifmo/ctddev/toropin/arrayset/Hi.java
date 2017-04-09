package ru.ifmo.ctddev.toropin.arrayset;


import java.util.*;

import static sun.swing.MenuItemLayoutHelper.max;

/**
 * Created by impy on 05.04.17.
 */
public class Hi {

    public static void main(String[] argc) {
        Scanner scanner = new Scanner(System.in);
        int n, x;
        n = scanner.nextInt();
        x = scanner.nextInt();
        String s = scanner.nextLine();
        Boolean f = false;
        for (int i = 0; i <= n - x && !f; i++) {
            if ((i == 0 || s.charAt(i- 1) != 'N') &&
                (i + x  == n || s.charAt(i + x) != 'N')){
                Boolean tempF = true;
//                System.out.println(i);
                for (int j = i; j < i + x && tempF; j++) {
                    if (s.charAt(j) == 'Y') {
                        tempF = false;
                    }
                }
                if (tempF) {
                    f = true;
                }
            }
        }
        int ans  = 0;
        int maxAns = 0;
        for (int i = 0; i < n; i++) {
            if (s.charAt(i) == 'N') {
                ans++;
                maxAns = max(ans, maxAns);
            } else {
                ans = 0;
            }
        }
        if (f && (x >= maxAns)) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }

    }
}