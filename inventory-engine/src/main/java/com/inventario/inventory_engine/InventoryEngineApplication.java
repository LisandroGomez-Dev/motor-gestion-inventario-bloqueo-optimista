package com.inventario.inventory_engine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Esta es la clase principal de la aplicación.
 * Spring Boot la usa para levantar todo el proyecto web y su contexto de dependencias.
 * En términos simples: aquí empieza la API del sistema.
 */
@SpringBootApplication
public class InventoryEngineApplication {

	/**
	 * Punto de entrada de la aplicación.
	 * Cuando ejecutamos la app, Java llama a este método y Spring Boot se encarga de iniciar la API.
	 */
	public static void main(String[] args) {
		SpringApplication.run(InventoryEngineApplication.class, args);
	}

}
