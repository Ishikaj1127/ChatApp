import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class CClient {
    private static PrintWriter out;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Client");
        JTextArea textarea = new JTextArea(20, 40);
        JTextField input = new JTextField(30);
        JButton send = new JButton("Send");
        JButton image = new JButton("Image");
        JPanel panel = new JPanel();
        textarea.setEditable(false);
        panel.add(input);
        panel.add(send);
        panel.add(image);
        frame.add(new JScrollPane(textarea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 12345);
            textarea.append("Connected to Server\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            send.addActionListener(e -> {
                String msg = input.getText();
                out.println(msg);
                input.setText("");
            });

            input.addActionListener(e -> {
                String msg = input.getText();
                out.println(msg);
                input.setText("");
            });

            image.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select an image");
                int result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        SendImage(socket, file);
                        textarea.append("Image sent\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        textarea.append("Sent: " + message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SendImage(Socket socket, File file) {
        try {
            byte[] buff = new byte[52428800];
            int bytes;
            FileInputStream fin = new FileInputStream(file);
            OutputStream fout = socket.getOutputStream();
            while ((bytes = fin.read(buff)) != -1) {
                fout.write(buff, 0, bytes);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
