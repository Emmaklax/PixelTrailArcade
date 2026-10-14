package com.game.gfx;

import java.awt.Canvas;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.game.main.Game;

public class Windows {

    private static final String NAME = "Super Mario Bros";
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 720;

    private final JFrame frame;

    public Windows(Canvas canvas) {
        if (canvas == null) {
            throw new IllegalArgumentException("El canvas no puede ser null");
        }

        frame = new JFrame(NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (canvas instanceof Game game) {
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent event) { game.shutdown(); }
            });
        }
        frame.setResizable(false);

        canvas.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        canvas.setFocusable(true);

        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> canvas.requestFocusInWindow());
    }

}
