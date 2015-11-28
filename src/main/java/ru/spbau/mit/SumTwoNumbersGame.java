package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    public static final int MAX_VALUE = 10000;
    private final GameServer server;
    private final Random random = new Random();
    private int a, b;

    public SumTwoNumbersGame(GameServer server) {
        this.server = server;
        startGame();
    }

    synchronized void startGame() {
        a = random.nextInt(MAX_VALUE);
        b = random.nextInt(MAX_VALUE);
        server.broadcast(String.format("%d %d", a, b));
    }

    @Override
    public synchronized void onPlayerConnected(String id) {
        server.sendTo(id, String.format("%d %d", a, b));
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
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
