package ru.yandex.practicum;

public class WordNotFoundInDictionaryException extends WordleException {

    public WordNotFoundInDictionaryException(String word) {
        super("Слово '" + word + "' отсутствует в словаре");
    }
}

