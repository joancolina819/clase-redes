package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Date;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import UDPStreaming.AudioUDPClient;
//Esta clase funciona como el cliente principal del servidor principal.

public class ChatClient {

	public static final String TRUSTTORE_LOCATION="C:/Program Files/Java/jre1.8.0_181/bin/clavesRedes";
	static int[] velocidades;
	static int caballoApostado;
	static AudioInputStream audioInputStream;
	static SourceDataLine sourceDataLine;
	static String usuario;
	static String date;
	static String cabal;
	static String canti;
	static String gano="NO";
	public static void main(String[] args) {
		

		System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);
		SSLSocketFactory ssl = (SSLSocketFactory) SSLSocketFactory.getDefault();
		velocidades= new int[6];
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {

			
			Socket client = ssl.createSocket("localhost", 1248);
			//Aquí se inicia por un Hilo el cliente que ejecutara el strimig de sonido 
			Runnable run =  new Runnable() {
				
				@Override
				public void run() {
					new AudioUDPClient();
					
				}
				
			};
			new Thread(run).start();
			
			//Auí se inicializa por otro hilo el cliente que ejecutara el strimi de a cancion de fondo
			CancionUDPCliente cancion=	new CancionUDPCliente();	
			cancion.start();
		
			//de aquí apra bajo se lee y se envia la informacion al servidor.
			System.out.println("Conectado al servidor");

			BufferedReader cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter cOut = new PrintWriter(client.getOutputStream(), true);

		String comando ="";
			System.out.print("Ingrese su nombre de usuario: ");
			String nombre = in.readLine();
			String caballo = in.readLine();
			caballoApostado=Integer.parseInt(caballo);
			String cantidad = in.readLine();
			cOut.println(nombre+".."+caballo+".."+cantidad);

			usuario=nombre;
			verificarUsuario();
			cabal = caballo;
			canti = cantidad;
			java.util.Date fecha = new Date();
			date =fecha.toString();
			while (!comando.equalsIgnoreCase("close")) {

				if (in.ready()) {
					comando = in.readLine();
					if (comando.equalsIgnoreCase("close")) {
						break;
					} 
				}

			
					String mensajeServidor= cIn.readLine();
					System.out.println(mensajeServidor);
					
					if (mensajeServidor.equals("La carrera acaba de iniciar")) {
						for (int i = 0; i < velocidades.length; i++) {
							velocidades[i]=Integer.parseInt(cIn.readLine());
						}
						HiloPintar pintar= new HiloPintar(velocidades);
						pintar.start();
					}
				

			}

			cIn.close();
			cOut.close();
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	//Esta clase Hilo se encarga de pintar por consola la carrera de los caballos
	static class HiloPintar extends Thread{
		
		int[] velocidades;
		String[] caballos;
		int tiempo;
		
		public HiloPintar(int[] velocidades) {
			//Aqui se inicializan los caballos y se marca el caballo al que aposto el usuarioo
			this.velocidades=velocidades;
			caballos = new String[velocidades.length];
			for (int i = 0; i < velocidades.length; i++) {
				if (i==caballoApostado) {
					caballos[i]=i+"*";
				}else {
					
					
					caballos[i]=i+"";
				}
			}
			tiempo=0;
		}
		public  void run(){
			
			while (tiempo<=20) {
				tiempo++;
				
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int i = 0; i < ChatClient.velocidades.length; i++) {
					
					System.out.println(caballos[i]+"\n");
				}
				System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				
				for (int i = 0; i < ChatClient.velocidades.length; i++) {
					String avance="";
					for (int j = 0; j <velocidades[i];  j++) {
						avance=avance+"-";
					}
					caballos[i]=caballos[i]+avance;
				}
			}
			int max=velocidades[0];
			String c=""+0;
			
			//Aqui se calcula que caballo fue el ganador de la carrera 
			for(int i = 0; i < velocidades.length; i++)
			{
		
				if(max<velocidades[i])
				{
					c=""+i;
				}
			}
			if (c.equals(cabal)) {
				gano="SI";
			}
			System.out.println("El caballo ganador es: #"+c);
			
			System.out.println("Fin de la carrera");
			
			//aquí se guardarn los datos de la carrera.
			String ruta = "recurso/user-"+usuario+".txt";
			try {
				guardarDatos(ruta);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	//Este metoodo se encarga de verificar que el usuario que esta usando esta clase cliente ya esta registrado en el txr de usuarios. en caso de no ser así lo agrega al txt
	static public void verificarUsuario() throws IOException {
		File file = new File("recurso/data.txt");
		FileReader r = new FileReader(file);
		BufferedReader read = new BufferedReader(r);
		FileWriter f = new FileWriter(file,true);
		BufferedWriter w = new BufferedWriter(f);
		String linea = read.readLine();
		boolean encontro =false;
		while (linea!=null) {
			if (linea.equals(usuario)) {
				encontro=true;
			}
			linea= read.readLine();
		}
		if (!encontro) {
			w.write(usuario);
		}
		read.close();
		w.close();
	}
	
	//Este metodo se encarga de guardar los datos de la carrera despues de cada carrera. los datos que se guardarn  son fecha y cantidad y caballo y si gano la carrera el caballo al que se aposto-.
	static public void guardarDatos(String ruta) throws IOException {
		File file = new File(ruta);
		FileWriter f = new FileWriter(file,true);
		BufferedWriter w = new BufferedWriter(f);
		
		if (file.exists()) {
			w.write("\n");
			w.write("FECHA:"+date+" CANTIDAD APOSTADA: "+canti+ " CABALLO: "+ cabal+" GANO CARRERA:"+gano);
			
		} 
		w.close();
	}
	
}
