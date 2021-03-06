/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.visual_tools.nubomedia.nuboEarJava;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kurento.client.EventListener;
import org.kurento.client.KurentoClient;
import org.kurento.client.OnIceCandidateEvent; 
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.client.internal.NotEnoughResourcesException; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession; 
import org.springframework.web.socket.handler.TextWebSocketHandler; 
import org.kurento.client.EndpointStats; 
import org.kurento.client.Stats; 
import org.springframework.web.socket.CloseStatus; 
import org.kurento.module.nuboeardetector.*;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Chroma handler (application and media logic).
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author David Fernandez (d.fernandezlop@gmail.com)
 * @since 6.0.0
 */
public class NuboEarJavaHandler extends TextWebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(NuboEarJavaHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final ConcurrentHashMap<String, UserSession> users = new ConcurrentHashMap<String, UserSession>();

    private WebRtcEndpoint webRtcEndpoint = null;
    private NuboEarDetector ear = null;
    private int visualizeEar = -1;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
	throws Exception {
	JsonObject jsonMessage = gson.fromJson(message.getPayload(),
					       JsonObject.class);

	log.debug("Incoming message: {}", jsonMessage);

	switch (jsonMessage.get("id").getAsString()) {
	case "start":
	    start(session, jsonMessage);
	    break;
	case "show_ears":	
	    setVisualization(session,jsonMessage);
	    break;
	case "scale_factor":
	    setScaleFactor(session,jsonMessage);
	    break;
	case "process_num_frames":
	    setProcessNumberFrames(session,jsonMessage);
	    break;
	case "width_to_process":
	    setWidthToProcess(session,jsonMessage);
	    break;	
	case "get_stats":			
	    getStats(session);
	    break;
	case "stop": {
	    UserSession user = users.remove(session.getId());
	    if (user != null) {
		user.release();
	    }
	    break;
	}
	case "onIceCandidate": {
	    JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

	    UserSession user = users.get(session.getId());
	    if (user != null) {
		user.addCandidate(candidate);
	    }
	    break;
	}

	default:
	    error(session,"Invalid message with id " + jsonMessage.get("id").getAsString());
	    break;
	}
    }

    private void start(final WebSocketSession session, JsonObject jsonMessage) {
	try {

	    String sessionId = session.getId();
	    UserSession user = new UserSession(sessionId);
	    users.put(sessionId,user);
	    webRtcEndpoint = user.getWebRtcEndpoint();
	    
	    //Ice Candidate
	    webRtcEndpoint.addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
	      @Override
		  public void onEvent(OnIceCandidateEvent event) {
		  JsonObject response = new JsonObject();
		  response.addProperty("id", "iceCandidate");
		  response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
		  sendMessage(session, new TextMessage(response.toString()));
	      }
	    });

	    /******** Media Logic ********/
	    ear = new NuboEarDetector.Builder(user.getMediaPipeline()).build();

			
	    webRtcEndpoint.connect(ear);
	    ear.connect(webRtcEndpoint);
	    ear.activateServerEvents(1, 3000);
	    addEarListener();
	    // SDP negotiation (offer and answer)
	    String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
	    String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

	    // Sending response back to client
	    JsonObject response = new JsonObject();
	    response.addProperty("id", "startResponse");
	    response.addProperty("sdpAnswer", sdpAnswer);

	    synchronized (session) {
		sendMessage(session,new TextMessage(response.toString()));
	    }
	    webRtcEndpoint.gatherCandidates();

	} catch (NotEnoughResourcesException e) {
	    log.warn("Not enough resources", e);
	    notEnoughResources(session);	    
	}
	catch (Throwable t) {
	    log.error("Exception starting session", t);
	    error(session, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
    }

    private void addEarListener()
    {    	
    	ear.addOnEarListener(new EventListener<OnEarEvent>() {
	    	@Override
    		public void onEvent(OnEarEvent event)
    		{
	    		
	    		System.out.println("-------------------------Ear Detected-------------------------" );
    		}	    	
		});

    }
    private void notEnoughResources(WebSocketSession session) {
	// 1. Send notEnoughResources message to client
	JsonObject response = new JsonObject();
	response.addProperty("id", "notEnoughResources");
	sendMessage(session, new TextMessage(response.toString()));
	
	// 2. Release media session
	release(session);
    } 

    private void setVisualization(WebSocketSession session,JsonObject jsonObject)
    {

	try{
	    visualizeEar = jsonObject.get("val").getAsInt();
	    if (null != ear)
		{
		    System.out.println("Visuzlize Ear .... " + visualizeEar);
		    ear.showEars(visualizeEar);
		}

	} catch (Throwable t){
	    error(session, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
    }
	
    private void setScaleFactor(WebSocketSession session,JsonObject jsonObject)
    {
	
	try{
	    int scale = jsonObject.get("val").getAsInt();
	    
	    if (null != ear)
		{
		    log.debug("Sending setscaleFactor...." + scale);		  
		    ear.multiScaleFactor(scale);
		}
	    
	} catch (Throwable t){
	    error(session, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
    }

    private void setProcessNumberFrames(WebSocketSession session,JsonObject jsonObject)
    {
	
	try{
	    int num_img = jsonObject.get("val").getAsInt();
	    
	    if (null != ear)
		{
		    log.debug("Sending process num frames...." + num_img);
		    
		    ear.processXevery4Frames(num_img);
 		}
	    
	} catch (Throwable t){
	    error(session, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
    }
		
    private void setWidthToProcess(WebSocketSession session,JsonObject jsonObject)
    {
	
	try{
	    int width = jsonObject.get("val").getAsInt();
	    
	    if (null != ear)
		{
		    log.debug("Sending width...." + width);
		    ear.widthToProcess(width);
		}
	    
	} catch (Throwable t){
	    error(session, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
    } 

    private void getStats(WebSocketSession session)
    {    	
    	try {
	    Map<String,Stats> wr_stats= webRtcEndpoint.getStats();
    	
	    for (Stats s :  wr_stats.values()) {
    		
		switch (s.getType()) {		
		case endpoint:
		    EndpointStats end_stats= (EndpointStats) s;
		    double e2eVideLatency= end_stats.getVideoE2ELatency() / 1000000;
    				
		    JsonObject response = new JsonObject();
		    response.addProperty("id", "videoE2Elatency");
		    response.addProperty("message", e2eVideLatency);				
		    sendMessage(session,new TextMessage(response.toString()));				
		    break;
	
		default:	
		    break;
		}				
	    }
	} catch (Throwable t) {
	    log.error("Exception getting stats...", t);
	}
    }


    private synchronized void sendMessage(WebSocketSession session, TextMessage message) {
	try {
	    session.sendMessage(message);
	} catch (IOException e) {
	    log.error("Exception sending message", e);
	}
    }
    
    private void error(WebSocketSession session, String message) {
	
	JsonObject response = new JsonObject();
	response.addProperty("id", "error");
	response.addProperty("message", message);
	sendMessage(session,new TextMessage(response.toString()));
	// 2. Release media session
	release(session);
    }

    private void release(WebSocketSession session) {
	UserSession user = users.remove(session.getId());
	if (user != null) {
	    user.release();
	}
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
	log.info("Closed websocket connection of session {}", session.getId());
	release(session);
    }
}
