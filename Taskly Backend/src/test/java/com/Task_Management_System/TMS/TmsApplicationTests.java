package com.Task_Management_System.TMS;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Starts the whole application, so it needs PostgreSQL (skip with -DexcludedGroups=database)
@Tag("database")
@SpringBootTest
class TmsApplicationTests {

	@Test
	void contextLoads() {
	}

}
