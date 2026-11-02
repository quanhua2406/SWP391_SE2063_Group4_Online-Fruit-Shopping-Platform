package com.mycompany.onlinefruitshopping.support;

import com.mycompany.onlinefruitshopping.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.*;
import java.util.*;

/** Minimal request/session boundary for filter tests; no servlet container or database. */
public final class ServletHarness {
    public final Map<String, Object> sessionValues = new HashMap<>();
    public final Map<String, Object> attributes = new HashMap<>();
    public final Map<String, String> parameters = new HashMap<>();
    public final Map<String, String> headers = new HashMap<>();
    public final Map<String, String> responseHeaders = new HashMap<>();
    public boolean hasSession;
    public boolean invalidated;
    public int status = 200;
    public int calls;
    public String redirect;
    public String query;
    public String pathInfo;
    public final String path;
    public final String method;
    public final HttpServletRequest request;
    public final HttpServletResponse response;
    public final FilterChain chain = (req, res) -> calls++;
    private final HttpSession session;

    public ServletHarness(String path, String method) {
        this.path = path;
        this.method = method;
        session = proxy(HttpSession.class, (object, called, args) -> {
            return switch (called.getName()) {
                case "getAttribute" -> sessionValues.get(args[0]);
                case "setAttribute" -> { sessionValues.put((String) args[0], args[1]); yield null; }
                case "removeAttribute" -> { sessionValues.remove(args[0]); yield null; }
                case "invalidate" -> { invalidated = true; hasSession = false; sessionValues.clear(); yield null; }
                case "getAttributeNames" -> Collections.enumeration(sessionValues.keySet());
                case "getId" -> "test-session";
                default -> defaultValue(called.getReturnType());
            };
        });
        ServletContext context = proxy(ServletContext.class,
                (object, called, args) -> defaultValue(called.getReturnType()));
        request = proxy(HttpServletRequest.class, (object, called, args) -> {
            return switch (called.getName()) {
                case "getMethod" -> method;
                case "getServletPath" -> path;
                case "getContextPath" -> "/OnlineFruitShopping";
                case "getRequestURI" -> "/OnlineFruitShopping" + path;
                case "getPathInfo" -> pathInfo;
                case "getQueryString" -> query;
                case "getDispatcherType" -> DispatcherType.REQUEST;
                case "getServletContext" -> context;
                case "getSession" -> {
                    boolean create = args == null || args.length == 0 || Boolean.TRUE.equals(args[0]);
                    if (create) hasSession = true;
                    yield hasSession ? session : null;
                }
                case "getParameter" -> parameters.get(args[0]);
                case "getHeader" -> headers.get(args[0]);
                case "getAttribute" -> attributes.get(args[0]);
                case "setAttribute" -> { attributes.put((String) args[0], args[1]); yield null; }
                default -> defaultValue(called.getReturnType());
            };
        });
        response = proxy(HttpServletResponse.class, (object, called, args) -> {
            return switch (called.getName()) {
                case "sendError", "setStatus" -> { status = (int) args[0]; yield null; }
                case "sendRedirect" -> { status = 302; redirect = (String) args[0]; yield null; }
                case "setHeader" -> { responseHeaders.put((String) args[0], (String) args[1]); yield null; }
                default -> defaultValue(called.getReturnType());
            };
        });
    }

    public HttpSession session() { hasSession = true; return session; }

    public ServletHarness as(int id, String role) {
        session().setAttribute("user", user(id, role));
        return this;
    }

    public static User user(int id, String role) {
        User user = new User();
        user.setUserID(id);
        user.setRoleName(role);
        user.setStatus("active");
        return user;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}
