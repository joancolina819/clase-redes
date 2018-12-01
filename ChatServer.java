package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.net.ssl.SSLServerSocketFactory;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sql.rowset.spi.SyncResolver;

import UDPStreaming.AudioUDPServer;
import web.WebServerHack1;

//Esta clase es el servidor principal, el cual administra todos los srvicios que son realizar una apeusta, los servicios de striming y el serviico we. 
public class ChatServer {
 
	public static final String KEYSTORE_LOCATION= "C:/Program Files/Java/jre1.8.0_181/bin/clavesRedes";
	public static final String KEYSTORE_PASSWORD= "onepiece1";
	static ArrayList<Usuario> usuariosAux;
	static ArrayList<CharThread> hilos;
	static hiloMinuto hiloTiempo;
	static ArrayList<Socket> clientes;
	static HashMap<String, ArrayDeque<String>> mensajes;
	static ServerSocket server = null;
	static boolean salio = true;	
	static int[] apuestasCaballos;
	static int[] totalApuestasCaballos;

	public static void main(String[] args) {
//Estas son las claves para el ssl
		System.setProperty("javax.net.ssl.keyStore",KEYSTORE_LOCATION);
		System.setProperty("javax.net.ssl.keyStorePassword",KEYSTORE_PASSWORD);
		clientes = new ArrayList<>();
		//usuarios = new HashMap<>();
		usuariosAux = new ArrayList<>();
		mensajes = new HashMap<>();
		server = null;
		apuestasCaballos = new int[6];
		totalApuestasCaballos = new int[6];
		try {

			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			
			server = ssf.createServerSocket(1248);
			//Aquí se ejecuta la clase servidor que administrara el servicio de striming de sonido
			Runnable run =  new Runnable() {

				@Override
				public void run() {
					new AudioUDPServer();
				}
				
			};
			new Thread(run).start();
			
			//Aqui se ejecuta la clase serviodr que administra el front en la pagina web
			Runnable run2 =  new Runnable() {
				@Override
				public void run() {
					new WebServerHack1();
				}
			};
			new Thread(run2).start();
		
			//aqui se ejecuta el servidor que adminsitrara lac ancion de fondo que escuchan los clientes.
				CancionUDPServer cancion=	new CancionUDPServer();	
		cancion.start();

		//Aqui se ejecuta el hilo que determina la cantiadad de tiempo que tienen para apostar. y aqui se escucha las solicitudes de los clientes 
			hiloTiempo = new hiloMinuto();
			hiloTiempo.start();
			System.out.println("Servidor listo para recibir solicitudes");

			while (salio) {
				if (!server.isClosed()) {

				
					
					Socket client = server.accept();
					clientes.add(client);
					System.out.println("Solicitud recibida");
					CharThread hilo = new CharThread(client);
					hilo.start();
				}
			}

		} catch (Exception e) {
			
			 e.printStackTrace();
		}


	}
	

	//Calcula la totalidad de apuestas pro cada caballo
	static void calcularApuestasXcaballo() {
		for (int i = 0; i < usuariosAux.size(); i++) {
			if(usuariosAux.get(i).caballoApostado.equals(0+"")) {
				apuestasCaballos[0] = apuestasCaballos[0]+1;
			}else if ((usuariosAux.get(i).caballoApostado.equals(1+""))) {
				apuestasCaballos[1] = apuestasCaballos[1]+1;	
			} else if ((usuariosAux.get(i).caballoApostado.equals(2+""))) {
				apuestasCaballos[2] = apuestasCaballos[2]+1;
			}else if ((usuariosAux.get(i).caballoApostado.equals(3+""))) {
				apuestasCaballos[3] = apuestasCaballos[3]+1;
			}else if ((usuariosAux.get(i).caballoApostado.equals(4+""))) {
				apuestasCaballos[4] = apuestasCaballos[4]+1;
			} else if ((usuariosAux.get(i).caballoApostado.equals(5+""))) {
				apuestasCaballos[5] = apuestasCaballos[5]+1;
			}
		}
	}
	//cada entra un usuario al servidor se agrega un nuevo usuario de tipo Usuario a la lista de usuarios
	static synchronized void agregarUsuario(String nombre, String caballo, int cantidad) {
		Usuario nuevo = new Usuario(nombre, caballo, cantidad);
		usuariosAux.add(nuevo);
		totalApuestasCaballos[Integer.parseInt(caballo)] = totalApuestasCaballos[Integer.parseInt(caballo)]+cantidad;
	//	System.out.println(nombre+" gggg");
		for (int i = 0; i < usuariosAux.size(); i++) {
			System.out.println("Usuarios conectados: " + usuariosAux.get(i).getNombre()+" Apuesta: "+ usuariosAux.get(i).getCantidadApostada() +" Caballo: " +usuariosAux.get(i).getCaballoApostado()+"\n");
			}
	}
	/*
	 * pinta en consola
	 */
	static synchronized void print(String lin) {
		System.out.println(lin);
	}

