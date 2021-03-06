package Misc.Gui.Main;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JMenuBar;
import Misc.Gui.Controller.Control;
import java.awt.Dimension;
import javax.swing.JFrame;

public class OuterFrame extends JFrame
{
    static final long serialVersionUID = 1L;
    public MainPanel mainPanel;
    public Control c;
    public JMenuBar menu;
    
    public OuterFrame(final Control controller) {
        this.setTitle("Heart failure detector");
        this.setDefaultCloseOperation(3);
        this.c = controller;
        final Color blue = new Color(0.75f, 0.85f, 1.0f);
        this.getContentPane().setBackground(blue);
        (this.menu = new JMenuBar()).setBackground(blue);
        this.setLayout(new BorderLayout(4, 4));
        this.add(this.menu, "North");
    }
}
