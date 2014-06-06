package com.murerz.repoz.web.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtil {

	public static String getURIWithoutContextPath(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();

		if (contextPath != null && !contextPath.isEmpty()) {
			uri = uri.replace(contextPath, "");
		}

		return uri;
	}

	public static String param(HttpServletRequest req, String name) {
		String ret = req.getParameter(name);
		if (ret != null) {
			ret = ret.trim();
		}
		return ret == null || ret.length() == 0 ? null : ret;
	}

	public static Cookie cookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	public static void writeJson(HttpServletResponse resp, Object object) {
		try {
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(object == null ? "null" : object.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void redirectByJS(HttpServletResponse resp, String url) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>");
		sb.append("<script language=\"javascript\">");
		sb.append("function redirect() {");
		sb.append("location = '").append(url).append("';");
		sb.append("}");
		sb.append("</script>");
		sb.append("</head><body onload=\"redirect();\"></body></html>");
		writeHtml(resp, sb);
	}

	public static void writeHtml(HttpServletResponse resp, Object html) {
		try {
			resp.setContentType("text/html");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(html == null ? "" : html.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void addCookie(HttpServletResponse resp, String name, String value, String path, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		resp.addCookie(cookie);

	}

	public static String paramRequire(HttpServletRequest req, String name) {
		String value = param(req, name);
		if (value == null) {
			throw new RuntimeException("param required: " + name);
		}
		return value;
	}

	public static void checkLength(HttpServletRequest req) {
		final Integer tamanho = 1024 * 1024;
		if (req.getContentLength() > tamanho) {
			throw new RuntimeException("Conteudo do Request maior que " + tamanho + "Bytes");
		}
	}

	public static void checkContentType(HttpServletRequest req, String contentType) {
		if (req.getContentType() == null || !req.getContentType().contains(contentType)) {
			throw new RuntimeException("Content Type Invalido");
		}
	}

	public static List<String> paramList(HttpServletRequest req, String name) {
		String[] ret = req.getParameterValues(name);
		if (ret == null) {
			ret = new String[0];
		}
		return Arrays.asList(ret);
	}

	public static void writeText(HttpServletResponse resp, String str) {
		try {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(str == null ? "" : str);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void checkHasMoreThan(Collection<?> collection, Long limit) {
		if (collection.size() > limit) {
			throw new RuntimeException("Nao e permitido salvar mais que " + limit + " registros por vez");
		}
	}

	public static void writeBytes(HttpServletResponse resp, String contentType, String charset, byte[] content) {
		try {
			resp.setContentType(contentType);
			if (charset != null) {
				resp.setCharacterEncoding(charset);
			}
			ServletOutputStream out = resp.getOutputStream();
			out.write(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getHostPort(HttpServletRequest req) {
		return new StringBuilder().append(req.getRemoteAddr()).append(":").append(req.getRemotePort()).toString();
	}

	public static UserPass getBasicInfo(HttpServletRequest req) {
		String auth = req.getHeader("Authorization");
		if (auth == null) {
			return null;
		}
		auth = auth.replaceAll("^Basic ", "");
		auth = CryptUtil.decodeBase64String(auth, "UTF-8");
		String[] array = auth.split(":");
		String username = Util.str(array[0]);
		String password = Util.str(array[1]);
		if (username == null || password == null) {
			return null;
		}
		return new UserPass().setUsername(username).setPassword(password);
	}

	public static void sendMethodNotAllowed(HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendNotFound(HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendUnauthorized(HttpServletResponse resp, HttpServletRequest req) {
		try {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendForbidden(HttpServletResponse resp, HttpServletRequest req) {
		try {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
