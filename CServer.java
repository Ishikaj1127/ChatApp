import java.io.*;
import java.util.*;
import javax.swing.*;
import java.net.*;

public class CServer {
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Server");
        JTextArea textarea = new JTextArea(30, 50);
        textarea.setEditable(false);
        frame.add(new JScrollPane(textarea));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try (ServerSocket ss = new ServerSocket(12345)) {
            textarea.append("Server started on port 12345\n");
            while (true) {
                Socket socket = ss.accept();
                textarea.append("Client Connected: " + socket + "\n");
                ClientHandler ch = new ClientHandler(socket, textarea);
                clients.add(ch);
                new Thread(ch).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        JTextArea textarea;

        public ClientHandler(Socket socket, JTextArea textarea) {
            this.socket = socket;
            this.textarea = textarea;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String message;

                new Thread(this::recieveImage).start();

                while ((message = in.readLine()) != null) {
                    textarea.append("Client: " + message + "\n");
                    for (ClientHandler client : clients) {
                        client.out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void recieveImage() {
            try {
                FileOutputStream fout = new FileOutputStream("Recieve.png");
                InputStream fin = socket.getInputStream();
                byte[] buff = new byte[4096];
                int bytes;
                while ((bytes = fin.read(buff)) != -1) {
                    fout.write(buff, 0, bytes);
                }
                fout.close();
                textarea.append("Image received\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
