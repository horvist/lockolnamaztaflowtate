package com.loxon.javachallenge.modules2016.gui.view;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectFactory;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;


/**
 * @author kalmarr
 */
public class MapWindow extends JWindow {

	private static final long serialVersionUID = 7159230572798940943L;

	private final int X = 0;
    private final int Y = 0;
    private int cols;
    private int rows;

    private JLabel[][] map;

    private ClassLoader classLoader;

    private static final int sizeOfPicInPixel = 25;

    private static final Map<Integer, ImageIcon> pictureCache = new HashMap<Integer, ImageIcon>();

    public MapWindow(int x, int y) {
        init(x, y);
    }

    private void init(int xSize, int ySize) {
        classLoader = getClass().getClassLoader();
        cols = xSize + 1;
        rows = ySize + 1;
        this.map = new JLabel[rows][cols];

        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new GridLayout(rows, cols));
        setBounds(0, 0, cols * sizeOfPicInPixel, rows * sizeOfPicInPixel);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + (e.getX() - X),
                        getLocation().y + (e.getY() - Y));
            }
        });

        ImageIcon imageIcon = null;
        try {
            File file = new File(classLoader.getResource(Picture.UNKNOWN_FIELD.getPath()).getFile());
            imageIcon = new ImageIcon(ImageIO.read(file));
        } catch (Exception e) {
            // never happen
        }
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                JLabel newLabel = new JLabel();
                newLabel.setIcon(imageIcon);
                map[i][j] = newLabel;
                mapPanel.add(newLabel);
            }
        }
        add(mapPanel);
    }

    public void modifyField(final WsCoordinate coord, final Field field) {
        try {
            map[rows - coord.getY() - 1][coord.getX()].setIcon(getImageIconForField(field));
            revalidate();
        } catch (Exception e) {
            System.out.println("Cannot change image here: x=" + coord.getX() + ", y=" + coord.getY());
        }
    }

    private ImageIcon getImageIconForField(final Field field) throws IOException {
    	final Picture picture = getPictureForField(field);
    	ImageIcon imageIcon = pictureCache.get(picture.ordinal());
    	if (imageIcon == null) {
    		File file = new File(classLoader.getResource(getPictureForField(field).getPath()).getFile());
    		imageIcon = new ImageIcon(ImageIO.read(file));
    		pictureCache.put(picture.ordinal(), imageIcon);
    	}

    	return imageIcon;
    }

    private Picture getPictureForField(final Field field) {
        switch (field.getObjectType()) {
            case TUNNEL:
                if (FieldTeam.ALLY.equals(field.getTeam())) {
                    WsCoordinate exit = Factory.createMap().getShuttleExit();
                    if(exit.getY() == field.getY() && exit.getX() == field.getX()){
                        return Picture.EXIT;
                    }
                    return Picture.TUNNEL;
                }
                return Picture.ENEMY_TUNNEL;

            case SHUTTLE:
                if (FieldTeam.ALLY.equals(field.getTeam())) {
                    return Picture.SHUTTLE;
                }
                return Picture.ENEMY_SHUTTLE;

            case BUILDER_UNIT:
                if (FieldTeam.ALLY.equals(field.getTeam())) {
                    return getUnitPicture(field);
                }
                return Picture.ENEMY_TEAM_MEMBER;

            case ROCK:
                return Picture.CRYSTAL;

            case GRANITE:
                return Picture.GRANITE;

            case OBSIDIAN:
                return Picture.OBSIDIAN;

            default:
                return Picture.UNKNOWN_FIELD;
        }
    }

    public void showWindow() {
        setVisible(true);
    }

    private Picture getUnitPicture(Field field){
        switch (Factory.createMap().getUnitNumByField(field)){
            case 1:
                return Picture.TEAM_MEMBER1;
            case 2:
                return Picture.TEAM_MEMBER2;
            case 3:
                return Picture.TEAM_MEMBER3;
            case 4:
                return Picture.TEAM_MEMBER4;
            default:
                return Picture.TEAM_MEMBER;
        }
    }

    // main method only for testing
    public static void main(String[] args) throws Exception {
        MapWindow mapWindow = new MapWindow(52, 26);
        mapWindow.showWindow();
        WsCoordinate coord = new ObjectFactory().createWsCoordinate();
        for (int i = 0; i < 27; i++) {
            for (int j = 0; j < 53; j++) {
                coord.setX(j);
                coord.setY(i);
                mapWindow.modifyField(coord, getRandomField());
//                Thread.sleep(100L);
            }
        }
//        coord.setX(30);
//        coord.setY(0);
//        mapWindow.modifyField(coord, getRandomField());
    }

    public static Field getRandomField() {
        Field field = new Field(0, 0);
        Random rn = new Random();
        int answer = rn.nextInt(3) + 1;
        if (answer == 1) {
            field.setTeam(FieldTeam.ALLY);
        } else {
            field.setTeam(FieldTeam.ENEMY);
        }
        answer = rn.nextInt(6) + 1;
        switch (answer) {
            case 1:
                field.setObjectType(ObjectType.TUNNEL);
                break;
            case 2:
                field.setObjectType(ObjectType.SHUTTLE);
                break;
            case 3:
                field.setObjectType(ObjectType.BUILDER_UNIT);
                break;
            case 4:
                field.setObjectType(ObjectType.ROCK);
                break;
            case 5:
                field.setObjectType(ObjectType.GRANITE);
                break;
            case 6:
                field.setObjectType(ObjectType.OBSIDIAN);
                break;
        }
        return field;
    }

    private enum Picture {
        UNKNOWN_FIELD("pictures/unknown_field.png"),
        // tunnels
        TUNNEL("pictures/tunnel.png"),
        ENEMY_TUNNEL("pictures/enemy_tunnel.png"),
        //shuttle
        SHUTTLE("pictures/shuttle.png"),
        ENEMY_SHUTTLE("pictures/enemy_shuttle.png"),
        // team mems
        TEAM_MEMBER("pictures/team_mem.png"),
        TEAM_MEMBER1("pictures/team_mem1.png"),
        TEAM_MEMBER2("pictures/team_mem2.png"),
        TEAM_MEMBER3("pictures/team_mem3.png"),
        TEAM_MEMBER4("pictures/team_mem4.png"),
        ENEMY_TEAM_MEMBER("pictures/enemy_team_mem.png"),
        // rock types
        OBSIDIAN("pictures/wall.png"),
        GRANITE("pictures/granite.png"),
        CRYSTAL("pictures/crystal.png"),
        EXIT("pictures/tunnel_exit.png");

        private String picture;

        Picture(String pic) {
            this.picture = pic;
        }

        public String getPath() {
            return picture;
        }
    }
}