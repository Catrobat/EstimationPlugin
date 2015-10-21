package org.catrobat.estimationplugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import java.util.HashMap;
import java.util.Map;
;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class MyPluginServlet extends HttpServlet
{
    private static final String PLUGIN_STORAGE_KEY = "org.catrobat.estimationplugin";
    private static final Logger log = LoggerFactory.getLogger(MyPluginServlet.class);
    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer templateRenderer;
    private final PluginSettingsFactory pluginSettingsFactory;

    public MyPluginServlet(UserManager userManager,
                           LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username))
        {
            redirectToLogin(request, response);
            return;
        }
        //response.getWriter().write("<html><body>Hi again! Looking good.</body></html>");
        Map<String, Object> context = new HashMap<String, Object>();// Maps.newHashMap();

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".name") == null){
            String noName = "Enter a name here.";
            pluginSettings.put(PLUGIN_STORAGE_KEY +".name", noName);
        }

        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".age") == null){
            String noAge = "Enter an age here.";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".age", noAge);
        }

        context.put("name", pluginSettings.get(PLUGIN_STORAGE_KEY + ".name"));
        context.put("age", pluginSettings.get(PLUGIN_STORAGE_KEY + ".age"));
        response.setContentType("text/html;charset=utf-8");
        templateRenderer.render("admin.vm", response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(PLUGIN_STORAGE_KEY + ".name", req.getParameter("name"));
        pluginSettings.put(PLUGIN_STORAGE_KEY + ".age", req.getParameter("age"));
        response.sendRedirect("test");
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}