package ru.spbau.mit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


public class QuizGame implements Game {
    private static class Dictionary {
        private static class Entry {
            public final String question, answer;

            private Entry(String question, String answer) {
                this.question = question;
                this.answer = answer;
            }
        }

        final private Deque<Entry> dictionary = new ArrayDeque<>();

        public void reloadFrom(String dictionaryFilename) throws FileNotFoundException {
            try (Scanner s = new Scanner(new FileReader(dictionaryFilename))) {
                Deque<Entry> newDictionary = new ArrayDeque<>();
                for (; ; ) {
                    String line;
                    try {
                        line = s.nextLine();
                    } catch (NoSuchElementException e) {
                        break;
                    }
                    String[] data = line.split(";");
                    String answer = data[1];
                    String question = String.format("%s (%d letters)", data[0], data[1].length());
                    newDictionary.add(new Entry(question, answer));
                }
                synchronized (dictionary) {
                    dictionary.clear();
                    dictionary.addAll(newDictionary);
                }
            }
        }

        public Entry nextEntry() {
            synchronized (dictionary) {
                Entry result = dictionary.getFirst();
                dictionary.removeFirst();
                dictionary.addLast(result);
                return result;
            }
        }
    }

    private static class RoundHandler {
        final private QuizGame game;
        final private Dictionary.Entry currentEntry;
        final private int delayUntilNextLetter;
        final private int maxLettersToOpen;
        final private Timer timer = new Timer();
        int openedLetters = 0;
        private boolean stopped = false;

        private RoundHandler(QuizGame game, Dictionary.Entry currentEntry, int delayUntilNextLetter, int maxLettersToOpen) {
            if (delayUntilNextLetter <= 0 || maxLettersToOpen < 0) {
                throw new IllegalArgumentException();
            }

            this.game = game;
            this.currentEntry = currentEntry;
            this.delayUntilNextLetter = delayUntilNextLetter;
            this.maxLettersToOpen = maxLettersToOpen;
            this.game.server.broadcast("New round started: " + currentEntry.question);
            scheduleOpenNextLetter();
        }

        private void scheduleOpenNextLetter() {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (RoundHandler.this) {
                        if (stopped) {
                            return;
                        }
                        openedLetters++;
                        if (openedLetters <= maxLettersToOpen) {
                            game.server.broadcast("Current prefix is " + currentEntry.answer.substring(0, openedLetters));
                            scheduleOpenNextLetter();
                        } else {
                            game.server.broadcast("Nobody guessed, the word was " + currentEntry.answer);
                            stop();
                            game.restartRound();
                        }
                    }
                }
            }, delayUntilNextLetter);
        }

        public void makeGuess(String id, String msg) {
            synchronized (this) {
                if (stopped) {
                    return;
                }
                if (msg.equals(currentEntry.answer)) {
                    game.server.broadcast("The winner is " + id);
                    stop();
                    game.restartRound();
                } else {
                    game.server.sendTo(id, "Wrong try");
                }
            }
        }

        public void stop() {
            synchronized (this) {
                if (!stopped) {
                    stopped = true;
                    timer.cancel();
                }
            }
        }

        public void onPlayerConnected(String id) {
            synchronized (this) {
                if (!stopped) {
                    game.server.sendTo(id, "New round started: " + currentEntry.question);
                }
            }
        }
    }

    private Dictionary dictionary = new Dictionary();
    private int delayUntilNextLetter = 0;
    private int maxLettersToOpen = 0;

    final private GameServer server;
    private RoundHandler roundHandler = null;

    public QuizGame(GameServer server) {
        this.server = server;
    }

    public void setDelayUntilNextLetter(int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) throws FileNotFoundException {
        dictionary.reloadFrom(dictionaryFilename);
    }

    @Override
    public void onPlayerConnected(String id) {
        synchronized (this) {
            if (roundHandler != null) {
                roundHandler.onPlayerConnected(id);
            }
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            if (msg == "!start") {
                if (roundHandler == null) {
                    roundHandler = new RoundHandler(this, dictionary.nextEntry(), delayUntilNextLetter, maxLettersToOpen);
                }
            } else if (msg == "!stop") {
                if (roundHandler != null) {
                    roundHandler.stop();
                    roundHandler = null;
                    server.broadcast("Game has been stopped by " + id);
                }
            } else {
                if (roundHandler != null) {
                    roundHandler.makeGuess(id, msg);
                }
            }
        }
    }

    private void restartRound() {
        synchronized (this) {
            assert roundHandler != null;
            roundHandler = new RoundHandler(this, dictionary.nextEntry(), delayUntilNextLetter, maxLettersToOpen);
        }
    }
}