	/*agrega mensajes de los usuarios a la lista de mensajes
	 * PRE: el usuario esta en linea
	 */
	static synchronized void agregarMensaje(String user, String mensaje) {
		mensajes.get(user).add(mensaje);
	}

	static synchronized boolean mensajesPendientes(String user) {
		return !mensajes.get(user).isEmpty();
	}

	/*obitene los mensajes pedniente de los cleintes.
	 * PRE: el usuario tiene mensajes pendientes
	 */
	static synchronized String getMensaje(String user) {
		return mensajes.get(user).remove();
	}

	
	
	//Esta clase hilo minuto se encarga de administrar el tiempo que 
	//tienen lo clientes para apostar. en este caso este hilo estará vivo por 40 segundos. Ademas, se encarga de enviar la informacion a los cliente para que puedan ver la carrera.
	static class hiloMinuto extends Thread {
		public int tiempo;
		public PrintWriter cOut;
		public Socket client;
		public hiloMinuto() {
		
		}
		@Override
		public void run() {

			while (tiempo <= 40) {

				
				tiempo++;
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("segundos:" + tiempo);
			}
			Random randon = new Random();
			int velocidad1= randon.nextInt(10)+1;
			int velocidad2= randon.nextInt(10)+1;
			int velocidad3= randon.nextInt(10)+1;
			int velocidad4= randon.nextInt(10)+1;
			int velocidad5= randon.nextInt(10)+1;
			int velocidad6= randon.nextInt(10)+1 ;
			calcularApuestasXcaballo();
			System.out.println("CANTIDAD DE APUESTAS POR CABALLO");
		for (int i = 0; i < apuestasCaballos.length; i++) {
			System.out.println("Caballo#"+i+": "+apuestasCaballos[i]);
		}
		System.out.println("TOTAL APUESTAS POR CABALLO");
		for (int i = 0; i < apuestasCaballos.length; i++) {
			System.out.println("Caballo#"+i+": "+totalApuestasCaballos[i]);
		}
			for (int i = 0; i < clientes.size(); i++) {
			
				try {
					cOut = new PrintWriter(clientes.get(i).getOutputStream(), true);
					cOut.println("La carrera acaba de iniciar");
					cOut.println(velocidad1);
					cOut.println(velocidad2);
					cOut.println(velocidad3);
					cOut.println(velocidad4);
					cOut.println(velocidad5);
					cOut.println(velocidad6);
				} catch (IOException e) {
				
					e.printStackTrace();
				}
			}

		}

	}

	//Esta clase hilo representa a un usuario, cuando un usuario entra al servidor se ejecuta por un hilo diferente. 
	static class CharThread extends Thread {

		public Socket client;
		public BufferedReader cIn;
		public PrintWriter cOut;
		public String user;

		public CharThread(Socket client) {

			this.client = client;
			try {
				cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
				cOut = new PrintWriter(client.getOutputStream(), true);
				user = cIn.readLine();
				String[] info = user.split("\\..");
				agregarUsuario(info[0],info[1], Integer.parseInt(info[2]));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		//Aquí dej al hilo corriendo siempre por que no es encesario matar al cliente en ningun momento.
		@Override
		public void run() {

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			try {

				String comando = "";

				while (!comando.equalsIgnoreCase("close")) {

				}

				cIn.close();
				cOut.close();
				client.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	//Esta clase maneja la informaicon del usuario de las carreras
	static class Usuario {

		private String nombre;
		private String caballoApostado;
		private int cantidadApostada;
		
		public Usuario(String nombre,String caballo, int cantidad) {
			caballoApostado=caballo;
		this.nombre= nombre;
			cantidadApostada=cantidad;
		}

		public String getCaballoApostado() {
			return caballoApostado;
		}

		public void setCaballoApostado(String caballoApostado) {
			this.caballoApostado = caballoApostado;
		}

		public int getCantidadApostada() {
			return cantidadApostada;
		}

		public void setCantidadApostada(int cantidadApostada) {
			this.cantidadApostada = cantidadApostada;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		
		
		
	}
	}



