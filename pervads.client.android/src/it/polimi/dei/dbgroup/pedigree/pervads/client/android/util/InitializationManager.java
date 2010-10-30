package it.polimi.dei.dbgroup.pedigree.pervads.client.android.util;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.widget.InitializationProgressDialog;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class InitializationManager {
	private static final Logger L = new Logger(InitializationManager.class
			.getSimpleName());
	
	private static final ProgressMonitor MockProgressMonitor = new ProgressMonitor() {
		
		@Override
		public void progress(int progress) {
			// do nothing
		}
		
		@Override
		public void message(int messageId) {
			// do nothing
		}
		
		@Override
		public void message(String message) {
			// do nothing
		}
		
		@Override
		public void max(int max) {
			// do nothing
		}
		
		@Override
		public void indeterminate(boolean indeterminate) {
			// do nothing
		}
		
		@Override
		public void increment(int by) {
			// do nothing
		}
	};

	private static interface ProgressInfo {
		public void apply(ProgressDialog dialog);
	}

	private static class InitializerParams {
		public Context context;
		public OnFinishListener listener;
		public Initializable[] initializables;

		public InitializerParams(Context context, OnFinishListener listener,
				Initializable... initializables) {
			super();
			this.context = context;
			this.listener = listener;
			this.initializables = initializables;
		}
	}

	private static class Initializer extends
			AsyncTask<InitializerParams, ProgressInfo, InitializerParams> {
		private class Max implements ProgressInfo {
			private int max;

			public Max(int max) {
				this.max = max;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				dialog.setMax(max);
			}
		}

		private class Progress implements ProgressInfo {
			private int progress;

			public Progress(int progress) {
				this.progress = progress;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				dialog.setProgress(progress);
			}
		}

		private class Message implements ProgressInfo {
			private String message;

			public Message(String message) {
				this.message = message;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				dialog.setMessage(message);
			}
		}

		private static class MessageId implements ProgressInfo {
			private int messageId;

			public MessageId(int messageId) {
				this.messageId = messageId;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				if (dialog.getContext() != null)
					dialog.setMessage(dialog.getContext().getText(messageId));
			}
		}

		private static class Indeterminate implements ProgressInfo {
			private boolean indeterminate;

			public Indeterminate(boolean indeterminate) {
				this.indeterminate = indeterminate;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				if(dialog.isIndeterminate() && !indeterminate) {
					dialog.setIndeterminate(false);
					dialog.setMax(1);
					dialog.setProgress(0);
				}
				else if(!dialog.isIndeterminate() && indeterminate) {
					dialog.setIndeterminate(true);
					dialog.setMax(1);
					dialog.setProgress(1);
				}
			}
		}

		private static class Increment implements ProgressInfo {
			private int by;

			public Increment(int by) {
				this.by = by;
			}

			@Override
			public void apply(ProgressDialog dialog) {
				dialog.incrementProgressBy(by);
			}
		}

		private final ProgressMonitor monitor = new ProgressMonitor() {

			@Override
			public void progress(int progress) {
				publishProgress(new Progress(progress));
			}

			@Override
			public void message(int messageId) {
				publishProgress(new MessageId(messageId));
			}

			@Override
			public void message(String message) {
				publishProgress(new Message(message));
			}

			@Override
			public void max(int max) {
				publishProgress(new Max(max));
			}

			@Override
			public void indeterminate(boolean indeterminate) {
				publishProgress(new Indeterminate(indeterminate));
			}

			@Override
			public void increment(int by) {
				publishProgress(new Increment(by));
			}

		};

		private final Logger L = new Logger(Initializer.class.getSimpleName());

		private ProgressDialog dialog;

		public Initializer(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		protected InitializerParams doInBackground(InitializerParams... params2) {
			InitializerParams params = params2[0];
			for (Initializable initializable : params.initializables) {
				if (!initializable.isInitialized(params.context)) {
					L.d("initializing "
							+ initializable.getClass().getSimpleName());
					initializable.initialize(params.context, monitor);
					L.d("initialized "
							+ initializable.getClass().getSimpleName());
				}
			}
			return params;
		}

		@Override
		protected void onProgressUpdate(ProgressInfo... values) {
			super.onProgressUpdate(values);
			if (dialog != null && dialog.isShowing()) {
				for (ProgressInfo info : values) {
					info.apply(dialog);
				}
			}
		}

		@Override
		protected void onPostExecute(InitializerParams result) {
			super.onPostExecute(result);
			if(dialog != null) dialog.dismiss();
			initializer = null;
			if (result.listener != null)
				result.listener.onFinish();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(dialog != null) dialog.show();
		}

		
	}

	private static Initializer initializer = null;

	public interface OnFinishListener {
		public void onFinish();
	}

	public static boolean areInitialized(Context context,
			Initializable... initializables) {
		for (Initializable initializable : initializables) {
			if (!initializable.isInitialized(context))
				return false;
		}
		return true;
	}
	
	public static void initializeAsync(Context context, Initializable... initializables) {
		initializeAsync(context, null, initializables);
	}
	
	public static void initializeAsync(Context context, boolean showDialog, Initializable... initializables) {
		initializeAsync(context, null, showDialog, initializables);
	}
	
	public static void initializeAsync(Context context, OnFinishListener listener,
			Initializable... initializables) {
		boolean showDialog = false;
		if(context instanceof Activity) showDialog = true;
		initializeAsync(context, listener, showDialog, initializables);
	}

	public static void initializeAsync(Context context, OnFinishListener listener, boolean showDialog,
			Initializable... initializables) {
		if (initializer != null) {
			throw new RuntimeException(
					"initialize called while initializer was still running");
		}
		
		if (initializables.length == 0
				|| areInitialized(context, initializables)) {
			L.d("not initializing " + formatInitializables(initializables)
					+ " because already initialized");
			if (listener != null)
				listener.onFinish();
			return;
		}

		L.d("creating initializer task");
		ProgressDialog dialog = null;
		if(showDialog) dialog = new InitializationProgressDialog(context);
		InitializerParams params = new InitializerParams(context, listener,
				initializables);
		initializer = new Initializer(dialog);

		L.d("executing initializer task for "
				+ formatInitializables(initializables));
		initializer.execute(params);
	}
	
	public static void initializeSync(Context context, Initializable... initializables) {
		for(Initializable initializable : initializables) {
			if(!initializable.isInitialized(context)) initializable.initialize(context, MockProgressMonitor);
		}
	}

	private static String formatInitializables(Initializable... initializables) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (Initializable initializable : initializables) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(initializable.getClass().getSimpleName());
		}
		sb.append("]");
		return sb.toString();
	}
}
