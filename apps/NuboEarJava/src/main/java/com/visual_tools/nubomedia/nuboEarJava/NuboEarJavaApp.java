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


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Chroma main class.
 * 
 * @author Victor Manuel Hidalgo (vmhidalgo@visual-tolls.com)
 * @since 5.0.0
 */

@SpringBootApplication
@EnableWebSocket
public class NuboEarJavaApp implements WebSocketConfigurer {

	

	@Bean
	public NuboEarJavaHandler handler() {
		return new NuboEarJavaHandler();
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler(), "/nuboeardetector");
	}

	public static void main(String[] args) throws Exception {
		new SpringApplication(NuboEarJavaApp.class).run(args);
	}
}
