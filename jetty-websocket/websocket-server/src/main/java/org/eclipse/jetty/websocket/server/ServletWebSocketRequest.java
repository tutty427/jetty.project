package org.eclipse.jetty.websocket.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.websocket.protocol.ExtensionConfig;

public class ServletWebSocketRequest extends HttpServletRequestWrapper implements WebSocketRequest
{
    private List<String> subProtocols = new ArrayList<>();
    private List<ExtensionConfig> extensions;

    public ServletWebSocketRequest(HttpServletRequest request)
    {
        super(request);

        Enumeration<String> protocols = request.getHeaders("Sec-WebSocket-Protocol");
        String protocol = null;
        while ((protocol == null) && (protocols != null) && protocols.hasMoreElements())
        {
            String candidate = protocols.nextElement();
            for (String p : parseProtocols(candidate))
            {
                subProtocols.add(p);
            }
        }

        extensions = new ArrayList<>();
        Enumeration<String> e = request.getHeaders("Sec-WebSocket-Extensions");
        while (e.hasMoreElements())
        {
            QuotedStringTokenizer tok = new QuotedStringTokenizer(e.nextElement(),",");
            while (tok.hasMoreTokens())
            {
                extensions.add(ExtensionConfig.parse(tok.nextToken()));
            }
        }
    }

    @Override
    public List<ExtensionConfig> getExtensions()
    {
        return extensions;
    }

    @Override
    public String getHost()
    {
        return getHeader("Host");
    }

    /**
     * Get the endpoint of the WebSocket connection.
     * <p>
     * Per the <a href="https://tools.ietf.org/html/rfc6455#section-1.3">Opening Handshake (RFC 6455)</a>
     */
    @Override
    public String getHttpEndPointName()
    {
        return getRequestURI();
    }

    @Override
    public String getOrigin()
    {
        String origin = getHeader("Origin");
        if (origin == null)
        {
            // Fall back to older version
            origin = getHeader("Sec-WebSocket-Origin");
        }
        return origin;
    }

    @Override
    public List<String> getSubProtocols()
    {
        return subProtocols;
    }

    @Override
    public boolean hasSubProtocol(String test)
    {
        return subProtocols.contains(test);
    }

    @Override
    public boolean isOrigin(String test)
    {
        return test.equalsIgnoreCase(getOrigin());
    }

    protected String[] parseProtocols(String protocol)
    {
        if (protocol == null)
        {
            return new String[]
            { null };
        }
        protocol = protocol.trim();
        if ((protocol == null) || (protocol.length() == 0))
        {
            return new String[]
            { null };
        }
        String[] passed = protocol.split("\\s*,\\s*");
        String[] protocols = new String[passed.length + 1];
        System.arraycopy(passed,0,protocols,0,passed.length);
        return protocols;
    }

}
