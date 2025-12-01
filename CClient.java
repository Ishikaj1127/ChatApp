import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class CClient {
    private static PrintWriter out;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Client");
        JTextArea textarea = new JTextArea(30, 50);
        JTextField input = new JTextField(40);
        JButton send = new JButton("Send");
        JButton img = new JButton("Image");
        JPanel panel = new JPanel();
        textarea.setEditable(false);
        panel.add(input);
        panel.add(send);
        panel.add(img);
        frame.add(new JScrollPane(textarea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        try {
            Socket socket = new Socket("localhost", 12345);
            textarea.append("Connected to server\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            send.addActionListener(e -> {
                String message = input.getText();
                out.println(message);
                out.flush();
                input.setText("");
            });

            input.addActionListener(e -> {
                String message = input.getText();
                out.println(message);
                out.flush();
                input.setText("");
            });
            img.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select an image");
                int result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        sendImage(socket, file);
                        textarea.append("Image sent\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        textarea.append("Server: " + msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void sendImage(Socket socket, File file) {
        try {
            byte[] buff = new byte[4096];
            int bytes;
            FileInputStream fin = new FileInputStream(file);
            OutputStream fout = socket.getOutputStream();
            while ((bytes = fin.read(buff)) != -1) {
                fout.write(buff, 0, bytes);
            }
            fout.flush();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
