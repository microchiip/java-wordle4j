package ru.yandex.practicum;

public class DictionaryLoadException extends WordleException {

    public DictionaryLoadException(String message) {
        super(message);
    }

    public DictionaryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

