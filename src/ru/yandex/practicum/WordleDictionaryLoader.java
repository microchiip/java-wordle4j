package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {


    public WordleDictionary load(String filename, PrintWriter log) throws DictionaryLoadException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new DictionaryLoadException("Файл словаря не найден: " + filename);
        }

        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = WordleDictionary.normalize(line.trim());
                if (isValidGameWord(normalized)) {
                    words.add(normalized);
                }
            }
        } catch (IOException e) {
            throw new DictionaryLoadException("Ошибка чтения файла словаря: " + filename, e);
        }

        if (words.isEmpty()) {
            throw new DictionaryLoadException(
                    "Словарь не содержит подходящих слов (нужны существительные из 5 русских букв)");
        }

        log.println("Словарь загружен: " + words.size() + " слов из файла " + filename);
        log.flush();

        return new WordleDictionary(words);
    }

    private boolean isValidGameWord(String word) {
        if (word.length() != 5) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c < 'а' || c > 'я') {
                return false;
            }
        }
        return true;
    }
}
