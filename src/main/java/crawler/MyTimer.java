package crawler;

import javax.swing.*;
import java.util.List;

public class MyTimer extends SwingWorker<Long, Long> {
    private final JLabel elapsedTimeLabel;
    private final long timeLimitSeconds;

    public MyTimer(JLabel elapsedTimeLabel, long timeLimitSeconds) {
        this.elapsedTimeLabel = elapsedTimeLabel;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    @Override
    protected Long doInBackground() {
        long timeAtStart = System.currentTimeMillis();
        long elapsedTime = 0;
        while (elapsedTime <= timeLimitSeconds) {
            elapsedTime = (System.currentTimeMillis() - timeAtStart) / 1000;
            publish(elapsedTime);
        }
        return elapsedTime;
    }

    @Override
    protected void process(List<Long> chunks) {
        long value = chunks.get(0);
        int minutes = 0;
        int seconds;

        if (value > 60) {
            minutes = (int) value / 60;
            seconds = (int) value % 60;
        } else {
            seconds = (int) value;
        }
        String timeElapsed = String.format("%d:%02d", minutes, seconds);
        elapsedTimeLabel.setText(timeElapsed);
    }
}
