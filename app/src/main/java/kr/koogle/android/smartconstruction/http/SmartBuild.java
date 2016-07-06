package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;
import java.util.HashMap;

public class SmartBuild {

    public String strCode;
    public int intLevel;
    public int intCate;
    public int intKind;
    public String strType;

    public String strName;
    public String strNameLong;
    public String strStartDate;
    public String strEndDate;
    public String strAddress;

    public String strBuildUseCode;
    public int intBuildGround;
    public int intBuildBasement;
    public String strBuildStructureCode;
    public String strBuildArea11;
    public String strBuildArea12;
    public String strBuildArea21;
    public String strBuildArea22;
    public String strBuildArea31;
    public String strBuildArea32;

    public String strCostString;
    public int intCostNumber;
    public int intCostNumber2;
    public int intCostNumber3;
    public String strCostVAT;
    public String strCostHidden;
    public int intCostTotal;

    public String strClientCode1;
    public String strClientCode2;
    public String strClientCode3;
    public String strEngineerCode1;
    public String strEngineerCode2;
    public String strEngineercode3;
    public String strArchitectCode1;
    public String strArchitectCode2;
    public String strArchitectCode3;

    public String strImageURL;
    public String strImageURLBIM;
    public String strLat;
    public String strLng;

    public HashMap<String, String> mapFiles; // String 파일명 : strFile1
    public HashMap<String, SmartWork> mapSmartWorks; // String 날짜 : 2016.06.12


    private static int lastContactId = 0;
    public static ArrayList<SmartBuild> createSmartBuildsList(int numContacts) {
        ArrayList<SmartBuild> contacts = new ArrayList<SmartBuild>();

        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new SmartBuild());
        }

        return contacts;
    }
}
