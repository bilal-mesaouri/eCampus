package com.example.eCampus;

import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.eCampus.bootstrap.StartupDataSeeder;

@SpringBootApplication
public class ECampusApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECampusApplication.class, args);
	}

	@Bean
	ApplicationRunner seedStartupData(List<StartupDataSeeder> seeders) {
		return args -> {
			for (StartupDataSeeder seeder : seeders) {
				seeder.seed();
			}
		};
	}
}
