package com.mycompany.studentdbmanager;

import gui.StudentFrame;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new StudentFrame();
        });
    }
}