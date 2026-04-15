package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.DictionaryLoadException;
import ru.yandex.practicum.exceptions.InvalidWordException;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionaryException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        try (PrintWriter log = createLog("wordle.log")) {
            log.println("=== Запуск Wordle ===");
            log.flush();

            try {
                WordleDictionaryLoader loader = new WordleDictionaryLoader();
                WordleDictionary dictionary = loader.load("words_ru.txt", log);
                WordleGame game = new WordleGame(dictionary, log);

                printWelcome();

                Scanner scanner = new Scanner(System.in);

                while (!game.isOver()) {
                    System.out.print("Осталось попыток: " + game.getStepsLeft() + " > ");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        String suggestion = game.getSuggestedWord();
                        if (suggestion != null) {
                            System.out.println("Подсказка: " + suggestion);
                        } else {
                            System.out.println("Нет подходящих подсказок.");
                        }
                        continue;
                    }

                    try {
                        String hint = game.makeGuess(input);
                        System.out.println(input);
                        System.out.println(hint);
                    } catch (InvalidWordException e) {
                        System.out.println("Некорректное слово: " + e.getMessage());
                    } catch (WordNotFoundInDictionaryException e) {
                        System.out.println("Слово не найдено в словаре. Попробуйте другое.");
                    }
                }

                System.out.println("---");
                if (game.isWon()) {
                    System.out.println("Поздравляем! Вы угадали слово!");
                } else {
                    System.out.println("Игра окончена. Попытки исчерпаны.");
                }
                System.out.println("Загаданное слово: " + game.getAnswer());

                log.println("Игра завершена. Результат: " + (game.isWon() ? "победа" : "поражение")
                        + ". Слово: " + game.getAnswer());

            } catch (DictionaryLoadException e) {
                log.println("ОШИБКА ПРОГРАММЫ: " + e.getMessage());
                log.flush();
                System.err.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace(log);
                log.flush();
                System.err.println("Критическая ошибка: " + e.getMessage());
            } finally {
                log.println("=== Конец сессии ===");
                log.flush();
            }

        } catch (DictionaryLoadException e) {
            System.err.println("Невозможно создать лог-файл: " + e.getMessage());
        }
    }

    private static PrintWriter createLog(String filename) throws DictionaryLoadException {
        try {
            return new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            throw new DictionaryLoadException("Невозможно создать лог-файл: " + filename, e);
        }
    }

    private static void printWelcome() {
        System.out.println("Добро пожаловать в Wordle!");
        System.out.println("Угадайте слово из 5 русских букв за 6 попыток.");
        System.out.println("  + = буква на правильном месте");
        System.out.println("  ^ = буква есть, но не на том месте");
        System.out.println("  - = буквы нет в слове");
        System.out.println("Введите пустую строку (Enter) — получите подсказку.");
        System.out.println("---");
    }
}
