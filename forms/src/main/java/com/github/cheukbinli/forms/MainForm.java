package com.github.cheukbinli.forms;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.application;
import com.github.cheukbinli.core.im.dodo.DodoApi;
import com.github.cheukbinli.core.im.dodo.model.Authorization;
import com.github.cheukbinli.core.im.dodo.model.dto.request.MessageBodyTextRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.request.SetChannelMessageSendRequest;
import com.github.cheukbinli.core.ns.constant.ScreenState;
import org.apache.commons.codec.binary.Hex;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class MainForm extends JFrame {
    public JPanel panel1;
    private JButton 启动Button;
    private JButton 队列信息Button;
    private JButton 截图Button;
    private JButton 重启PGButton;
    private JTabbedPane tabbedPane1;
    private JTable table1;
    private JTextField a1111TextField;
    private JTextField a127001TextField;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField a1921681225TextField;
    private JTextField a6000TextField;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JTabbedPane tabbedPane2;
    private JTextArea textArea1;
    private JTextArea textArea2;
    public JPanel picturePane;
    private JTextPane textPane1;
    private JButton button1;
    private JTextArea noticeMessagetextArea3;
    private JButton 发送Button;
    private JButton 清空Button;
    private JTextField noticeIslandSourceIdTextField;
    private JTextField noticeChannelIDTextField;
    private JTextField dodoApiPathTextField;
    private JTextField noticeClientIdTextField2;
    private JTextField noticeTokenTextField1;
    private JButton 连接Button;
    private JButton 连接Button1001;
    private JTextArea textArea1001;
    private JButton 发送Button1001;
    private JButton 清空Button1001;
    private JButton 上班Button1001;
    private JButton 下班Button1001;

    volatile boolean pwoerOn = true;

    private application a = null;
    private Image image = null;
    private volatile boolean screenshot = false;
    private DodoApi noticeRobot;

    @Override
    public void paintComponents(Graphics g) {
        rePrint(g);
        super.paintComponents(g);
    }

    public void rePrint(Graphics g) {
        Thread repring = new Thread(new Runnable() {
            @Override
            public void run() {
                Point point = textPane1.getLocation();
                while (true) {
                    if (null == image) {
                        synchronized (this) {
                            try {
                                wait(1000);
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    textPane1.getGraphics().drawImage(image, point.x, point.y, textPane1.getWidth(), textPane1.getHeight(), null);
                    image = null;
                }
            }
        });
        repring.start();
    }

    public MainForm() {
        a = new application();
        ExecutorService executorService = Executors.newCachedThreadPool();
        启动Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                启动Button.setEnabled(false);
                textArea1.setAutoscrolls(true);
                textArea2.setAutoscrolls(true);
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        a.start();
                    }
                });
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                if (textArea1.getRows() > 100) {
                                    textArea1.removeAll();
                                }
                                String msg = GlobalLogger.pullMsgLog();
                                textArea1.append(msg);
                                textArea1.setCaretPosition(textArea1.getDocument().getLength());
                            }
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Function<Throwable, String> function = new Function<Throwable, String>() {
                                @Override
                                public String apply(Throwable throwable) {
                                    if (null != throwable) {
                                        return throwable.getMessage();
                                    }
                                    return null;
                                }
                            };
                            while (true) {
                                if (textArea2.getRows() > 100) {
                                    textArea2.removeAll();
                                }
                                textArea2.append(GlobalLogger.pullExceptionLog(function));
                            }
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        });

        截图Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                try {
//                    byte[] imageByte = a.getSwitchSysbotAggregateService().getSwitchCommandApi().pixelPeek();
//                    textPane1.getLocation();
//                    image = ImageIO.read(new ByteArrayInputStream(Hex.encodeHexString(imageByte).getBytes()));
//                } catch (Exception ex) {
//                    throw new RuntimeException(ex);
//                }
                try {
                    byte[] imageByte = a.getSwitchSysbotAggregateService().getSwitchCommandApi().pixelPeek();
                    image = ImageIO.read(new ByteArrayInputStream(Hex.decodeHex(new String(imageByte))));
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    a.getSwitchSysbotAggregateService().getSwitchCommandApi().SetScreen(pwoerOn ? ScreenState.Off : ScreenState.On);
                    pwoerOn = !pwoerOn;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        重启PGButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    a.getSwitchSysbotAggregateService().getSwitchService().restrart();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        连接Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noticeRobot = new DodoApi(
                        dodoApiPathTextField.getText(),
                        new Authorization(
                                noticeIslandSourceIdTextField.getText(),
                                noticeClientIdTextField2.getText(),
                                noticeTokenTextField1.getText()));
                noticeRobot.init();
                连接Button.setEnabled(false);
            }
        });
        发送Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (null == noticeRobot) {
                    GlobalLogger.append("公告机器人末初始化");
                    return;
                }
                try {
                    noticeRobot.SetChannelMessageSend(
                            new SetChannelMessageSendRequest()
                                    .setChannelId(noticeChannelIDTextField.getText())
                                    .setMessageBody(new MessageBodyTextRequest().setContent(String.format("<@online>%s", noticeMessagetextArea3.getText())))
                    );
                    发送Button.setText("");
                } catch (IOException ex) {
                    GlobalLogger.append(ex);
                    ex.printStackTrace();
                }
            }
        });
    }
}
