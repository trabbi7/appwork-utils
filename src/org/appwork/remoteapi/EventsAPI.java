/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public abstract class EventsAPI implements EventsAPIInterface {

    private final HashMap<HttpSession, MinTimeWeakReference<EventsAPIQueue>> eventQueues  = new HashMap<HttpSession, MinTimeWeakReference<EventsAPIQueue>>();

    protected long                                                           queueTimeout = 5 * 60 * 1000;

    public abstract boolean isSessionAllowed(HttpSession session);

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response) {
        this.listen(request, response, -1l);
    }

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, final Long lastEventID) {
        if (!(request instanceof SessionRemoteAPIRequest)) { throw new RemoteAPIUnauthorizedException(); }
        final SessionRemoteAPIRequest<? extends HttpSession> sr = (SessionRemoteAPIRequest<?>) request;
        if (!this.isSessionAllowed(sr.getSession())) {
            /* session is not allowed */
            synchronized (this) {
                this.eventQueues.remove(sr.getSession());
            }
            throw new RemoteAPIUnauthorizedException();
        }
        if (sr.getSession() != null && !sr.getSession().isAlive()) {
            /* session no longer alive, remove it */
            synchronized (this) {
                this.eventQueues.remove(sr.getSession());
            }
            throw new RemoteAPIUnauthorizedException();
        }
        /* response object */
        HashMap<String, Object> ret = new HashMap<String, Object>();

        EventsAPIQueue queue = null;
        synchronized (this) {
            final MinTimeWeakReference<EventsAPIQueue> mqueue = this.eventQueues.get(sr.getSession());
            if (mqueue == null || (queue = mqueue.get()) == null) {
                queue = new EventsAPIQueue();
                this.eventQueues.put(sr.getSession(), new MinTimeWeakReference<EventsAPIQueue>(queue, this.queueTimeout, "EventQueue for" + sr.getSession().getSessionID()));
            }
        }
        EventsAPIEvent event = queue.pullEvent();
        synchronized (queue) {
            if (event == null) {
                try {
                    queue.wait(30 * 1000l);
                } catch (final InterruptedException e) {
                }
            }
        }
        if (event == null) {
            event = queue.pullEvent();
        }
        final ArrayList<HashMap<String, Object>> eventArray = new ArrayList<HashMap<String, Object>>();
        boolean checkID = lastEventID >= 0;
        while (event != null) {
            final HashMap<String, Object> eventJson = new HashMap<String, Object>();
            if (event.getProcessID() != null) {
                eventJson.put("pid", event.getProcessID());
            }
            eventJson.put("messageid", event.getMessageID());
            if (checkID) {
                /* check if we are out of sync */
                if (event.getMessageID() != lastEventID + 1) { throw new RemoteAPIOutOfSyncException(); }
                checkID = false;
            }
            eventJson.put("data", event.getData());
            eventArray.add(eventJson);

            ret.put("data", eventArray);
            ret.put("id", event.getMessageID());
            event = queue.pullEvent();
        }

        if (ret.size() == 0) {
            /* no events to send, send heartbeat */
            ret = new EventsAPIHeartbeat().getRemoteAPIExceptionResponse();
        }
        String text = JSonStorage.toString(ret);

        if (request.getJqueryCallback() != null) {
            /* wrap response into a valid jquery callback response format */
            final StringBuilder sb = new StringBuilder();
            sb.append(request.getJqueryCallback());
            sb.append("(");
            sb.append(text);
            sb.append(");");
            text = sb.toString();
        }
        int length;
        try {
            length = text.getBytes("UTF-8").length;
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text/javascript"));
            response.getOutputStream().write(text.getBytes("UTF-8"));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void publishEvent(final EventsAPIEvent event, final HttpSession... receivers) {
        if (receivers == null || receivers.length == 0) { return; }
        for (final HttpSession receiver : receivers) {
            EventsAPIQueue queue = null;
            synchronized (this) {
                if (!receiver.isAlive()) {
                    /* session no longer alive, remove it */
                    this.eventQueues.remove(receiver);
                    continue;
                }
                final MinTimeWeakReference<EventsAPIQueue> mqueue = this.eventQueues.get(receiver);
                if (mqueue == null || (queue = mqueue.get()) == null) {
                    queue = new EventsAPIQueue();
                    this.eventQueues.put(receiver, new MinTimeWeakReference<EventsAPIQueue>(queue, 5 * 60 * 1000, "EventQueue for" + receiver.getSessionID()));
                }
            }
            queue.pushEvent(event);
            synchronized (queue) {
                queue.notify();
            }
        }
    }
}