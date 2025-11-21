package com.vietct.OrderFlow;

import org.springframework.boot.SpringApplication;

public class TestOrderFlowApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderFlowApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
