package com.wangym.lombok.job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WordDict {

    private List<WordEntity> list = new ArrayList<>();

    public void add(String name) {
        int index = list.indexOf(new WordEntity(name));
        if (index >= 0) {
            list.get(index).incr();
        } else {
            list.add(new WordEntity(name));
        }
    }

    public String print() {
        List<WordEntity> sortList = list.stream()
                .sorted(Comparator.comparing(WordEntity::getName))
                .collect(Collectors.toList());
        StringBuffer sb = new StringBuffer();
        for (WordEntity w : sortList) {
            sb.append(w.toString()).append("\n");
        }
        return sb.toString();
    }
}
