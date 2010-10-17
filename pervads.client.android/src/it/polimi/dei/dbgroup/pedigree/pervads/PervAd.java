package it.polimi.dei.dbgroup.pedigree.pervads;

import android.os.Parcel;
import android.os.Parcelable;

public class PervAd implements Parcelable {
	private String id;
	private String networkName;
	private String content;
	private boolean seen;
	private long findTime;

	private PervAd(Parcel parcel) {
		this.id = parcel.readString();
		this.networkName = parcel.readString();
		this.content = parcel.readString();
		this.seen = parcel.readByte() != 0;
		this.findTime = parcel.readLong();
	}

	public PervAd(String id, String networkName, String content, long findTime) {
		this.id = id;
		this.networkName = networkName;
		this.content = content;
		this.seen = false;
		this.findTime = findTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isSeen() {
		return seen;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public long getFindTime() {
		return findTime;
	}

	public void setFindTime(long findTime) {
		this.findTime = findTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(id);
		parcel.writeString(networkName);
		parcel.writeString(content);
		parcel.writeByte(seen ? (byte) 1 : (byte) 0);
		parcel.writeLong(findTime);
	}

	public static final Creator<PervAd> CREATOR = new Creator<PervAd>() {

		@Override
		public PervAd[] newArray(int size) {
			return new PervAd[size];
		}

		@Override
		public PervAd createFromParcel(Parcel source) {
			return new PervAd(source);
		}
	};
}
