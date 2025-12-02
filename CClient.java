import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class CClient {
    static PrintWriter textOut;
    static DataOutputStream dataOut;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Client");
        JTextArea textarea = new JTextArea(20, 40);
        JTextField input = new JTextField(30);
        JButton send = new JButton("Send");
        JButton img = new JButton("Image");
        JPanel panel = new JPanel();

        textarea.setEditable(false);
        panel.add(input);
        panel.add(send);
        panel.add(img);

        frame.add(new JScrollPane(textarea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 12345);
            textarea.append("Connected to server\n");

            BufferedReader textIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());

            textOut = new PrintWriter(socket.getOutputStream(), true);
            dataOut = new DataOutputStream(socket.getOutputStream());

            send.addActionListener(e -> {
                String msg = input.getText();
                sendText(msg);
                input.setText("");
            });

            input.addActionListener(e -> {
                String msg = input.getText();
                sendText(msg);
                input.setText("");
            });

            img.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    sendImage(file, textarea);
                }
            });

            new Thread(() -> {
                try {
                    while (true) {
                        String header = textIn.readLine();
                        if (header == null)
                            break;

                        if (header.startsWith("TEXT:")) {
                            textarea.append("Server: " + header.substring(5) + "\n");

                        } else if (header.startsWith("IMAGE:")) {
                            int size = Integer.parseInt(header.substring(6));
                            byte[] imgData = dataIn.readNBytes(size);

                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void sendText(String msg) {
        textOut.println("TEXT:" + msg);
    }

    private static void sendImage(File file, JTextArea textarea) {
        try {
            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());

            textOut.println("IMAGE:" + data.length);
            dataOut.write(data);
            dataOut.flush();

            textarea.append("Image sent\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
