package com.loxon.javachallenge.modules2016.gui.view;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectFactory;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * @author kalmarr
 */
public class ScoreWindow extends JWindow {

    private int X = 0;

    private int Y = 0;

    private JLabel peneltyPointLabel;

    private JLabel rewardPointLabel;

    private JLabel totalPointLabel;

    private JLabel bonusLabel;

    public ScoreWindow() {
        init();
    }

    private void init() {
        setBounds(0, 0, 250, 150);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(4, 2));

        bonusLabel = new JLabel("0", JLabel.CENTER);
        peneltyPointLabel = new JLabel("0", JLabel.CENTER);
        rewardPointLabel = new JLabel("0", JLabel.CENTER);
        totalPointLabel = new JLabel("0", JLabel.CENTER);

        resultPanel.add(new JLabel("Total points", JLabel.RIGHT));
        resultPanel.add(totalPointLabel);
        resultPanel.add(new JLabel("Penelty points", JLabel.RIGHT));
        resultPanel.add(peneltyPointLabel);
        resultPanel.add(new JLabel("Reward points", JLabel.RIGHT));
        resultPanel.add(rewardPointLabel);
        resultPanel.add(new JLabel("Total bonusPoints", JLabel.RIGHT));
        resultPanel.add(bonusLabel);

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + (e.getX() - X),
                        getLocation().y + (e.getY() - Y));
            }
        });

        add(resultPanel);
    }

    public void showWindow() {
        setVisible(true);
    }

    public void refreshScore(WsScore score) {
        bonusLabel.setText(String.valueOf(score.getBonus()));
        peneltyPointLabel.setText(String.valueOf(score.getPenalty()));
        rewardPointLabel.setText(String.valueOf(score.getReward()));
        totalPointLabel.setText(String.valueOf(score.getTotal()));
        revalidate();
    }

    // main method only for testing
    public static void main(String[] args) throws Exception {
        ScoreWindow sc = new ScoreWindow();
        sc.showWindow();
        ObjectFactory objectFactory = new ObjectFactory();
        int i = 0;
        while (i < 100) {
            WsScore score = objectFactory.createWsScore();
            score.setTotal(i++);
            score.setPenalty(i++);
            score.setBonus(i++);
            score.setReward(i++);
            sc.refreshScore(score);
            Thread.sleep(1000L);
        }
    }
}
