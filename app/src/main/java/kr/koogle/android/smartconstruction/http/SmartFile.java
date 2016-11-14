package kr.koogle.android.smartconstruction.http;

import android.os.Parcel;
import android.os.Parcelable;

public class SmartFile implements Parcelable{

    public int intId;

    public String strBBS;
    public String strBBSId;
    public int intState;
    public int intLevel;
    public int intOrder;

    public String strType;
    public String strSiteId;
    public String strWriter;
    public String strNameOrigin;
    public String strName;

    public String strURL;
    public String strThumbnail;
    public String strBuildCode;
    public String strBuildDate;
    public String strLavorCode;

    public String strLavor2Code;
    public String strBuildName;
    public String strLocation;
    public String strMemo;
    public int intWidth;

    public int intHeight;
    public int intSize;
    public String strLat;
    public String strLng;
    public String datRegist;

    public SmartFile()
    {
        super();
    }

    public SmartFile(Parcel src)
    {
        super();
        this.intId = src.readInt();

        this.strBBS = src.readString();
        this.strBBSId = src.readString();
        this.intState = src.readInt();
        this.intLevel = src.readInt();
        this.intOrder = src.readInt();

        this.strType = src.readString();
        this.strSiteId = src.readString();
        this.strWriter = src.readString();
        this.strNameOrigin = src.readString();
        this.strName = src.readString();

        this.strURL = src.readString();
        this.strThumbnail = src.readString();
        this.strBuildCode = src.readString();
        this.strBuildDate = src.readString();
        this.strLavorCode = src.readString();

        this.strLavor2Code = src.readString();
        this.strBuildName = src.readString();
        this.strLocation = src.readString();
        this.strMemo = src.readString();
        this.intWidth = src.readInt();

        this.intHeight = src.readInt();
        this.intSize = src.readInt();
        this.strLat = src.readString();
        this.strLng = src.readString();
        this.datRegist = src.readString();
    }

    //parcel 오브젝트 종류
    @Override
    public int describeContents() {
        return 0;
    }

    // 실제 오브젝트
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(intId);

        dest.writeString(strBBS);
        dest.writeString(strBBSId);
        dest.writeInt(intState);
        dest.writeInt(intLevel);
        dest.writeInt(intOrder);

        dest.writeString(strType);
        dest.writeString(strSiteId);
        dest.writeString(strWriter);
        dest.writeString(strNameOrigin);
        dest.writeString(strName);

        dest.writeString(strURL);
        dest.writeString(strThumbnail);
        dest.writeString(strBuildCode);
        dest.writeString(strBuildDate);
        dest.writeString(strLavorCode);

        dest.writeString(strLavor2Code);
        dest.writeString(strBuildName);
        dest.writeString(strLocation);
        dest.writeString(strMemo);
        dest.writeInt(intWidth);

        dest.writeInt(intHeight);
        dest.writeInt(intSize);
        dest.writeString(strLat);
        dest.writeString(strLng);
        dest.writeString(datRegist);
    }

    // 복구하는 생성자 writeToParcel 에서 기록한 순서를 똑같이 해줘야함
    public void readFromParcel(Parcel src) {
        this.intId = src.readInt();

        this.strBBS = src.readString();
        this.strBBSId = src.readString();
        this.intState = src.readInt();
        this.intLevel = src.readInt();
        this.intOrder = src.readInt();

        this.strType = src.readString();
        this.strSiteId = src.readString();
        this.strWriter = src.readString();
        this.strNameOrigin = src.readString();
        this.strName = src.readString();

        this.strURL = src.readString();
        this.strThumbnail = src.readString();
        this.strBuildCode = src.readString();
        this.strBuildDate = src.readString();
        this.strLavorCode = src.readString();

        this.strLavor2Code = src.readString();
        this.strBuildName = src.readString();
        this.strLocation = src.readString();
        this.strMemo = src.readString();
        this.intWidth = src.readInt();

        this.intHeight = src.readInt();
        this.intSize = src.readInt();
        this.strLat = src.readString();
        this.strLng = src.readString();
        this.datRegist = src.readString();
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public SmartFile createFromParcel(Parcel in) {
            return new SmartFile(in);
        }

        @Override
        public SmartFile[] newArray(int size) {
            // TODO Auto-generated method stub
            return new SmartFile[size];
        }

    };

}
