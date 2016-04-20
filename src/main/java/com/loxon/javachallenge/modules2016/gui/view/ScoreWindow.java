package com.loxon.javachallenge.modules2016.gui.view;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.CommonResp;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectFactory;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.HardBot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * @author kalmarr
 */
public class ScoreWindow extends JWindow {

    private final int X = 0;

    private final int Y = 0;

    private JLabel penaltyPointLabel;

    private JLabel rewardPointLabel;

    private JLabel totalPointWithoutPenaltyLabel;

    private JLabel totalPointLabel;

    private JLabel bonusLabel;

    private JLabel actionPointLabel;

    private JLabel explosionLeftLabel;

    private JLabel roundsLeftLabel;

    public ScoreWindow() {
        init();
    }

    private void init() {
        setBounds(0, 0, 350, 200);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(8, 2));

        bonusLabel = new JLabel("0", JLabel.CENTER);
        penaltyPointLabel = new JLabel("0", JLabel.CENTER);
        rewardPointLabel = new JLabel("0", JLabel.CENTER);
        totalPointLabel = new JLabel("0", JLabel.CENTER);
        actionPointLabel = new JLabel("0", JLabel.CENTER);
        explosionLeftLabel = new JLabel("0", JLabel.CENTER);
        roundsLeftLabel = new JLabel("0", JLabel.CENTER);
        totalPointWithoutPenaltyLabel = new JLabel("0", JLabel.CENTER);

        resultPanel.add(new JLabel("Total points without penalty:", JLabel.RIGHT));
        resultPanel.add(totalPointWithoutPenaltyLabel);
        resultPanel.add(new JLabel("Total points", JLabel.RIGHT));
        resultPanel.add(totalPointLabel);
        resultPanel.add(new JLabel("Penalty points", JLabel.RIGHT));
        resultPanel.add(penaltyPointLabel);
        resultPanel.add(new JLabel("Reward points", JLabel.RIGHT));
        resultPanel.add(rewardPointLabel);
        resultPanel.add(new JLabel("Total bonusPoints", JLabel.RIGHT));
        resultPanel.add(bonusLabel);
        resultPanel.add(new JLabel("ActionPoints Left", JLabel.RIGHT));
        resultPanel.add(actionPointLabel);
        resultPanel.add(new JLabel("Exploxions Left", JLabel.RIGHT));
        resultPanel.add(explosionLeftLabel);
        resultPanel.add(new JLabel("Rounds Left", JLabel.RIGHT));
        resultPanel.add(roundsLeftLabel);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
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

    public void refreshScore(WsScore score, int actionPointLeft, int explLeft, int roundsLeft) {
        totalPointWithoutPenaltyLabel.setText(String.valueOf(score.getTotal()-score.getPenalty()));
        bonusLabel.setText(String.valueOf(score.getBonus()));
        penaltyPointLabel.setText(String.valueOf(score.getPenalty()));
        rewardPointLabel.setText(String.valueOf(score.getReward()));
        totalPointLabel.setText(String.valueOf(score.getTotal()));
        actionPointLabel.setText(String.valueOf(actionPointLeft));
        explosionLeftLabel.setText(String.valueOf(explLeft));
        roundsLeftLabel.setText(String.valueOf(roundsLeft));
        revalidate();
    }

    // main method only for testing
    public static void main(String[] args) throws Exception {
        ScoreWindow sc = new ScoreWindow();
        sc.showWindow();

        HardBot hardBot = new HardBot(args[1], args[2], args[0]);

        while (true) {
            CommonResp commonResp = hardBot.isMyTurnTest();
            sc.refreshScore(commonResp.getScore(), commonResp.getActionPointsLeft(), commonResp.getExplosivesLeft(), commonResp.getTurnsLeft());
            if (commonResp.getTurnsLeft() == 0) {
                System.exit(0);
            }
            Thread.sleep(151L);
        }
    }

}
