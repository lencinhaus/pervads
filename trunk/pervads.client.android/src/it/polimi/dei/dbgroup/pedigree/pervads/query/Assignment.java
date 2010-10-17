package it.polimi.dei.dbgroup.pedigree.pervads.query;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class Assignment implements Parcelable, Serializable {
	public static final long serialVersionUID = 0L;
	private String valueURI;
	public Assignment() {
		
	}
	
	public Assignment(String valueURI) {
		this.valueURI = valueURI;
	}

	public String getValueURI() {
		return valueURI;
	}

	public void setValueURI(String valueURI) {
		this.valueURI = valueURI;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(valueURI);
	}
	
	public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
		
		@Override
		public Assignment[] newArray(int size) {
			return new Assignment[size];
		}
		
		@Override
		public Assignment createFromParcel(Parcel source) {
			return new Assignment(source);
		}
	};
	
	private Assignment(Parcel in) {
		this(in.readString());
	}
}
