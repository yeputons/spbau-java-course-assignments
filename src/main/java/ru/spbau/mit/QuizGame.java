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

        private final Deque<Entry> dictionary = new ArrayDeque<>();

        public void reloadFrom(String dictionaryFilename) throws FileNotFoundException {
            try (Scanner s = new Scanner(new FileReader(dictionaryFilename))) {
                Deque<Entry> newDictionary = new ArrayDeque<>();
                while (s.hasNextLine()) {
                    String[] data = s.nextLine().split(";");
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
        // I want static class and explicit field instead of implicit access because
        // there are some final duplicated fields in RoundHandler which should not be
        // mixed with what's defined in QuizGame
        private final QuizGame game;
        private final Dictionary.Entry currentEntry;
        private final int delayUntilNextLetter;
        private final int maxLettersToOpen;
        private final Timer timer = new Timer();
        private int openedLetters = 0;
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

        public synchronized void onPlayerConnected(String id) {
            if (!stopped) {
                game.server.sendTo(id, "New round started: " + currentEntry.question);
            }
        }

        private void scheduleOpenNextLetter() {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    openNextLetter();
                }
            }, delayUntilNextLetter);
        }

        private synchronized void openNextLetter() {
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

        public synchronized void makeGuess(String id, String msg) {
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

        public synchronized void stop() {
            if (!stopped) {
                stopped = true;
                timer.cancel();
            }
        }
    }

    private final Dictionary dictionary = new Dictionary();
    private final GameServer server;

    private int delayUntilNextLetter = 0;
    private int maxLettersToOpen = 0;

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
    public synchronized void onPlayerConnected(String id) {
        if (roundHandler != null) {
            roundHandler.onPlayerConnected(id);
        }
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
        switch (msg) {
            case "!start":
                if (roundHandler == null) {
                    roundHandler = new RoundHandler(this, dictionary.nextEntry(), delayUntilNextLetter, maxLettersToOpen);
                }
                break;
            case "!stop":
                if (roundHandler != null) {
                    roundHandler.stop();
                    roundHandler = null;
                    server.broadcast("Game has been stopped by " + id);
                }
                break;
            default:
                if (roundHandler != null) {
                    roundHandler.makeGuess(id, msg);
                }
                break;
        }
    }

    private synchronized void restartRound() {
        assert roundHandler != null;
        roundHandler = new RoundHandler(this, dictionary.nextEntry(), delayUntilNextLetter, maxLettersToOpen);
    }
}
