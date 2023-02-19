package com.github.cheukbinli;

import com.github.cheukbinli.forms.MainForm;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");

        MainForm j = new MainForm();
        j.setTitle("PKM-BOT");
        j.setVisible(true);
        j.setSize(800, 640);
        j.setContentPane(j.panel1);
        j.paintComponents(j.getGraphics());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}