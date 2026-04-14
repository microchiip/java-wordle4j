package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static PrintWriter testLog;
    private static WordleDictionary testDictionary;

    @BeforeAll
    static void setUpClass() {
        testLog = new PrintWriter(System.out, true);
        List<String> words = Arrays.asList(
                "герой", "гонец", "слово", "буква", "место",
                "поиск", "игрок", "ответ", "задач", "пятак"
        );
        testDictionary = new WordleDictionary(words);
    }

    @BeforeEach
    void setUp() {
    }


    // Тесты WordleDictionary

    @Test
    void testNormalizeToLowercase() {
        assertEquals("слово", WordleDictionary.normalize("СЛОВО"));
    }

    @Test
    void testNormalizeYo() {
        assertEquals("ежик", WordleDictionary.normalize("ёжик"));
    }

    @Test
    void testNormalizeMixed() {
        assertEquals("елка", WordleDictionary.normalize("ЁЛКА"));
    }

    @Test
    void testContainsPresent() {
        assertTrue(testDictionary.contains("герой"));
    }

    @Test
    void testContainsAbsent() {
        assertFalse(testDictionary.contains("кошка"));
    }

    @Test
    void testMatchHintAllCorrect() {
        assertEquals("+++++", WordleDictionary.matchHint("герой", "герой"));
    }

    @Test
    void testMatchHintAllMissing() {
        assertEquals("-----", WordleDictionary.matchHint("буква", "герой"));
    }

    @Test
    void testMatchHintFromSpec() {
        assertEquals("+^-^-", WordleDictionary.matchHint("гонец", "герой"));
    }

    @Test
    void testMatchHintDetailedPositions() {
        String hint = WordleDictionary.matchHint("гонец", "герой");
        assertEquals('+', hint.charAt(0)); // г совпал на позиции 0
        assertEquals('^', hint.charAt(1)); // о есть в "герой" (на позиции 3)
        assertEquals('-', hint.charAt(2)); // н отсутствует
        assertEquals('^', hint.charAt(3)); // е есть в "герой" (на позиции 1)
        assertEquals('-', hint.charAt(4)); // ц отсутствует
    }

    @Test
    void testMatchHintDuplicateLetters() {
        String hint = WordleDictionary.matchHint("ооооо", "молот");
        assertEquals('+', hint.charAt(1));
        assertEquals('+', hint.charAt(3));
        assertEquals('-', hint.charAt(0));
        assertEquals('-', hint.charAt(2));
        assertEquals('-', hint.charAt(4));
    }

    @Test
    void testGetRandomWordInDictionary() {
        String word = testDictionary.getRandomWord();
        assertNotNull(word);
        assertTrue(testDictionary.contains(word));
    }

    @Test
    void testFilterCandidatesNoGuesses() {
        List<String> candidates = testDictionary.filterCandidates(
                new ArrayList<>(), new ArrayList<>());
        assertEquals(testDictionary.size(), candidates.size());
    }

    @Test
    void testFilterCandidatesEliminatesAbsentLetter() {
        List<String> guesses = Collections.singletonList("гонец");
        List<String> hints = Collections.singletonList("+^-^-");
        List<String> candidates = testDictionary.filterCandidates(guesses, hints);
        assertFalse(candidates.contains("гонец"));
    }

    @Test
    void testFilterCandidatesKeepsAnswer() {
        List<String> guesses = Collections.singletonList("гонец");
        List<String> hints = Collections.singletonList("+^-^-");
        List<String> candidates = testDictionary.filterCandidates(guesses, hints);
        assertTrue(candidates.contains("герой"));
    }

    // Тесты WordleGame

    @Test
    void testGameInitialSteps() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertEquals(6, game.getStepsLeft());
    }

    @Test
    void testGameInitiallyNotOver() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertFalse(game.isOver());
        assertFalse(game.isWon());
    }

    @Test
    void testMakeGuessReducesSteps() throws Exception {
        WordleGame game = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals("герой") ? "гонец" : "герой";
        game.makeGuess(wrong);
        assertEquals(5, game.getStepsLeft());
    }

    @Test
    void testMakeGuessReturnsHint() throws Exception {
        WordleGame game = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals("герой") ? "гонец" : "герой";
        String hint = game.makeGuess(wrong);
        assertNotNull(hint);
        assertEquals(5, hint.length());
    }

    @Test
    void testWinCondition() throws Exception {
        List<String> singleWord = Collections.singletonList("герой");
        WordleDictionary dict = new WordleDictionary(singleWord);
        WordleGame game = new WordleGame(dict, testLog);

        String hint = game.makeGuess("герой");
        assertEquals("+++++", hint);
        assertTrue(game.isWon());
        assertTrue(game.isOver());
    }

    @Test
    void testGameOverAfterSixWrongGuesses() throws Exception {
        List<String> words = Arrays.asList("герой", "гонец");
        WordleDictionary dict = new WordleDictionary(words);
        WordleGame game = new WordleGame(dict, testLog);

        String wrong = game.getAnswer().equals("герой") ? "гонец" : "герой";
        for (int i = 0; i < 6; i++) {
            game.makeGuess(wrong);
        }

        assertFalse(game.isWon());
        assertTrue(game.isOver());
        assertEquals(0, game.getStepsLeft());
    }

    @Test
    void testInvalidWordTooShort() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertThrows(InvalidWordException.class, () -> game.makeGuess("кот"));
    }

    @Test
    void testInvalidWordEnglishLetters() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertThrows(InvalidWordException.class, () -> game.makeGuess("hello"));
    }

    @Test
    void testInvalidWordEmpty() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertThrows(InvalidWordException.class, () -> game.makeGuess(""));
    }

    @Test
    void testWordNotInDictionary() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeGuess("абвгд"));
    }

    @Test
    void testGetSuggestedWordNotNull() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        String suggestion = game.getSuggestedWord();
        assertNotNull(suggestion);
        assertTrue(testDictionary.contains(suggestion));
    }

    @Test
    void testSuggestedWordsDoNotRepeat() {
        WordleGame game = new WordleGame(testDictionary, testLog);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < testDictionary.size(); i++) {
            String s = game.getSuggestedWord();
            if (s == null) {
                break;
            }
            assertFalse(seen.contains(s), "Подсказка повторилась: " + s);
            seen.add(s);
        }
    }

    @Test
    void testGuessHistoryRecorded() throws Exception {
        WordleGame game = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals("герой") ? "гонец" : "герой";
        game.makeGuess(wrong);
        assertEquals(1, game.getGuesses().size());
        assertEquals(1, game.getHints().size());
        assertEquals(wrong, game.getGuesses().get(0));
    }

    @Test
    void testNormalizationInMakeGuess() throws Exception {
        List<String> singleWord = Collections.singletonList("герой");
        WordleDictionary dict = new WordleDictionary(singleWord);
        WordleGame game = new WordleGame(dict, testLog);
        String hint = game.makeGuess("ГЕРОЙ");
        assertEquals("+++++", hint);
        assertTrue(game.isWon());
    }
}
