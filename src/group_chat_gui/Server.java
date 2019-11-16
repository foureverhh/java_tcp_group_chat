package group_chat_gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.ServerSocket;
import java.util.List;

public class Server {
    private JFrame frame;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JTextField txt_max;
    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JPanel north_panel;
    private JPanel south_panel;
    private JScrollPane right_panel;
    private JScrollPane left_panel;
    private JSplitPane center_split;
    private JList userList;
    private DefaultListModel listModel;

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private List<ClientThread> clients;

    private boolean isStart = false;

    public Server() {
        frame = new JFrame("Server");
        //ImageIcon icon = new ImageIcon("/Users/foureverhh/gruppArbete/resources/chat.png");
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/chat.png")));
        //frame.setIconImage(icon.getImage());
        contentArea = new JTextArea();
        contentArea.setEnabled(false);
        contentArea.setForeground(Color.BLUE);
        txt_message = new JTextField();
        txt_max = new JTextField("30");
        txt_port  = new JTextField("6666");
        btn_start = new JButton("Start");
        btn_send = new JButton("Send");
        btn_stop = new JButton("Stop");
        btn_stop.setEnabled(false);
        listModel = new DefaultListModel();
        userList = new JList(listModel);

        south_panel = new JPanel(new BorderLayout());
        south_panel.setBorder(new TitledBorder("Write:"));
        south_panel.add(txt_message,"Center");
        south_panel.add(btn_send,"East");

        left_panel = new JScrollPane(userList);
        left_panel.setBorder(new TitledBorder("Users online:"));

        right_panel = new JScrollPane(contentArea);
        right_panel.setBorder(new TitledBorder("Receives:"));

        center_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left_panel,right_panel);
        center_split.setDividerLocation(100);
        //center_split.setDividerSize(10);

        north_panel = new JPanel();
        north_panel.setLayout(new GridLayout(1,6));
        north_panel.add(new JLabel("Max Limits:"));
        north_panel.add(txt_max);
        north_panel.add(new Label("Port:"));
        north_panel.add(txt_port);
        north_panel.add(btn_start);
        north_panel.add(btn_stop);
        north_panel.setBorder(new TitledBorder("Configuration:"));

        frame.setLayout(new BorderLayout());
        frame.add(north_panel,"North");
        frame.add(center_split,"Center");
        frame.add(south_panel,"South");
        frame.setSize(600,400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
/*

    //Server send method
    public void send(){
        if(!isStart){
            JOptionPane.showMessageDialog(frame,);
        }
    }
*/






    private class ServerThread {
    }

    private class ClientThread {
    }

    //main methods
    public static void main(String[] args) {
        new Server();
    }
}
