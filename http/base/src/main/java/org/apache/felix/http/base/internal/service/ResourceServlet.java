/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.base.internal.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ResourceServlet extends HttpServlet
{
    private final String path;

    public ResourceServlet(String path)
    {
        this.path = path;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String target = req.getPathInfo();
        if (target == null)
        {
            target = "";
        }

        if (!target.startsWith("/"))
        {
            target += "/" + target;
        }

        String resName = this.path + target;
        URL url = getServletContext().getResource(resName);

        if (url == null)
        {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else
        {
            handle(req, res, url, resName);
        }
    }

    private void handle(HttpServletRequest req, HttpServletResponse res, URL url, String resName) throws IOException
    {
        String contentType = getServletContext().getMimeType(resName);
        if (contentType != null)
        {
            res.setContentType(contentType);
        }

        long lastModified = getLastModified(url);
        if (lastModified != 0)
        {
            res.setDateHeader("Last-Modified", lastModified);
        }

        if (!resourceModified(lastModified, req.getDateHeader("If-Modified-Since")))
        {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        else
        {
            copyResource(url, res);
        }
    }

    private long getLastModified(URL url)
    {
        long lastModified = 0;

        try
        {
            URLConnection conn = url.openConnection();
            lastModified = conn.getLastModified();
        }
        catch (Exception e)
        {
            // Do nothing
        }

        if (lastModified == 0)
        {
            String filepath = url.getPath();
            if (filepath != null)
            {
                File f = new File(filepath);
                if (f.exists())
                {
                    lastModified = f.lastModified();
                }
            }
        }

        return lastModified;
    }

    private boolean resourceModified(long resTimestamp, long modSince)
    {
        modSince /= 1000;
        resTimestamp /= 1000;

        return resTimestamp == 0 || modSince == -1 || resTimestamp > modSince;
    }

    private void copyResource(URL url, HttpServletResponse res) throws IOException
    {
        URLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        try
        {
            conn = url.openConnection();

            is = conn.getInputStream();
            os = res.getOutputStream();
            // FELIX-3987 content length should be set *before* any streaming is done 
            // as headers should be written before the content is actually written...
            int len = getContentLength(conn);
            if (len >= 0)
            {
                res.setContentLength(len);
            }

            byte[] buf = new byte[1024];
            int n;

            while ((n = is.read(buf, 0, buf.length)) >= 0)
            {
                os.write(buf, 0, n);
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }

            if (os != null)
            {
                os.close();
            }
        }
    }

    private int getContentLength(URLConnection conn)
    {
        int length = -1;

        length = conn.getContentLength();
        if (length < 0)
        {
            // Unknown, try whether it is a file, and if so, use the file 
            // API to get the length of the content...
            String path = conn.getURL().getPath();
            if (path != null)
            {
                File f = new File(path);
                // In case more than 2GB is streamed 
                if (f.length() < Integer.MAX_VALUE)
                {
                    length = (int) f.length();
                }
            }
        }
        return length;
    }
}
