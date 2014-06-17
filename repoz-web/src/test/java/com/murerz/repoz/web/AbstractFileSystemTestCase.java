package com.murerz.repoz.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.googlecode.mycontainer.commons.http.Request;
import com.googlecode.mycontainer.commons.http.Response;

public abstract class AbstractFileSystemTestCase extends AbstractTestCase {

	@Test
	public void testRepo() {
		assertEquals(new Integer(404), a.code("GET", "/r/file.txt"));
		assertEquals(new Integer(404), a.code("GET", "/r/with/some/dir/other.txt"));

		assertEquals(new Integer(200), a.code("PUT", "/r/file.txt", "text/plain;charset=UTF-8", "my text file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/with/some/dir/other.txt", "text/plain;charset=UTF-8", "my other file"));

		assertResp(Request.create("GET", "/r/file.txt"), 200, "text/plain", "UTF-8", "my text file");
		assertResp(Request.create("GET", "/r/with/some/dir/other.txt"), 200, "text/plain", "UTF-8", "my other file");

		assertEquals(new Integer(200), a.code("DELETE", "/r/file.txt"));
		assertEquals(new Integer(200), a.code("DELETE", "/r/with/some/dir/other.txt"));

		assertEquals(new Integer(404), a.code("GET", "/r/file.txt"));
		assertEquals(new Integer(404), a.code("GET", "/r/with/some/dir/other.txt"));
	}

	@Test
	public void testDeleteRepository() {
		assertEquals(new Integer(200), a.code("PUT", "/r/a/file.txt", "text/plain;charset=UTF-8", "my text file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/a/with/some/dir/other.txt", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/b/other.txt", "text/plain;charset=UTF-8", "my other file"));

		assertResp(Request.create("GET", "/r/a/file.txt"), 200, "text/plain", "UTF-8", "my text file");
		assertResp(Request.create("GET", "/r/a/with/some/dir/other.txt"), 200, "text/plain", "UTF-8", "my other file");
		assertResp(Request.create("GET", "/r/b/other.txt"), 200, "text/plain", "UTF-8", "my other file");

		assertEquals(new Integer(200), a.code("DELETE", "/r/a"));
		assertEquals(new Integer(404), a.code("GET", "/r/a/file.txt"));
		assertEquals(new Integer(404), a.code("GET", "/r/a/with/some/dir/other.txt"));
		assertResp(Request.create("GET", "/r/b/other.txt"), 200, "text/plain", "UTF-8", "my other file");
	}

	@Test
	public void testListFiles() {
		assertEquals(new Integer(200), a.code("PUT", "/r/a/docs/file.txt", "text/plain;charset=UTF-8", "my text file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/a/docs/some/dir/other.txt", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/a/config/x.txt", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/a/other.txt", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/b/other.txt", "text/plain;charset=UTF-8", "my other file"));

		assertResp(Request.create("GET", "/r/a/docs?l=true"), 200, "text/plain", "UTF-8", "/a/docs/file.txt\n/a/docs/some\n");
		assertResp(Request.create("GET", "/r/a/config?l=true"), 200, "text/plain", "UTF-8", "/a/config/x.txt\n");
		assertResp(Request.create("GET", "/r/a/config/x.txt?l=true"), 200, "text/plain", "UTF-8", "");
		assertResp(Request.create("GET", "/r/a?l=true"), 200, "text/plain", "UTF-8", "/a/config\n/a/docs\n/a/other.txt\n");
		assertResp(Request.create("GET", "/r/?l=true"), 200, "text/plain", "UTF-8", "/a\n/b\n");

		assertResp(Request.create("GET", "/r/a/docs?l=true&r=true"), 200, "text/plain", "UTF-8", "/a/docs/file.txt\n/a/docs/some\n/a/docs/some/dir\n/a/docs/some/dir/other.txt\n");
		assertResp(Request.create("GET", "/r/a/config?l=true&r=true"), 200, "text/plain", "UTF-8", "/a/config/x.txt\n");
		assertResp(Request.create("GET", "/r/a/config/x.txt?l=true&r=true"), 200, "text/plain", "UTF-8", "");
		assertResp(Request.create("GET", "/r/a?l=true&r=true"), 200, "text/plain", "UTF-8",
				"/a/config\n/a/config/x.txt\n/a/docs\n/a/docs/file.txt\n/a/docs/some\n/a/docs/some/dir\n/a/docs/some/dir/other.txt\n/a/other.txt\n");
		assertResp(Request.create("GET", "/r/?l=true&r=true"), 200, "text/plain", "UTF-8",
				"/a\n/a/config\n/a/config/x.txt\n/a/docs\n/a/docs/file.txt\n/a/docs/some\n/a/docs/some/dir\n/a/docs/some/dir/other.txt\n/a/other.txt\n/b\n/b/other.txt\n");
	}

	@Test
	public void testMediaType() {
		assertEquals(new Integer(404), a.code("GET", "/r/file.txt"));
		assertEquals(new Integer(404), a.code("GET", "/r/with/some/dir/other"));
		assertEquals(new Integer(404), a.code("GET", "/r/file.texinfo"));

		assertEquals(new Integer(200), s.execute(Request.create("PUT", "/r/file.txt").contentType("application/octet-stream").content("my text file".getBytes())).code());
		assertEquals(new Integer(200), a.code("PUT", "/r/with/some/dir/other", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), s.execute(Request.create("PUT", "/r/file.texinfo").contentType("application/octet-stream").content("my text info".getBytes())).code());

		assertResp(Request.create("GET", "/r/file.txt"), 200, "text/plain", null, "my text file");
		assertResp(Request.create("GET", "/r/with/some/dir/other"), 200, "text/plain", "UTF-8", "my other file");
		assertResp(Request.create("GET", "/r/file.texinfo"), 200, "application/x-texinfo", null, "my text info");

		assertEquals(new Integer(200), a.code("DELETE", "/r/file.txt"));
		assertEquals(new Integer(200), a.code("DELETE", "/r/with/some/dir/other"));
		assertEquals(new Integer(200), a.code("DELETE", "/r/file.texinfo"));

		assertEquals(new Integer(404), a.code("GET", "/r/file.txt"));
		assertEquals(new Integer(404), a.code("GET", "/r/with/some/dir/other"));
		assertEquals(new Integer(200), a.code("DELETE", "/r/file.texinfo"));
	}

	@Test
	public void testListDeleteRepository() {
		Response resp = execute("main:123", "GET", "/access?path=/", null, null);
		assertEquals(new Integer(200), resp.code());
		assertEquals("", resp.content().text().trim());

		assertEquals(new Integer(200), a.code("PUT", "/r/a/x/f.txt", "text/plain;charset=UTF-8", "my other file"));
		assertEquals(new Integer(200), a.code("PUT", "/r/b/x/f.txt", "text/plain;charset=UTF-8", "my other file"));

		resp = execute("main:123", "GET", "/access?path=/", null, null);
		assertEquals(new Integer(200), resp.code());
		assertEquals("/a\n/b", resp.content().text().trim());

		assertEquals(new Integer(200), execute("main:123", "DELETE", "/access?path=/a", null, null).code());

		resp = execute("main:123", "GET", "/access?path=/", null, null);
		assertEquals(new Integer(200), resp.code());
		assertEquals("/b", resp.content().text().trim());
	}

}
