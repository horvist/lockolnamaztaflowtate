package com.loxon.javachallenge.modules2016.gui.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author kalmarr
 */
public class MapWindow extends JWindow {

    private int X = 0;
    private int Y = 0;

    private JPanel[][] map;

    private static final int sizeOfPicInPixel = 35;

    public MapWindow(int x, int y) {
        init(x, y);
    }

    private void init(int x, int y) {
        this.map = new JPanel[x + 1][y + 1];

        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new GridLayout(x, y));
        setBounds(0, 0, x * sizeOfPicInPixel, y * sizeOfPicInPixel);
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + (e.getX() - X),
                        getLocation().y + (e.getY() - Y));
            }
        });

        for(int i = 0; i < x * y; i++){
            try {
                mapPanel.add(createPanel());
            } catch (Exception e){
                mapPanel.add(new JPanel());
            }
        }

        //TODO fill map

        add(mapPanel);
    }

    public JLabel createPanel() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(Pictures.TUNNEL.getPath()).getFile());
        BufferedImage myPicture = ImageIO.read(file);
        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
        return picLabel;
    }

    public void showWindow(){
        setVisible(true);
    }

    public static void main(String[] args) {
        MapWindow mapWindow = new MapWindow(20,20);
        mapWindow.showWindow();
    }

    private enum Pictures {
        UNKNOWN_FIELD("unknown_field.png"),
        TUNNEL("tunnel.png");

        private String picture;

        private Pictures(String pic){
            this.picture = pic;
        }

        public String getPath(){
            return picture;
        }
    }
}
