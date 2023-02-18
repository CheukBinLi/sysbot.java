package com.github.cheukbinli.forms;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.application;
import com.github.cheukbinli.core.ns.constant.ScreenState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    volatile boolean pwoerOn = true;

    public MainForm() {
        application a = new application();
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
//                    FileOutputStream out = new FileOutputStream(new File("/Users/cheukbinli/Downloads/11111.jpg"));
//                    out.write(imageByte);
//                    out.close();
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                } catch (InterruptedException ex) {
//                    throw new RuntimeException(ex);
//                } catch (DecoderException ex) {
//                    throw new RuntimeException(ex);
//                }
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
    }
}
