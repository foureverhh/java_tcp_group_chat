package group_chat_gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
        left_panel.setBorder(new TitledBorder("Users:"));

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

        //Handle close frame
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(isStart){
                    //shutdown server
                    closeServer();
                }
                //exit program
                System.exit(0);
            }
        });

        //Press enter in txt_message to send message to client
        txt_message.addActionListener(l->{
            send();
        });

        //Press btn_send to send message to client
        btn_send.addActionListener(l->{
            send();
        });

        //Press btn_start to start server
        btn_start.addActionListener(l->{
            if(isStart){
                JOptionPane.showMessageDialog(frame,"Server is already started",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int max=1, port=1;
            try {
                max = Integer.parseInt(txt_max.getText());
                if(max<=0){
                    throw new Exception();
                }
            }catch (Exception e){
                JOptionPane.showMessageDialog(frame,"Limits must be a positive integer larger than 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            try{
                port = Integer.parseInt(txt_port.getText());
                if(port<=0){
                    throw new Exception();
                }
            }catch (Exception e){
                JOptionPane.showMessageDialog(frame,"Port must be a positive integer larger than 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            //start server
            serverStart(max,port);
            contentArea.append("Server started at port: "+port+" with max limit as "+max);
            JOptionPane.showMessageDialog(frame,"Server started");
            txt_max.setEnabled(false);
            txt_port.setEnabled(false);
            btn_stop.setEnabled(true);
        });

        //Press btn_stop to stop server
        btn_stop.addActionListener(l->{
            if(!isStart){
                JOptionPane.showMessageDialog(frame,"Server is not on, no need to shut it down",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            closeServer();
            btn_start.setEnabled(true);
            txt_max.setEnabled(true);
            txt_port.setEnabled(true);
            btn_stop.setEnabled(false);
            contentArea.append("Server shutdown");
            JOptionPane.showMessageDialog(frame,"Server shutdown");
        });
    }


    private void serverStart(int max,int port){
        clients = new ArrayList<>();
        try {
            //Set up ServerSocket
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket,max);
            //Start server thread
            serverThread.start();
            isStart = true;
        } catch (IOException e) {
            isStart = false;
            JOptionPane.showMessageDialog(frame,"Server start failed");
        }
    }

    private void closeServer() {
        if(serverThread!=null){
            //Stop server thread
            serverThread.interrupt();
        }
        for(int i=clients.size()-1; i>=0;i--){
            /*
            *
            * getWriter()?????
            *
            *
            * */
            //Send server CLOSE command to all clients
            clients.get(i).getWriter().println("CLOSE");
            clients.get(i).getWriter().flush();
            //Release resource
            clients.get(i).interrupt();
            try {
                clients.get(i).getReader().close();
                clients.get(i).getWriter().close();
                clients.get(i).getSocket().close();
                clients.remove(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(serverSocket!=null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        listModel.removeAllElements();
        isStart = false;

    }

    //Server send method
    public void send(){
        if(!isStart){
            JOptionPane.showMessageDialog(frame,"Server not started");
            return;
        }
        if(clients.size()==0){
            JOptionPane.showMessageDialog(frame,"no other users online");
        }
        String message = txt_message.getText().trim();
        if(message==null||message.equals("")){
            JOptionPane.showMessageDialog(frame,"message can not be empty");
        }
        sendServerMessage(message);
        contentArea.append("Server send message: "+message);
        txt_message.setText(null);
    }

    //Send system message
    public void sendServerMessage(String message){
        for (int i = clients.size()-1; i >=0 ; i--) {
            clients.get(i).getWriter().println("Server system message: "+message);
            clients.get(i).getWriter().flush();
        }
    }

    private class ServerThread extends Thread{
        //Server thread is to start ServerSocket
        //listen to connection request
        private ServerSocket serverSocket;
        private int max;

        public ServerThread(ServerSocket serverSocket, int max) {
            this.serverSocket = serverSocket;
            this.max = max;
        }

        @Override
        public void run() {
            //Keep on receive client connection request
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    if(clients.size()==max){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream());
                        //Receive client user profile info
                        String info = reader.readLine();
                        StringTokenizer st = new StringTokenizer(info,"@");
                        User user = new User(st.nextToken(),st.nextToken());
                        //confirm connection built up
                        writer.println("MAX@server: sorry, "+user.getName()+" from "+user.getIp()+" , too many users, connect later please");
                        writer.flush();
                        //release resource
                        reader.close();
                        writer.close();
                        socket.close();
                        continue;
                    }
                    ClientThread client = new ClientThread(socket);
                    client.start();
                    clients.add(client);
                    listModel.addElement(client.getUser().getName());//Update user list
                    contentArea.append(client.getUser().getName()+client.getUser().getIp()+" is online\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientThread extends Thread{
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private User user;

        public ClientThread(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                //Get user info from client side
                String userInfo = reader.readLine();
                StringTokenizer st = new StringTokenizer(userInfo,"@");
                user = new User(st.nextToken(),st.nextToken());
                //Response connection successful to client
                writer.println(user.getName()+" from "+user.getIp()+ " succeed to connect");
                writer.flush();
                //List all users to the client
                if(clients.size()>0){
                    String temp = "";
                    for (int i = 0; i < clients.size() ; i++) {
                        temp += clients.get(i).getUser().getName()+"/"+clients.get(i).getUser().getIp()+
                                "@";
                    }
                    writer.println("USERLIST@"+clients.size()+"@"+temp);
                    writer.flush();
                }
                //Inform all other users, this user is online
                for(ClientThread client : clients){
                    if(client!= this){
                        client.getWriter().println("ADD@"+this.user.getName()+" "+this.user.getIp());
                        client.getWriter().flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message = null;
            while (true){
                try {
                    message = reader.readLine();
                    if(message.equals("CLOSE")){//client would offline
                        contentArea.append(this.getUser().getName()+" "+this.getUser().getIp()+" is offline!\r\n");
                        reader.close();
                        writer.close();
                        socket.close();
                    //send offline info to all other users
                    for(ClientThread client : clients){
                        if(client!=this){
                            client.getWriter().println("DELETE@"+this.user.getName()+" "+this.user.getIp());
                            client.getWriter().flush();
                        }
                    }
                    listModel.removeElement(this.user.getName());//update the name list
                    //stop and remove this clientThread
                    clients.remove(this);
                    socket.close();
                    this.interrupt();
             /*       for (int i = clients.size() - 1; i >= 0; i--) {
                        if (clients.get(i).getUser() == user) {
                            ClientThread temp = clients.get(i);
                            clients.remove(i);// 删除此用户的服务线程
                            temp.stop();// 停止这条服务线程
                            return;
                        }
                    }*/
                    }else {
                        dispatcherMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void dispatcherMessage(String message) {
            StringTokenizer st = new StringTokenizer(message,"@");
            String source = st.nextToken();
            String owner = st.nextToken();
            String content = st.nextToken();
            message = source+" says: "+content;
            contentArea.append(message+"\r\n");
            if(owner.equals("ALL")){
                for(ClientThread client : clients){
                    client.getWriter().println(message+" (group info)");
                    client.getWriter().flush();
                }
            }
        }

        public Socket getSocket() {
            return socket;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public User getUser() {
            return user;
        }
    }

    //main methods
    public static void main(String[] args) {
        new Server();
    }
}
