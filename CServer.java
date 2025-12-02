import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class CServer {
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Server");
        JTextArea textarea = new JTextArea(20, 40);
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
                synchronized (clients) {
                    clients.add(ch);
                }
                new Thread(ch).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        Socket socket;
        BufferedReader textIn;
        DataInputStream dataIn;
        PrintWriter textOut;
        DataOutputStream dataOut;
        JTextArea textarea;

        public ClientHandler(Socket socket, JTextArea textarea) throws IOException {
            this.socket = socket;
            this.textarea = textarea;
            this.textIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.dataIn = new DataInputStream(socket.getInputStream());
            this.textOut = new PrintWriter(socket.getOutputStream(), true);
            this.dataOut = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String header = textIn.readLine();
                    if (header == null)
                        break;

                    if (header.startsWith("TEXT:")) {
                        handleText(header.substring(5));

                    } else if (header.startsWith("IMAGE:")) {
                        int size = Integer.parseInt(header.substring(6));
                        handleImage(size);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clients) {
                    clients.remove(this);
                }
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private void handleText(String msg) {
            textarea.append("Client: " + msg + "\n");

            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.textOut.println("TEXT:" + msg);
                }
            }
        }

        private void handleImage(int size) throws IOException {
            textarea.append("Receiving image (" + size + " bytes)\n");

            byte[] imgData = dataIn.readNBytes(size);

            try (FileOutputStream out = new FileOutputStream("Received_From_Client.png")) {
                out.write(imgData);
            }

        }
    }
}
