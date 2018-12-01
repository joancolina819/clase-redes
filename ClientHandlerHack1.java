package web;

import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.*;
public class ClientHandlerHack1 implements Runnable{
	
	private final Socket socket;
	
	private String usuario="";

	//Esta clase es el cliente del serivico web y es quien administra la informaicon del usuario y la pagina web.
	public static final  String RUTA_PAGINA ="./recurso/data.txt";
	public ClientHandlerHack1(Socket socket)
	{
		this.socket =  socket;
	}
	
	@Override
	public void run() {
	
		System.out.println("\nClientHandler Started for " + this.socket);
		while(true) 
		{
			handleRequest(this.socket);
		}		
		
	}
	
	//Este metood se encarga de escuchar las solicitues de la pagina web
	public void handleRequest(Socket socket)
	{
		try {
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String headerLine = in.readLine();
			if(headerLine!=null)
			{
				
			
				System.out.println(headerLine);
				// A tokenizer is a process that splits text into a series of tokens
				StringTokenizer tokenizer =  new StringTokenizer(headerLine);
				//The nextToken method will return the next available token
				String httpMethod = tokenizer.nextToken();
				// The next code sequence handles the GET method. A message is displayed on the
				// server side to indicate that a GET method is being processed
				if(httpMethod.equals("GET"))
				{
					System.out.println("Get method processed");
					String httpQueryString = tokenizer.nextToken();
					System.out.println(httpQueryString);
					if(httpQueryString.equals("/"))
					{
						StringBuilder responseBuffer =  new StringBuilder();
						String str="";
						BufferedReader buf = new BufferedReader(new FileReader(System.getProperty("user.dir") +"/src/web/javascr.html"));
						
						while ((str = buf.readLine()) != null) {
							responseBuffer.append(str);
					    }
						sendResponse(socket, 200, responseBuffer.toString());		
					    buf.close();
					}
					if(httpQueryString.contains("/?gift="))
					{
						//System.out.println("Get method processed");
						String[] response =  httpQueryString.split("gift=");
						StringBuilder responseBuffer =  new StringBuilder();
						//AQUI ESTA EL TRUCO
						System.out.println(response[1]);
						if (verificarUsuario(response[1])) {
							
							String ruta = "recurso/user-"+response[1]+".txt";
							ArrayList<String> info = cargarInformacion(ruta);
							responseBuffer
							.append("<html>")
							.append("<head> <title> RESULTADOS </title> "
									+ "<style>"
									+ "body{"	
									+ "background:#000000;"
									+" color:#fff;"
									//+ "cursor: url(\"http://www.banderas-del-mundo.com/America_del_Sur/Colombia/colombia_mwd.gif\"), auto;"
									+ "}"
									+ "</style>"
									+ "</head> ")
							.append("<body bgcolor='black'>")
							.append("<font color='white'>"+response[1]+"</font><br>")
							.append("<h1 style='color:white'>" +"Resultados de la busqueda"+"</h1>");
							if (!info.isEmpty()) {
								
								for (int j = 0; j < info.size(); j++) {
									System.out.println(info.get(j));
									responseBuffer
									.append("<h1 style='color:white'>" +info.get(j)+"</h1>");
								}
							} else {
								responseBuffer
								.append("<h1 style='color:white'>" +"No hay resultados"+"</h1>");
							}
							responseBuffer
							.append("</body>")
							.append("</html>");
							sendResponse(socket, 200, responseBuffer.toString());	
									
						}
					
					    
					}
										    
				}
				
				else
				{
					System.out.println("The HTTP method is not recognized");
					sendResponse(socket, 405, "Method Not Allowed");
				}
			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	//Este metodo se encarga de verficar que la id del usuaio que se recibio de la pagina web si esta contenido en el txt de todos los usuarios
	@SuppressWarnings("resource")
	public boolean verificarUsuario(String usuer) throws IOException {
		
		File file = new File(RUTA_PAGINA);
		FileReader f = new FileReader(file);
		BufferedReader in = new  BufferedReader(f);
		String linea = in.readLine();

		while(linea!=null){
			if(linea.equals(usuer)) {
				return true;
			}else {
				
				linea= in.readLine();
			}
		}
		f.close();
		in.close();
		return false;
	}
	
	
	//Este metodo se encarga de cargar en una array todos los datos de las carreras que contiene el usuario con la ide que se recibio de la pagina web
	public ArrayList<String> cargarInformacion(String ruta) throws IOException   {
		System.out.println(ruta);
		ArrayList<String> datos = new ArrayList<>();
		File file = new File(ruta);
		FileReader f = new FileReader(file);
		BufferedReader in = new  BufferedReader(f);
		String linea = in.readLine();
		while(linea!=null){
			datos.add(linea);
		 linea = in.readLine();
		}
		in.close();
		f.close();
 		return datos;
	}
	
	//Este metood se encarga de armar el archivo web y enviar una respuesta al navegador
	public void sendResponse(Socket socket, int statusCode, String responseString)
	{
		String statusLine;
		String serverHeader = "Server: WebServer\r\n";
		String contentTypeHeader = "Content-Type: text/html\r\n";
		
		try {
			DataOutputStream out =  new DataOutputStream(socket.getOutputStream());
			if (statusCode == 200) 
			{
				statusLine = "HTTP/1.0 200 OK" + "\r\n";
				String contentLengthHeader = "Content-Length: "
				+ responseString.length() + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes(serverHeader);
				out.writeBytes(contentTypeHeader);
				out.writeBytes(contentLengthHeader);
				out.writeBytes("\r\n");
				out.writeBytes(responseString);
				} 
			else if (statusCode == 405) 
			{
				statusLine = "HTTP/1.0 405 Method Not Allowed" + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes("\r\n");
			} 
			else 
			{
				statusLine = "HTTP/1.0 404 Not Found" + "\r\n";
				out.writeBytes(statusLine);
				out.writeBytes("\r\n");
			}
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}