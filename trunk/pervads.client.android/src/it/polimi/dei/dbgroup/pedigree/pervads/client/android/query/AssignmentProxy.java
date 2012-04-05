package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class AssignmentProxy implements Parcelable, Serializable {
	public static final long serialVersionUID = 0L;
	private String valueURI;
	private Map<String, String> parameterValues = new HashMap<String, String>();
	private List<AssignmentProxy> childAssignments = new ArrayList<AssignmentProxy>();

	@SuppressWarnings("unused")
	private AssignmentProxy() {

	}

	public AssignmentProxy(String valueURI) {
		this.valueURI = valueURI;
	}

	public String getValueURI() {
		return valueURI;
	}

	public void setValueURI(String valueURI) {
		this.valueURI = valueURI;
	}

	public Map<String, String> getParameterValues() {
		return parameterValues;
	}
	
	public void setParameterValue(String parameterURI, String value) {
		parameterValues.put(parameterURI, value);
	}
	
	public String getParameterValue(String parameterURI) {
		return parameterValues.get(parameterURI);
	}

	public List<AssignmentProxy> getChildAssignments() {
		return childAssignments;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(valueURI);

		dest.writeInt(parameterValues.size());
		for(String parameterURI : parameterValues.keySet()) {
			dest.writeString(parameterURI);
			dest.writeString(parameterValues.get(parameterURI));
		}
		
		dest.writeTypedList(childAssignments);
	}

	public static final Creator<AssignmentProxy> CREATOR = new Creator<AssignmentProxy>() {

		@Override
		public AssignmentProxy[] newArray(int size) {
			return new AssignmentProxy[size];
		}

		@Override
		public AssignmentProxy createFromParcel(Parcel source) {
			return new AssignmentProxy(source);
		}
	};

	private AssignmentProxy(Parcel in) {
		this(in.readString());
		int parametersSize = in.readInt();
		for(int i=0; i < parametersSize; i++) {
			parameterValues.put(in.readString(), in.readString());
		}
		in.readTypedList(childAssignments, AssignmentProxy.CREATOR);
	}
}
