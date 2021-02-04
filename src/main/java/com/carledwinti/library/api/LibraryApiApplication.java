package com.carledwinti.library.api;

import com.carledwinti.library.api.service.EmailService;
import org.hibernate.validator.constraints.Email;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling //para habilitar o agendamento de tarefas no Spring
//public class LibraryApiApplication extends SpringBootServletInitializer { /*sem tomcat, vai inicializar a aplicação a partir de um servlet*/
public class LibraryApiApplication { /*com tomcat tomcat embedded, vai inicializar a aplicação a partir do tomcat embarcado*/

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
	/*@Scheduled(cron="1/30 * * * * *")//a cada 30 segundos de todas as horas de todos os dias.
	public void testeAgendamentoTarefas(){
		System.out.println("Executou uma tarefa teste agendada: " + LocalDateTime.now().toString());
	}*/

	//****Somente para teste local
	//commandLineRunner para executar coisas assim que subir a aplicação.
	/*@Autowired
	private EmailService emailService;
	@Bean
	public CommandLineRunner commandLineRunner(){
		return args -> {
			String mail1 = "carlinstr@gmail.com";
			String mail2 = "library-0d0860@inbox.mailtrap.io";
			String mail3 = "email2@oemail.com";
			List<String> emailTest = Arrays.asList(mail1, mail2, mail3);
			String message = "Testando serviço de EMAILS";
			emailService.sendEmailToLoansOverdue(message, emailTest);
			System.out.println("EMAILS ENVIADOS COM SUCESSO!");
		};
	}*/
	//****Somente para teste local

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
