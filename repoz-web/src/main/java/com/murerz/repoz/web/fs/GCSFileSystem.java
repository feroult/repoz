package com.murerz.repoz.web.fs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class GCSFileSystem implements FileSystem {

	private static final String SERVICE_ACCOUNT_EMAIL = "797755358727-0vo50r71dcf96p0kmgl03o2uh3tbdeut@developer.gserviceaccount.com";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static final Logger LOG = LoggerFactory.getLogger(GCSFileSystem.class);

	public RepozFile read(String path) {
		try {
			HttpRequestFactory factory = GCSHandler.me().getFactory();
			GenericUrl url = GCSHandler.me().createURL(path);
			HttpRequest req = factory.buildGetRequest(url);
			req.setThrowExceptionOnExecuteError(false);
			HttpResponse resp = executeCheck(req, 200, 404);
			if (resp.getStatusCode() == 404) {
				return null;
			}
			StreamRepozFile ret = new StreamRepozFile().setIn(resp.getContent());
			ret.setPath(path);
			ret.setMediaType("text/plain");
			ret.setCharset("UTF-8");
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(RepozFile file) {
		try {
			HttpRequestFactory factory = GCSHandler.me().getFactory();
			GenericUrl url = GCSHandler.me().createURL(file.getPath());
			InputStreamContent content = new InputStreamContent(file.getContentType(), file.getIn());
			HttpRequest req = factory.buildPutRequest(url, content);
			executeCheck(req, 200);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpResponse executeCheck(HttpRequest req, int... codes) {
		try {
			HttpResponse resp = req.execute();
			int code = resp.getStatusCode();
			if (!checkCode(code, codes)) {
				throw new RuntimeException("gcs request error, expected: " + Arrays.toString(codes) + ", but was: " + resp.getStatusCode() + " " + resp.getStatusMessage() + ". "
						+ resp.parseAsString());
			}
			return resp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean checkCode(int code, int... codes) {
		for (int i : codes) {
			if (i == code) {
				return true;
			}
		}
		return false;
	}

	public void delete(String path) {
		try {
			HttpRequestFactory factory = GCSHandler.me().getFactory();
			GenericUrl url = GCSHandler.me().createURL(path);
			HttpRequest req = factory.buildDeleteRequest(url);
			executeCheck(req, 204, 404);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteAll() {
	}

	public Set<String> listRepositories() {
		return null;
	}

	public Set<String> list(String path) {
		return null;
	}

	public Set<String> listRecursively(String path) {
		return null;
	}

}
