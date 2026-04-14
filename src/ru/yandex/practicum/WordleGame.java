package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordleGame {

    private static final int MAX_STEPS = 6;
    private static final int WORD_LENGTH = 5;

    private final String answer;
    private int stepsLeft;
    private final WordleDictionary dictionary;
    private final List<String> guesses;
    private final List<String> hints;
    private final Set<String> usedHints;
    private final PrintWriter log;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.stepsLeft = MAX_STEPS;
        this.guesses = new ArrayList<>();
        this.hints = new ArrayList<>();
        this.usedHints = new HashSet<>();

        log.println("Начата новая игра. Загаданное слово: " + answer);
        log.flush();
    }

    public String makeGuess(String word)
            throws InvalidWordException, WordNotFoundInDictionaryException {
        String normalized = validateAndNormalize(word);

        if (!dictionary.contains(normalized)) {
            throw new WordNotFoundInDictionaryException(normalized);
        }

        stepsLeft--;

        String hint = WordleDictionary.matchHint(normalized, answer);
        guesses.add(normalized);
        hints.add(hint);

        StringBuilder logMsg = new StringBuilder();
        logMsg.append("Ход ").append(MAX_STEPS - stepsLeft)
                .append(": слово='").append(normalized)
                .append("', подсказка='").append(hint)
                .append("', осталось шагов=").append(stepsLeft);
        log.println(logMsg.toString());
        log.flush();

        return hint;
    }

    public String getSuggestedWord() {
        List<String> candidates = dictionary.filterCandidates(guesses, hints);
        candidates.removeAll(usedHints);
        candidates.removeAll(guesses);

        if (candidates.isEmpty()) {
            log.println("Подсказок нет: нет подходящих слов");
            log.flush();
            return null;
        }

        String suggestion = candidates.get(new Random().nextInt(candidates.size()));
        usedHints.add(suggestion);

        log.println("Выдана подсказка: '" + suggestion + "', кандидатов было: " + candidates.size());
        log.flush();

        return suggestion;
    }

    public boolean isWon() {
        return !guesses.isEmpty() && guesses.get(guesses.size() - 1).equals(answer);
    }

    public boolean isOver() {
        return isWon() || stepsLeft <= 0;
    }

    public String getAnswer() {
        return answer;
    }

    public int getStepsLeft() {
        return stepsLeft;
    }

    public List<String> getGuesses() {
        return Collections.unmodifiableList(guesses);
    }

    public List<String> getHints() {
        return Collections.unmodifiableList(hints);
    }

    private String validateAndNormalize(String word) throws InvalidWordException {
        if (word == null || word.isBlank()) {
            throw new InvalidWordException("Слово не может быть пустым");
        }

        String normalized = WordleDictionary.normalize(word.trim());

        if (normalized.length() != WORD_LENGTH) {
            throw new InvalidWordException(
                    "Слово должно состоять из " + WORD_LENGTH + " букв, введено: " + normalized.length());
        }

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c < 'а' || c > 'я') {
                throw new InvalidWordException(
                        "Слово должно содержать только русские буквы, найден символ: '" + c + "'");
            }
        }

        return normalized;
    }
}
