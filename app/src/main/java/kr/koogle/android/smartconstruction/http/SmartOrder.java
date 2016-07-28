package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

public class SmartOrder {

    public int intId;
    public String strSiteId;
    public String strUserId;
    public String strUserName;
    public String strBuildCode;
    public String strTitle;
    public String strContent;
    public String strTel;
    public int intStatus;
    public int intLevel;
    public String workDay;
    public int intSMS;
    public int intEmail;
    public String datWrite;

    public ArrayList<SmartEmployee> arrEmployees;
    public ArrayList<SmartFile> arrFiles;
    public ArrayList<SmartComment> arrComments;

    public SmartOrder() {
        arrEmployees = new ArrayList<SmartEmployee>();
        arrFiles = new ArrayList<SmartFile>();
        arrComments = new ArrayList<SmartComment>();
    }

}
