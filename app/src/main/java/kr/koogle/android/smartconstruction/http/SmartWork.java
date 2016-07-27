package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartWork {

    public String strCode;
    public int intLevel;
    public String strBuildCode;
    public String strDate;
    public int intWeather;
    public String strMemo;
    public String strImageURL;

    public ArrayList<SmartLabor> arrSmartLabors;
    public ArrayList<SmartLabor> arrSmartLaborNexts;
    public ArrayList<SmartMaterial> arrSmartMaterials;
    public ArrayList<SmartEquipment> arrSmartEquipments;
    public ArrayList<SmartPhoto> arrSmartPhotos;

    public SmartWork() {
        arrSmartLabors = new ArrayList<SmartLabor>();
        arrSmartLaborNexts = new ArrayList<SmartLabor>();
        arrSmartMaterials = new ArrayList<SmartMaterial>();
        arrSmartEquipments = new ArrayList<SmartEquipment>();
        arrSmartPhotos = new ArrayList<SmartPhoto>();
    }

}
