/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.instrument.feign.issues.issue307;

import java.util.ArrayList;
import java.util.List;

import brave.sampler.Sampler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@FeignClient("participants")
interface ParticipantsClient {

	@GetMapping("/races/{raceId}")
	List<Object> getParticipants(@PathVariable String raceId);

}

public class Issue307Tests {

	@Test
	public void should_start_context() {
		try (ConfigurableApplicationContext applicationContext = SpringApplication.run(
				SleuthSampleApplication.class, "--spring.jmx.enabled=false",
				"--server.port=0")) {
			// code
		}
	}

}

@EnableAutoConfiguration
@Import({ ParticipantsBean.class })
@RestController
@EnableFeignClients
class SleuthSampleApplication {

	private static final Logger LOG = LoggerFactory
			.getLogger(SleuthSampleApplication.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Environment environment;

	@Autowired
	private ParticipantsBean participantsBean;

	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	Sampler defaultSampler() {
		return Sampler.ALWAYS_SAMPLE;
	}

	@GetMapping("/")
	public String home() {
		LOG.info("you called home");
		return "Hello World";
	}

	@GetMapping("/callhome")
	public String callHome() {
		LOG.info("calling home");
		return this.restTemplate.getForObject("http://localhost:" + port(), String.class);
	}

	private int port() {
		return this.environment.getProperty("local.server.port", Integer.class);
	}

}

@Component
class ParticipantsBean {

	@Autowired
	private ParticipantsClient participantsClient;

	public List<Object> defaultParticipants(String raceId) {
		return new ArrayList<>();
	}

}
