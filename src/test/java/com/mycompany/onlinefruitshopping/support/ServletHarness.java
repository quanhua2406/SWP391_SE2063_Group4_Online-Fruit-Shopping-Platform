package com.mycompany.onlinefruitshopping.support;

import com.mycompany.onlinefruitshopping.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.*;
import java.util.*;

/** HTTP boundary doubles only; authorization is always production code. */
public final class ServletHarness {
    public final Map<String, Object> sessionAttributes = new HashMap<>();
    public final Map<String, Object> attributes = new HashMap<>();
    public final Map<String, String> parameters = new HashMap<>();
    public final Map<String, String> headers = new HashMap<>();
    public final Map<String, String> responseHeaders = new HashMap<>();
    public String path = "/profile", method = "GET", context = "/fruit", redirect;
    public int status = 200, chainCalls, sessionsCreated, sessionIdChanges;
    public String forwarded, pathInfo;
    public boolean hasSession, invalidated;
    public final HttpSession session = proxy(HttpSession.class, (p, m, a) -> switch (m.getName()) {
        case "getAttribute" -> sessionAttributes.get(a[0]);
        case "setAttribute" -> { sessionAttributes.put((String) a[0], a[1]); yield null; }
        case "removeAttribute" -> { sessionAttributes.remove(a[0]); yield null; }
        case "invalidate" -> { invalidated = true; hasSession = false; sessionAttributes.clear(); yield null; }
        default -> throw new UnsupportedOperationException(m.getName());
    });
    private final ServletContext servletContext = proxy(ServletContext.class, (p, m, a) -> {
        if (m.getName().equals("log")) return null;
        throw new UnsupportedOperationException(m.getName());
    });
    public final HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> switch (m.getName()) {
        case "getSession" -> {
            boolean create = a == null || a.length == 0 || Boolean.TRUE.equals(a[0]);
            if (!hasSession && create) { hasSession = true; sessionsCreated++; }
            yield hasSession ? session : null;
        }
        case "getServletPath" -> path;
        case "getMethod" -> method;
        case "getContextPath" -> context;
        case "getPathInfo" -> pathInfo;
        case "getQueryString" -> null;
        case "changeSessionId" -> { sessionIdChanges++; yield "new-session-id"; }
        case "getRequestDispatcher" -> proxy(RequestDispatcher.class, (p2, m2, a2) -> {
            if (!m2.getName().equals("forward")) throw new UnsupportedOperationException(m2.getName());
            forwarded = (String) a[0]; return null;
        });
        case "getServletContext" -> servletContext;
        case "getHeader" -> headers.get(a[0]);
        case "getParameter" -> parameters.get(a[0]);
        case "getAttribute" -> attributes.get(a[0]);
        case "setAttribute" -> { attributes.put((String) a[0], a[1]); yield null; }
        case "setCharacterEncoding" -> null;
        default -> throw new UnsupportedOperationException(m.getName());
    });
    public final HttpServletResponse response = proxy(HttpServletResponse.class, (p, m, a) -> switch (m.getName()) {
        case "setStatus", "sendError" -> { status = (int) a[0]; yield null; }
        case "sendRedirect" -> { status = 302; redirect = (String) a[0]; yield null; }
        case "setHeader" -> { responseHeaders.put((String) a[0], (String) a[1]); yield null; }
        case "setCharacterEncoding" -> null;
        default -> throw new UnsupportedOperationException(m.getName());
    });
    public final FilterChain chain = (req, res) -> chainCalls++;
    public void login(String role) { hasSession = true; sessionAttributes.put("user", user(10, role)); }
    public static User user(int id, String role) {
        User u = new User(); u.setUserID(id); u.setRoleName(role); u.setStatus("active"); return u;
    }
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
