package it.polimi.dei.dbgroup.pedigree.pervads.client.android.widget;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AssignmentView extends TableLayout implements OnClickListener {
	public interface OnEntityClickListener {
		public void onValueClick(Value value);

		public void onDimensionClick(Dimension dimension);
	}

	private LayoutInflater inflater;
	private OnEntityClickListener listener = null;

	public AssignmentView(Context context) {
		super(context);
		initializeInflater(context);
	}

	public AssignmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeInflater(context);
	}

	public void setEntity(NamedEntity entity) {
		// clear the child views
		this.removeAllViews();

		if (entity instanceof Dimension)
			updateFromDimension((Dimension) entity);
		else if (entity instanceof Value)
			updateFromValue((Value) entity);
		invalidate();
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag != null && tag instanceof NamedEntity)
			notifyEntityClicked((NamedEntity) tag);
	}

	public void setOnEntityClickListener(OnEntityClickListener listener) {
		this.listener = listener;
	}

	private void initializeInflater(Context context) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private void notifyEntityClicked(NamedEntity entity) {
		if (entity != null && listener != null) {
			if (entity instanceof Value)
				listener.onValueClick((Value) entity);
			else if (entity instanceof Dimension)
				listener.onDimensionClick((Dimension) entity);
		}
	}

	private void updateFromValue(Value value) {
		while (value != null) {
			addRow(value.getParentDimension(), value);
			value = value.getParentDimension().getParentValue();
		}

	}

	private void updateFromDimension(Dimension dimension) {
		addRow(dimension, null);
		updateFromValue(dimension.getParentValue());
	}

	private void addRow(Dimension dimension, Value value) {
		final TableRow row = (TableRow) inflater.inflate(
				R.layout.assignment_view_row, this, false);
		final TextView dimensionTextView = (TextView) row
				.findViewById(R.id.dimension);
		dimensionTextView.setText(dimension.getName());
		dimensionTextView.setTag(dimension);
		dimensionTextView.setClickable(true);
		dimensionTextView.setOnClickListener(this);
		final TextView valueTextView = (TextView) row.findViewById(R.id.value);
		if (value == null) {
			valueTextView.setText(R.string.assignment_view_empty_value_text);
			valueTextView.setClickable(false);
		} else {
			valueTextView.setText(value.getName());
			valueTextView.setTag(value);
			valueTextView.setClickable(true);
			valueTextView.setOnClickListener(this);
		}
		addView(row, 0);
	}
}
