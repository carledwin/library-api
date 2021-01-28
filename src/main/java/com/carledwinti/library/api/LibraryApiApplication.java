package com.carledwinti.library.api;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LibraryApiApplication {

	@Bean //criando uma instancia Singleton para disponibilizar no context para outras classes via Injection
	public ModelMapper modelMapper(){
	return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
