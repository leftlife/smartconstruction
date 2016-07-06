package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartSingleton {
    private volatile static SmartSingleton uniqueInstance = null; // DCL
    private SmartSingleton() {}

    public static ArrayList<SmartBuild> arrSmartBuilds;

    public static SmartSingleton getInstance() {
        if (uniqueInstance == null) {
            // 이렇게 하면 처음에만 동기화 됨!!
            synchronized (SmartSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new SmartSingleton();
                    arrSmartBuilds = new ArrayList<SmartBuild>();
                }
            }
        }
        return uniqueInstance;
    }

}

/*
// 처음부터 JVM에서 Singleton의 유일한 인스턴스를 생성한다.

public class Singleton {
    private static Singleton uniqueInstance = new Singleton();

    private Singleton() {
    }

    public static Singleton getInstance() {
        return uniqueInstance;
    }
}
 */
