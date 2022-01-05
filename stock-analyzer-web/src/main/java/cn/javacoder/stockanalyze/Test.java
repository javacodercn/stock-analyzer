package cn.javacoder.stockanalyze;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Test {
    public static void main(String[] args) {
        CopyOnWriteArrayList set = new CopyOnWriteArrayList<>();
        //System.out.println(System.identityHashCode(set.toArray()));
        set.add(0);
        System.out.println(System.identityHashCode(set.toArray()));
        set.add(123);
        System.out.println(System.identityHashCode(set.toArray()));
        set.add("xyz");
        System.out.println(System.identityHashCode(set.toArray()));
        set.add("xyz33");
        System.out.println(System.identityHashCode(set.toArray()));
        System.out.println(set);
    }
}
