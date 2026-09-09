package com.example;

import java.util.List;

public class Sorter {
    public static List<Block> sorter(List<Block> list) {
        while (true) {
            int j = 0;
            for (int i = 0;i<list.size() - 1;i++) {
                Block next = list.get(i + 1);
                Block b = list.get(i);

                if (b.layer > next.layer) {
                    list.set(i + 1, b);
                    list.set(i, next);
                    j = 0;
                }else{
                    j++;
                }
            }
            if (j == list.size() - 1) {
                break;
            }
        }

        return list;
    }

}
