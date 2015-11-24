package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * GameServerImpl assumes that Connection is thread-safe
 */
public class GameServerImpl implements GameServer {
    final private Game game;
    final private AtomicInteger lastId = new AtomicInteger();
    final private ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<Game> gameClass = (Class<Game>) ClassLoader.getSystemClassLoader().loadClass(gameClassName);
        game = (Game)gameClass.getConstructor(GameServer.class).newInstance(this);
        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            String key = (String) prop.getKey();
            String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            String value = (String) prop.getValue();
            try {
                int valueInt = Integer.parseInt(value);
                gameClass.getMethod(setterName, int.class).invoke(game, valueInt);
            } catch (NumberFormatException e) {
                gameClass.getMethod(setterName, String.class).invoke(game, value);
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        final String id = Integer.toString(lastId.getAndIncrement());
        connections.put(id, connection);
        connection.send(id);
        game.onPlayerConnected(id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!connection.isClosed()) {
                    String msg;
                    try {
                        msg = connection.receive(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (msg == null) {
                        Thread.yield();
                        continue;
                    }
                    game.onPlayerSentMsg(id, msg);
                }
            }
        }).start();
    }

    @Override
    public void broadcast(final String message) {
        connections.forEach(new BiConsumer<String, Connection>() {
            @Override
            public void accept(String id, Connection connection) {
                connection.send(message);
            }
        });
    }

    @Override
    public void sendTo(String id, String message) {
        connections.get(id).send(message);
    }
}
