package chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//esta clase es un hilo que enviar una cancion por tcp a los clientes conectados al servidor. 
public class CancionUDPServer extends Thread{

	public CancionUDPServer() {
		
	}

	public void run() {
		File soundFile = AudioUtil.getSoundFile("recurso/Queen - Dont Stop Me Now (Official Video).wav");

		System.out.println("server: " + soundFile);

		try (ServerSocket serverSocker = new ServerSocket(6666); 
				FileInputStream in = new FileInputStream(soundFile)) {
			if (serverSocker.isBound()) {
				Socket client = serverSocker.accept();
				OutputStream out = client.getOutputStream();

				byte buffer[] = new byte[2048];
				int count;
				while ((count = in.read(buffer)) != -1)
					out.write(buffer, 0, count);
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		}

		System.out.println("server: shutdown");
	}
}