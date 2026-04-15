package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exceptions.InvalidWordException;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {


    private static final String WORD_GEROJ          = "герой";
    private static final String WORD_GONEC          = "гонец";
    private static final String WORD_BUKVA          = "буква";
    private static final String WORD_MOLOT          = "молот";
    private static final String WORD_OOOOO          = "ооооо";
    private static final String WORD_ABVGD          = "абвгд";

    private static final String HINT_ALL_CORRECT    = "+++++";
    private static final String HINT_ALL_MISSING    = "-----";
    private static final String HINT_SPEC           = "+^-^-";

    private static final int    MAX_STEPS           = 6;
    private static final int    WORD_LENGTH         = 5;


    private static PrintWriter testLog;
    private static WordleDictionary testDictionary;

    @BeforeAll
    static void setUpClass() {
        testLog = new PrintWriter(System.out, true);
        List<String> words = Arrays.asList(
                WORD_GEROJ, WORD_GONEC, "слово", WORD_BUKVA, "место",
                "поиск", "игрок", "ответ", "задач", "пятак"
        );
        testDictionary = new WordleDictionary(words);
    }

    @BeforeEach
    void setUp() {
    }

    // Тесты WordleDictionary

    @Test
    @DisplayName("normalize: слово в верхнем регистре переводится в нижний")
    void normalize_uppercaseRussianWord_returnsLowercase() {
        // Given
        String input = "СЛОВО";

        // When
        String result = WordleDictionary.normalize(input);

        // Then
        assertEquals("слово", result);
    }

    @Test
    @DisplayName("normalize: буква ё заменяется на е")
    void normalize_wordWithYo_replacesYoWithYe() {
        // Given
        String input = "ёжик";

        // When
        String result = WordleDictionary.normalize(input);

        // Then
        assertEquals("ежик", result);
    }

    @Test
    @DisplayName("normalize: верхний регистр и ё обрабатываются одновременно")
    void normalize_uppercaseWordWithYo_returnsLowercaseWithYe() {
        // Given
        String input = "ЁЛКА";

        // When
        String result = WordleDictionary.normalize(input);

        // Then
        assertEquals("елка", result);
    }

    @Test
    @DisplayName("contains: слово из словаря найдено")
    void contains_wordInDictionary_returnsTrue() {
        // Given / When / Then
        assertTrue(testDictionary.contains(WORD_GEROJ));
    }

    @Test
    @DisplayName("contains: слова нет в словаре — возвращает false")
    void contains_wordNotInDictionary_returnsFalse() {
        // Given / When / Then
        assertFalse(testDictionary.contains("кошка"));
    }

    @Test
    @DisplayName("matchHint: угаданное слово совпадает с ответом — все символы '+'")
    void matchHint_correctGuess_returnsAllPlus() {
        // Given
        String guess  = WORD_GEROJ;
        String answer = WORD_GEROJ;

        // When
        String hint = WordleDictionary.matchHint(guess, answer);

        // Then
        assertEquals(HINT_ALL_CORRECT, hint);
    }

    @Test
    @DisplayName("matchHint: нет общих букв — все символы '-'")
    void matchHint_noCommonLetters_returnsAllMinus() {
        // Given — "буква" и "герой" не имеют общих букв
        String guess  = WORD_BUKVA;
        String answer = WORD_GEROJ;

        // When
        String hint = WordleDictionary.matchHint(guess, answer);

        // Then
        assertEquals(HINT_ALL_MISSING, hint);
    }

    @Test
    @DisplayName("matchHint: пример из условия задачи — возвращает '+^-^-'")
    void matchHint_specExample_returnsMixedHint() {
        // Given
        String guess  = WORD_GONEC;
        String answer = WORD_GEROJ;

        // When
        String hint = WordleDictionary.matchHint(guess, answer);

        // Then
        assertEquals(HINT_SPEC, hint);
    }

    @Test
    @DisplayName("matchHint: позиционные подсказки из примера задачи корректны")
    void matchHint_specExample_correctPositionHints() {
        // Given
        String guess  = WORD_GONEC;
        String answer = WORD_GEROJ;

        // When
        String hint = WordleDictionary.matchHint(guess, answer);

        // Then
        assertEquals('+', hint.charAt(0)); // г совпал на позиции 0
        assertEquals('^', hint.charAt(1)); // о есть в "герой" (на позиции 3)
        assertEquals('-', hint.charAt(2)); // н отсутствует
        assertEquals('^', hint.charAt(3)); // е есть в "герой" (на позиции 1)
        assertEquals('-', hint.charAt(4)); // ц отсутствует
    }

    @Test
    @DisplayName("matchHint: повторяющаяся буква в догадке — количество подсказок ограничено вхождениями в ответе")
    void matchHint_duplicateLettersInGuess_countsBoundedByAnswer() {
        // Given — в "молот" буква 'о' встречается 2 раза: на позициях 1 и 3
        String guess  = WORD_OOOOO;
        String answer = WORD_MOLOT;

        // When
        String hint = WordleDictionary.matchHint(guess, answer);

        // Then
        assertEquals('+', hint.charAt(1)); // позиция 1: о совпала
        assertEquals('+', hint.charAt(3)); // позиция 3: о совпала
        assertEquals('-', hint.charAt(0)); // лишние о — отсутствуют
        assertEquals('-', hint.charAt(2));
        assertEquals('-', hint.charAt(4));
    }

    @Test
    @DisplayName("getRandomWord: возвращает слово, которое есть в словаре")
    void getRandomWord_calledOnDictionary_returnsWordFromDictionary() {
        // Given / When
        String word = testDictionary.getRandomWord();

        // Then
        assertNotNull(word);
        assertTrue(testDictionary.contains(word));
    }

    @Test
    @DisplayName("filterCandidates: без догадок возвращает все слова словаря")
    void filterCandidates_noGuesses_returnsAllWords() {
        // Given
        List<String> guesses = new ArrayList<>();
        List<String> hints   = new ArrayList<>();

        // When
        List<String> candidates = testDictionary.filterCandidates(guesses, hints);

        // Then
        assertEquals(testDictionary.size(), candidates.size());
    }

    @Test
    @DisplayName("filterCandidates: слова с отсутствующей буквой исключаются")
    void filterCandidates_absentLetterInGuess_eliminatesWordsWithThatLetter() {
        // Given — после "гонец" с подсказкой "+^-^-" буквы 'н' и 'ц' отсутствуют
        List<String> guesses = Collections.singletonList(WORD_GONEC);
        List<String> hints   = Collections.singletonList(HINT_SPEC);

        // When
        List<String> candidates = testDictionary.filterCandidates(guesses, hints);

        // Then — "гонец" сам должен выпасть (содержит 'н' и 'ц')
        assertFalse(candidates.contains(WORD_GONEC));
    }

    @Test
    @DisplayName("filterCandidates: правильный ответ остаётся среди кандидатов")
    void filterCandidates_withHint_keepsAnswerInCandidates() {
        // Given
        List<String> guesses = Collections.singletonList(WORD_GONEC);
        List<String> hints   = Collections.singletonList(HINT_SPEC);

        // When
        List<String> candidates = testDictionary.filterCandidates(guesses, hints);

        // Then — ответ "герой" должен остаться в кандидатах
        assertTrue(candidates.contains(WORD_GEROJ));
    }

    // Тесты WordleGame

    @Test
    @DisplayName("getStepsLeft: новая игра начинается с 6 попытками")
    void getStepsLeft_newGame_returnsSixSteps() {
        // Given / When
        WordleGame game = new WordleGame(testDictionary, testLog);

        // Then
        assertEquals(MAX_STEPS, game.getStepsLeft());
    }

    @Test
    @DisplayName("isOver/isWon: новая игра не завершена и не выиграна")
    void isOver_newGame_returnsFalse() {
        // Given / When
        WordleGame game = new WordleGame(testDictionary, testLog);

        // Then
        assertFalse(game.isOver());
        assertFalse(game.isWon());
    }

    @Test
    @DisplayName("makeGuess: валидное слово уменьшает количество попыток на 1")
    void makeGuess_validWord_reducesStepsByOne() throws Exception {
        // Given
        WordleGame game  = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals(WORD_GEROJ) ? WORD_GONEC : WORD_GEROJ;

        // When
        game.makeGuess(wrong);

        // Then
        assertEquals(MAX_STEPS - 1, game.getStepsLeft());
    }

    @Test
    @DisplayName("makeGuess: возвращает подсказку из 5 символов")
    void makeGuess_validWord_returnsFiveCharHint() throws Exception {
        // Given
        WordleGame game  = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals(WORD_GEROJ) ? WORD_GONEC : WORD_GEROJ;

        // When
        String hint = game.makeGuess(wrong);

        // Then
        assertNotNull(hint);
        assertEquals(WORD_LENGTH, hint.length());
    }

    @Test
    @DisplayName("makeGuess: верное слово переводит игру в состояние «выиграна»")
    void makeGuess_correctWord_setsGameWon() throws Exception {
        // Given
        List<String> singleWord = Collections.singletonList(WORD_GEROJ);
        WordleDictionary dict   = new WordleDictionary(singleWord);
        WordleGame game         = new WordleGame(dict, testLog);

        // When
        String hint = game.makeGuess(WORD_GEROJ);

        // Then
        assertEquals(HINT_ALL_CORRECT, hint);
        assertTrue(game.isWon());
        assertTrue(game.isOver());
    }

    @Test
    @DisplayName("isOver: после 6 неверных попыток игра завершена и не выиграна")
    void isOver_sixWrongGuesses_returnsTrue() throws Exception {
        // Given
        List<String> words    = Arrays.asList(WORD_GEROJ, WORD_GONEC);
        WordleDictionary dict = new WordleDictionary(words);
        WordleGame game       = new WordleGame(dict, testLog);
        String wrong = game.getAnswer().equals(WORD_GEROJ) ? WORD_GONEC : WORD_GEROJ;

        // When
        for (int i = 0; i < MAX_STEPS; i++) {
            game.makeGuess(wrong);
        }

        // Then
        assertFalse(game.isWon());
        assertTrue(game.isOver());
        assertEquals(0, game.getStepsLeft());
    }

    @Test
    @DisplayName("makeGuess: слово короче 5 букв — выбрасывает InvalidWordException")
    void makeGuess_tooShortWord_throwsInvalidWordException() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);

        // When / Then
        assertThrows(InvalidWordException.class, () -> game.makeGuess("кот"));
    }

    @Test
    @DisplayName("makeGuess: слово из латинских букв — выбрасывает InvalidWordException")
    void makeGuess_englishLetters_throwsInvalidWordException() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);

        // When / Then
        assertThrows(InvalidWordException.class, () -> game.makeGuess("hello"));
    }

    @Test
    @DisplayName("makeGuess: пустая строка — выбрасывает InvalidWordException")
    void makeGuess_emptyString_throwsInvalidWordException() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);

        // When / Then
        assertThrows(InvalidWordException.class, () -> game.makeGuess(""));
    }

    @Test
    @DisplayName("makeGuess: слово не в словаре — выбрасывает WordNotFoundInDictionaryException")
    void makeGuess_wordNotInDictionary_throwsWordNotFoundInDictionaryException() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);

        // When / Then
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeGuess(WORD_ABVGD));
    }

    @Test
    @DisplayName("getSuggestedWord: новая игра — подсказка из словаря и не null")
    void getSuggestedWord_newGame_returnsWordFromDictionary() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);

        // When
        String suggestion = game.getSuggestedWord();

        // Then
        assertNotNull(suggestion);
        assertTrue(testDictionary.contains(suggestion));
    }

    @Test
    @DisplayName("getSuggestedWord: несколько вызовов без ходов — подсказки не повторяются")
    void getSuggestedWord_multipleCallsWithoutGuess_eachSuggestionIsUnique() {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);
        Set<String> seen = new HashSet<>();

        // When / Then
        for (int i = 0; i < testDictionary.size(); i++) {
            String suggestion = game.getSuggestedWord();
            if (suggestion == null) {
                break;
            }
            assertFalse(seen.contains(suggestion), "Подсказка повторилась: " + suggestion);
            seen.add(suggestion);
        }
    }

    @Test
    @DisplayName("makeGuess: ход записывается в историю догадок и подсказок")
    void makeGuess_validWord_recordsGuessAndHintInHistory() throws Exception {
        // Given
        WordleGame game = new WordleGame(testDictionary, testLog);
        String wrong = game.getAnswer().equals(WORD_GEROJ) ? WORD_GONEC : WORD_GEROJ;

        // When
        game.makeGuess(wrong);

        // Then
        assertEquals(1, game.getGuesses().size());
        assertEquals(1, game.getHints().size());
        assertEquals(wrong, game.getGuesses().get(0));
    }

    @Test
    @DisplayName("makeGuess: слово в верхнем регистре нормализуется и принимается")
    void makeGuess_uppercaseWord_normalizesAndAcceptsWord() throws Exception {
        // Given
        List<String> singleWord = Collections.singletonList(WORD_GEROJ);
        WordleDictionary dict   = new WordleDictionary(singleWord);
        WordleGame game         = new WordleGame(dict, testLog);

        // When
        String hint = game.makeGuess("ГЕРОЙ");

        // Then
        assertEquals(HINT_ALL_CORRECT, hint);
        assertTrue(game.isWon());
    }
}
