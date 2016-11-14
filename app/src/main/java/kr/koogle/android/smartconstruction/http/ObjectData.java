package kr.koogle.android.smartconstruction.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ObjectData implements Parcelable
{

    String shout;
    int year;

    public ObjectData(String shout, int year)
    {
        this.shout = shout;
        this.year = year;
    }

    public ObjectData(Parcel src)
    {
        this.shout = src.readString();
        this.year = src.readInt();
    }

    // CREATOR 객체 생성 - Parcel 객체에서 데이터를 읽어 객체를 생성.
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public ObjectData createFromParcel(Parcel in)
        {
            return new ObjectData(in);
        }

        public ObjectData[] newArray(int size)
        {
            return new ObjectData[size];
        }
    };

    // Parcelable Interface Method
    public int describeContents()
    {
        return 0;
    }

    // Parcelable Interface Method - Parcel 에 데이터 쓰기
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(shout);
        dest.writeInt(year);
        //dest.writeStringArray(arrStr);
    }

}
