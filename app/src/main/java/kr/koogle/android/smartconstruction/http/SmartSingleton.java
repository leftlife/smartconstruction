package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartSingleton {
    private volatile static SmartSingleton uniqueInstance = null; // DCL
    private SmartSingleton() {}

    public static ArrayList<SmartBuild> arrSmartBuilds;
    public static ArrayList<SmartWork> arrSmartWorks;
    public static ArrayList<SmartBBSClient> arrSmartBBSClients;
    public static ArrayList<SmartBBSOrder> arrSmartBBSOrders;
    public static ArrayList<SmartPhoto> arrSmartPhotos;
    public static ArrayList<SmartEmployee> arrSmartEmployees;

    public static SmartSingleton getInstance() {
        if (uniqueInstance == null) {
            // 이렇게 하면 처음에만 동기화 됨!!
            synchronized (SmartSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new SmartSingleton();

                    arrSmartBuilds = new ArrayList<SmartBuild>();
                    arrSmartWorks = new ArrayList<SmartWork>();
                    arrSmartBBSClients = new ArrayList<SmartBBSClient>();
                    arrSmartBBSOrders = new ArrayList<SmartBBSOrder>();
                    arrSmartPhotos = new ArrayList<SmartPhoto>();
                    arrSmartEmployees = new ArrayList<SmartEmployee>();
                }
            }
        }
        return uniqueInstance;
    }

}

