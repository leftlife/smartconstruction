package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartClient {

    public int intId;
    public String strSiteId;
    public int intSort;
    public String strCate1;
    public String strTitle;
    public String strContent;
    public String strWriter;
    public String strUserId;
    public int intState;
    public int intLevel;
    public int intCount;
    public String datWrite;

    public ArrayList<SmartFile> arrFiles;
    public ArrayList<SmartComment> arrComments;

    public SmartClient() {
        arrFiles = new ArrayList<SmartFile>();
        arrComments = new ArrayList<SmartComment>();
    }

}
