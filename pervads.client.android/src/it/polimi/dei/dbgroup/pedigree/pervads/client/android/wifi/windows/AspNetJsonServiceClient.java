package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.windows;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiAdapterException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class AspNetJsonServiceClient {
	private URI serviceBaseUri;
	private static final Logger L = new Logger(AspNetJsonServiceClient.class
			.getSimpleName());
	private static final String DEFAULT_CHARSET = "UTF8";
	private static final String JSON_ERROR_HEADER = "jsonerror";
	private static final String JSON_RESPONSE_DATA_KEY = "d";
	private static final String JSON_EXCEPTION_DETAIL_KEY = "ExceptionDetail";
	private static final String EXCEPTION_MESSAGE_KEY = "Message";
	private static final String INNER_EXCEPTION_KEY = "InnerException";
	private static final String STACK_TRACE_KEY = "StackTrace";
	private static final Pattern STACK_TRACE_DUMP_PATTERN = Pattern
			.compile("^[ \\t]*in[ \\t]*(([a-zA-Z0-9\\-_\\.]+)[\\.])?([a-zA-Z0-9\\-_]+\\([^\\)]*\\))([ \\t]*in[ \\t]*(.+):riga[ \\t]*([0-9]+))?$");
	private static final int TRACE_CLASS_MATCH_GROUP = 2;
	private static final int TRACE_METHOD_MATCH_GROUP = 3;
	private static final int TRACE_FILE_MATCH_GROUP = 5;
	private static final int TRACE_LINE_MATCH_GROUP = 6;
	private static final int REQUEST_TIMEOUT = 30000; // milliseconds
	private static final String PARSE_EXCEPTION_FORMAT = "cannot parse %s data";
	private static HttpClient client;
	static {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, REQUEST_TIMEOUT);
		client = new DefaultHttpClient(params);
	}

	public AspNetJsonServiceClient(String serviceBaseUriStr)
			throws URISyntaxException {
		this(new URI(checkServiceUriString(serviceBaseUriStr)));
	}

	public AspNetJsonServiceClient(URI serviceBaseUri) {
		if (serviceBaseUri == null)
			throw new NullPointerException("serviceBaseUri cannot be null");
		this.serviceBaseUri = serviceBaseUri;
	}

	public JSONObject makeObjectRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			if (json.isNull(JSON_RESPONSE_DATA_KEY))
				return null;
			return json.getJSONObject(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {

			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "JSONObject"), ex);
		}
	}

	public boolean makeBooleanRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			return json.getBoolean(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "boolean"), ex);
		}
	}

	public JSONArray makeArrayRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			if (json.isNull(JSON_RESPONSE_DATA_KEY))
				return null;
			return json.getJSONArray(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "JSONArray"), ex);
		}
	}

	public String makeStringRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			if (json.isNull(JSON_RESPONSE_DATA_KEY))
				return null;
			return json.getString(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "String"), ex);
		}
	}

	public int makeIntRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			return json.getInt(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "int"), ex);
		}
	}

	public double makeDoubleRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			return json.getDouble(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "double"), ex);
		}
	}

	public long makeLongRequest(String method, JSONObject params)
			throws WifiAdapterException {
		final JSONObject json = makeRequest(method, params);

		try {
			return json.getLong(JSON_RESPONSE_DATA_KEY);
		} catch (JSONException ex) {
			throw new WifiAdapterException(String.format(
					PARSE_EXCEPTION_FORMAT, "long"), ex);
		}
	}

	private static String checkServiceUriString(String serviceUriString) {
		if (serviceUriString != null) {
			if (!serviceUriString.endsWith("/"))
				serviceUriString += "/";
		}
		return serviceUriString;
	}

	private JSONObject makeRequest(String method, JSONObject params)
			throws WifiAdapterException {
		URI uri = serviceBaseUri.resolve(method);
		HttpUriRequest request = null;
		if (Logger.D)
			L.d("starting request for method " + method + " on URI " + uri);
		if (Logger.V)
			L.v("creating service request");
		if (params != null) {
			request = new HttpPost(uri);
			HttpEntity entity = null;
			try {
				entity = new StringEntity(params.toString(), DEFAULT_CHARSET);
			} catch (UnsupportedEncodingException ueex) {
				throw new WifiAdapterException(
						"service request cannot be created because default encoding ("
								+ DEFAULT_CHARSET + ") is not supported", ueex);
			}
			((HttpPost) request).setEntity(entity);

			// set json content type
			request.setHeader("Content-Type", "application/json");
		} else
			request = new HttpGet(uri);
		L.v("service request created");

		L.v("executing service request");
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (Exception ex) {
			throw new WifiAdapterException(
					"an error occurred during request execution, request uri: "
							+ uri, ex);
		}
		L.v("service request executed");

		// read content into a string
		L.v("reading response content");
		String s = null;
		try {
			InputStreamReader streamReader = new InputStreamReader(response
					.getEntity().getContent());

			// check if we have response length
			int bufferSize = 8192;
			int contentLength = -1;
			Header contentLengthHeader = response
					.getFirstHeader("Content-Length");
			if (contentLengthHeader != null) {
				contentLength = Integer
						.parseInt(contentLengthHeader.getValue());
				bufferSize = Math.min(bufferSize, contentLength);
			}
			BufferedReader reader = new BufferedReader(streamReader, bufferSize);

			StringBuilder sb = null;
			if (contentLength != -1)
				sb = new StringBuilder(contentLength);
			else
				sb = new StringBuilder();

			while ((s = reader.readLine()) != null) {
				sb.append(s + "\n");
			}
			reader.close();
			s = sb.toString();
		} catch (Exception ex) {
			throw new WifiAdapterException(
					"an error occurred during response reading, request uri: "
							+ uri, ex);
		}
		L.v("response content read");

		// parse string into a JSON object
		L.v("parsing response JSON");
		JSONObject json = null;
		try {
			json = new JSONObject(s);
		} catch (JSONException ex) {
			throw new WifiAdapterException(
					"cannot parse response JSON, request uri: " + uri, ex);
		}
		L.v("response JSON parsed");

		// check if error response
		if (response.containsHeader(JSON_ERROR_HEADER)
				&& Boolean.parseBoolean(response.getFirstHeader(
						JSON_ERROR_HEADER).getValue())) {
			L.v("parsing response error details");
			// parse json error and throw exception
			WifiAdapterException ex = null;
			ServerSideException ssex = null;
			if (json.has(JSON_EXCEPTION_DETAIL_KEY)
					&& !json.isNull(JSON_EXCEPTION_DETAIL_KEY))
				try {
					JSONObject details = json
							.getJSONObject(JSON_EXCEPTION_DETAIL_KEY);
					ssex = buildServerSideException(details);
				} catch (JSONException jsex) {
					// cannot parse exception details, leave ssex null
				}

			String message = "service call returned an exception, request uri: "
					+ uri;
			if (ssex != null)
				ex = new WifiAdapterException(message, ssex);
			else
				ex = new WifiAdapterException(message);
			L.v("response error details parsed");
			throw ex;
		}
		L.d("request for method " + method + " completed");
		return json;
	}

	private static ServerSideException buildServerSideException(JSONObject exObj) {
		String message = null;
		if (exObj.has(EXCEPTION_MESSAGE_KEY)
				&& !exObj.isNull(EXCEPTION_MESSAGE_KEY))
			try {
				message = exObj.getString(EXCEPTION_MESSAGE_KEY);
			} catch (JSONException jsex) {
				// do nothing
			}
		ServerSideException inner = null;
		if (exObj.has(INNER_EXCEPTION_KEY)
				&& !exObj.isNull(INNER_EXCEPTION_KEY))
			try {
				inner = buildServerSideException(exObj
						.getJSONObject(INNER_EXCEPTION_KEY));
			} catch (JSONException jsex) {
				// do nothing
			}
		ServerSideException ex = null;
		if (message != null) {
			if (inner != null)
				ex = new ServerSideException(message, inner);
			else
				ex = new ServerSideException(message);
		} else if (inner != null)
			ex = new ServerSideException(inner);
		else
			ex = new ServerSideException();

		if (exObj.has(STACK_TRACE_KEY) && !exObj.isNull(STACK_TRACE_KEY)) {
			try {
				ex.setStackTrace(buildServerSideExceptionStackTrace(exObj
						.getString(STACK_TRACE_KEY)));
			} catch (JSONException jsex) {
				// do nothing
			}
		} else
			ex.setStackTrace(new StackTraceElement[0]);

		return ex;
	}

	private static StackTraceElement[] buildServerSideExceptionStackTrace(
			String stackTraceDump) {
		List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
		for (String trace : stackTraceDump.split("\r\n")) {
			Matcher m = STACK_TRACE_DUMP_PATTERN.matcher(trace);
			if (m.matches()) {
				String method = m.group(TRACE_METHOD_MATCH_GROUP);
				if (!TextUtils.isEmpty(method)) {
					String cls = m.group(TRACE_CLASS_MATCH_GROUP);
					if (TextUtils.isEmpty(cls))
						cls = "UNKNOWN_CLASS";
					String file = m.group(TRACE_FILE_MATCH_GROUP);
					if (TextUtils.isEmpty(file))
						file = null;
					int line = -1;
					if (file != null) {
						String lineStr = m.group(TRACE_LINE_MATCH_GROUP);
						if (!TextUtils.isEmpty(lineStr)) {
							try {
								line = Integer.parseInt(lineStr);
							} catch (Exception ex) {
								// do nothing
							}
						}
					}

					StackTraceElement element = new StackTraceElement(cls,
							method, file, line);
					elements.add(element);
				}
			}
		}

		return elements.toArray(new StackTraceElement[elements.size()]);
	}
}
