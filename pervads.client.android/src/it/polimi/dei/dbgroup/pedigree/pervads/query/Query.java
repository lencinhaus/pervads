package it.polimi.dei.dbgroup.pedigree.pervads.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Query implements Parcelable, Serializable {
	public static final long serialVersionUID = 0L;
	private List<Assignment> assignments = new ArrayList<Assignment>();
	private String name;
	private boolean enabled = true;

	public Query() {

	}

	public List<Assignment> getAssignments() {
		return assignments;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeInt(enabled?1:0);
		dest.writeTypedList(assignments);
	}

	public static final Parcelable.Creator<Query> CREATOR = new Creator<Query>() {
		
		@Override
		public Query[] newArray(int size) {
			return new Query[size];
		}
		
		@Override
		public Query createFromParcel(Parcel source) {
			return new Query(source);
		}
	};
	
	private Query(Parcel in) {
		this();
		name = in.readString();
		enabled = in.readInt() == 1;
		in.readTypedList(assignments, Assignment.CREATOR);
	}
}
