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
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
//https://blog.csdn.net/baolong47/article/details/6735853

public class Client {
    private JFrame frame;
    private JList userList;
    private JTextArea textArea;
    private JTextField textField;
    private JTextField txt_port;
    private JTextField txt_hostIP;
    private JTextField txt_name;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JPanel north_panel;
    private JPanel south_panel;
    private JScrollPane right_panel;
    private JScrollPane left_panel;
    private JSplitPane center_split;

    private DefaultListModel listModel;

    private boolean isConnected = false;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;//receive message from server
    private Map<String,User> onLineUsers = new HashMap<>();//All online users

    private Socket socket;

    public Client() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setForeground(Color.BLUE);
        textField = new JTextField();
        txt_port = new JTextField("6666");
        txt_hostIP = new JTextField("localhost");
        txt_name = new JTextField("xiaoqiang");
        btn_start = new JButton("连接");
        btn_stop = new JButton("断开");
        btn_send = new JButton("发送");
        listModel = new DefaultListModel();
        userList = new JList(listModel);

        north_panel = new JPanel();
        north_panel.setLayout(new GridLayout(1, 7));
        north_panel.add(new JLabel("端口"));
        north_panel.add(txt_port);
        north_panel.add(new JLabel("服务器IP"));
        north_panel.add(txt_hostIP);
        north_panel.add(new JLabel("姓名"));
        north_panel.add(txt_name);
        north_panel.add(btn_start);
        north_panel.add(btn_stop);
        north_panel.setBorder(new TitledBorder("连接信息"));

        right_panel = new JScrollPane(textArea);
        right_panel.setBorder(new TitledBorder("消息显示区"));
        left_panel = new JScrollPane(userList);
        left_panel.setBorder(new TitledBorder("在线用户"));
        south_panel = new JPanel(new BorderLayout());
        south_panel.add(textField, "Center");
        south_panel.add(btn_send, "East");
        south_panel.setBorder(new TitledBorder("写消息"));

        center_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel,
                right_panel);
        center_split.setDividerLocation(100);

        frame = new JFrame("客户机");
        // 更改JFrame的图标：
        //frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
        frame.setLayout(new BorderLayout());
        frame.add(north_panel, "North");
        frame.add(center_split,"Center");
        frame.add(south_panel,"South");
        frame.setSize(600,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //Press enter at textField
        textField.addActionListener(l->{
            send();
        });

        //Press btn_send to send msg
        btn_send.addActionListener(l->{
            send();
        });

        btn_start.addActionListener(l->{
            int port = 1;
            if(isConnected){
                JOptionPane.showMessageDialog(frame,"Client already connected");
                return;
            }
                    try {
                        try {
                            port = Integer.parseInt(txt_port.getText().trim());
                        } catch (NumberFormatException e2) {
                            throw new Exception("端口号不符合要求!端口为整数!");
                        }
                        String hostIp = txt_hostIP.getText().trim();
                        String name = txt_name.getText().trim();
                        if (name.equals("") || hostIp.equals("")) {
                            throw new Exception("姓名、服务器IP不能为空!");
                        }
                        boolean flag = connectServer(port, hostIp, name);
                        if (flag == false) {
                            throw new Exception("与服务器连接失败!");
                        }
                        frame.setTitle(name);
                        JOptionPane.showMessageDialog(frame, "成功连接!");
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(frame, exc.getMessage(),
                                "错误", JOptionPane.ERROR_MESSAGE);
                    }

        });

        // 单击断开按钮时事件
        btn_stop.addActionListener(l->{
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    boolean flag = closeConnection();// 断开连接
                    if (flag == false) {
                        throw new Exception("断开连接发生异常！");
                    }
                    JOptionPane.showMessageDialog(frame, "成功断开!");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    closeConnection();// 关闭连接
                }
                System.exit(0);// 退出程序
            }
        });

    }

    public static void main(String[] args) {
        new Client();
    }
    // 执行发送
    public void send() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = textField.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
        textField.setText(null);
    }


    private boolean connectServer(int port, String hostIp, String name) {
        try {
            socket = new Socket(hostIp,port);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Send user info to server
            sendMessage(name+"@"+socket.getLocalAddress().toString());
            //Set up messageThread to receive message, and show all messages on textArea
            messageThread = new MessageThread(reader,textArea);
            messageThread.start();
            isConnected = true;
            return true;
        } catch (IOException e) {
            textArea.append("Failed to connect to ip: "+hostIp+" port: "+port);
            isConnected = false;
            return false;
        }
    }

    public void sendMessage(String msg){
        writer.println(msg);
        writer.flush();
    }
    /*
     * why synchronized ?????
     * */
    //Client close by user
    private synchronized boolean closeConnection() {
        sendMessage("CLOSE");
        messageThread.interrupt();
        try {
            if(reader!=null){
                reader.close();
            }
            if(writer!=null)
                writer.close();
            if(socket!=null)
                socket.close();
            isConnected =false;
            return true;
        } catch (IOException e) {
            isConnected = true;
            return false;
        }
    }
    private class MessageThread extends Thread{
        private BufferedReader reader;
        private JTextArea textArea;

        public MessageThread(BufferedReader reader,JTextArea textArea){
            this.reader = reader;
            this.textArea = textArea;
        }

        //被动关闭
        public synchronized void closeCon(){
            //Empty user list
            listModel.removeAllElements();
            //release all resources
            try {
                if(reader!=null){
                    reader.close();
                }
                if(writer!=null)
                    writer.close();
                if(socket!=null)
                    socket.close();
                isConnected =false;
                this.interrupt();
            } catch (IOException e) {
                isConnected = true;
            }
        }

        @Override
        public void run() {
            String message = "";
            while (true){
                try {
                    message = reader.readLine();
                    StringTokenizer st = new StringTokenizer(message,"@");
                    String command = st.nextToken();//get command from sever
                    if(command.equals("CLOSE")){//Server is closed
                        textArea.append("Server is shutdown\r\n");
                        closeCon();
                        return;// end this thread
                    }else if(command.equals("ADD")){// 新用户上线
                        String username = "";
                        String userIP = "";
                        if((username = st.nextToken())!=null &&
                                (userIP = st.nextToken())!=null){
                            User user = new User(username,userIP);
                            onLineUsers.put(username,user);
                            listModel.addElement(username);
                        }
                    }else if (command.equals("DELETE")) {// 有用户下线更新在线列表
                        String username = st.nextToken();
                        User user = (User) onLineUsers.get(username);
                        onLineUsers.remove(user);
                        listModel.removeElement(username);
                    } else if (command.equals("USERLIST")) {// 加载在线用户列表
                        int size = Integer
                                .parseInt(st.nextToken());
                        String username = null;
                        String userIp = null;
                        for (int i = 0; i < size; i++) {
                            username = st.nextToken();
                            userIp = st.nextToken();
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);
                            listModel.addElement(username);
                        }
                    } else if (command.equals("MAX")) {// 人数已达上限
                        textArea.append(st.nextToken()
                                + st.nextToken() + "\r\n");
                        closeCon();// 被动的关闭连接
                        JOptionPane.showMessageDialog(frame, "服务器缓冲区已满！", "错误",
                                JOptionPane.ERROR_MESSAGE);
                        return;// 结束线程
                    } else {// 普通消息
                        textArea.append(message + "\r\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
