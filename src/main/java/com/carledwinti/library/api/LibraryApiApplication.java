package com.carledwinti.library.api;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling //para habilitar o agendamento de tarefas no Spring
public class LibraryApiApplication {

	@Bean //criando uma instancia Singleton para disponibilizar no context para outras classes via Injection
	public ModelMapper modelMapper(){
	return new ModelMapper();
	}

	//para anotar um method e permitir que ele seja executado de forma schedulada/agendada de tempos em tempos
	//utilizando a configuração de cron 'cronologico/tempo/cronologia'
	//qualquer classe gerenciada pelo Spring pode receber um method deste tipo
	//cron = "segundo minuto hora dia mês ano"
	//cron = "   1      2     3    4  5    6 "
	//@Scheduled(cron="10 * * * * *")//neste caso a tarefa test será executada todos os dias do ano a cada 1 minuto
	@Scheduled(cron="1/30 * * * * *")//a cada 30 segundos de todas as horas de todos os dias.
	public void testeAgendamentoTarefas(){
		System.out.println("Executou uma tarefa teste agendada: " + LocalDateTime.now().toString());
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
