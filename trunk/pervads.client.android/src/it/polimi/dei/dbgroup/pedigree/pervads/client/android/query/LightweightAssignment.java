package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class LightweightAssignment implements Parcelable, Serializable {
	public static final long serialVersionUID = 0L;
	private String valueURI;
	private LightweightAssignment() {
		
	}
	
	public LightweightAssignment(String valueURI) {
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
	
	public static final Creator<LightweightAssignment> CREATOR = new Creator<LightweightAssignment>() {
		
		@Override
		public LightweightAssignment[] newArray(int size) {
			return new LightweightAssignment[size];
		}
		
		@Override
		public LightweightAssignment createFromParcel(Parcel source) {
			return new LightweightAssignment(source);
		}
	};
	
	private LightweightAssignment(Parcel in) {
		this(in.readString());
	}
}
