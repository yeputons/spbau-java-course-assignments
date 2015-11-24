package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    final public static int MAX_VALUE = 10000;
    final private GameServer server;
    final private Random random = new Random();
    private volatile int a, b;

    public SumTwoNumbersGame(GameServer server) {
        this.server = server;
        startGame();
    }

    void startGame() {
        synchronized (this) {
            a = random.nextInt(MAX_VALUE);
            b = random.nextInt(MAX_VALUE);
            server.broadcast(String.format("%d %d", a, b));
        }
    }

    @Override
    public void onPlayerConnected(String id) {
        synchronized (this) {
            server.sendTo(id, String.format("%d %d", a, b));
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            int result = Integer.parseInt(msg);
            if (result == a + b) {
                server.sendTo(id, "Right");
                server.broadcast(id + " won");
                startGame();
            } else {
                server.sendTo(id, "Wrong");
            }
        }
    }
}
