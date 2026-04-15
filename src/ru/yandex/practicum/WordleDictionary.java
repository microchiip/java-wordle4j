package ru.yandex.practicum;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class WordleDictionary {

    private final List<String> words;

    public WordleDictionary(List<String> words) {
        if (words == null || words.isEmpty()) {
            throw new RuntimeException("Нельзя создать словарь из пустого списка слов");
        }
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public int size() {
        return words.size();
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new RuntimeException("Словарь пуст, невозможно выбрать случайное слово");
        }
        return words.get(new Random().nextInt(words.size()));
    }

    public static String normalize(String word) {
        return word.toLowerCase().replace('ё', 'е');
    }

    public static String matchHint(String guess, String answer) {
        if (guess.length() != answer.length()) {
            throw new RuntimeException(
                    "Длина слов не совпадает: '" + guess + "' vs '" + answer + "'");
        }

        int len = guess.length();
        char[] result = new char[len];
        boolean[] answerUsed = new boolean[len];
        boolean[] guessMatched = new boolean[len];

        for (int i = 0; i < len; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                answerUsed[i] = true;
                guessMatched[i] = true;
            }
        }

        for (int i = 0; i < len; i++) {
            if (guessMatched[i]) {
                continue;
            }
            char c = guess.charAt(i);
            boolean found = false;
            for (int j = 0; j < len; j++) {
                if (!answerUsed[j] && answer.charAt(j) == c) {
                    found = true;
                    answerUsed[j] = true;
                    break;
                }
            }
            result[i] = found ? '^' : '-';
        }

        return new String(result);
    }

    public List<String> filterCandidates(List<String> guesses, List<String> hints) {
        if (guesses.isEmpty()) {
            return new ArrayList<>(words);
        }

        Set<Character> absentLetters = new HashSet<>();
        Map<Integer, Character> correctPositions = new LinkedHashMap<>();
        Map<Character, Set<Integer>> wrongPositions = new LinkedHashMap<>();
        Set<Character> presentLetters = new HashSet<>();

        for (int g = 0; g < guesses.size(); g++) {
            String guess = guesses.get(g);
            String hint = hints.get(g);

            for (int i = 0; i < guess.length(); i++) {
                char c = guess.charAt(i);
                char h = hint.charAt(i);

                if (h == '+') {
                    correctPositions.put(i, c);
                    presentLetters.add(c);
                } else if (h == '^') {
                    presentLetters.add(c);
                    wrongPositions.computeIfAbsent(c, k -> new HashSet<>()).add(i);
                } else {
                    // Добавляем в отсутствующие только если буква точно не в слове
                    if (!presentLetters.contains(c)) {
                        absentLetters.add(c);
                    }
                }
            }
        }

        List<String> candidates = new ArrayList<>();
        for (String word : words) {
            if (matchesConstraints(word, absentLetters, correctPositions, wrongPositions, presentLetters)) {
                candidates.add(word);
            }
        }
        return candidates;
    }

    private boolean matchesConstraints(String word,
                                       Set<Character> absentLetters,
                                       Map<Integer, Character> correctPositions,
                                       Map<Character, Set<Integer>> wrongPositions,
                                       Set<Character> presentLetters) {
        for (char c : absentLetters) {
            if (word.indexOf(c) >= 0) {
                return false;
            }
        }

        for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
            if (word.charAt(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }

        for (char c : presentLetters) {
            if (word.indexOf(c) < 0) {
                return false;
            }
        }

        for (Map.Entry<Character, Set<Integer>> entry : wrongPositions.entrySet()) {
            char c = entry.getKey();
            for (int pos : entry.getValue()) {
                if (word.charAt(pos) == c) {
                    return false;
                }
            }
        }

        return true;
    }
}
