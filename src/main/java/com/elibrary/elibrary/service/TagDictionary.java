package com.elibrary.elibrary.service;

import java.util.List;
import java.util.Arrays;

public class TagDictionary {

    public static final List<String> DEFAULT_TAGS = Arrays.asList(
            "научная", "учебная", "художественная", "детская", "историческая",
            "техническая", "фантастика", "психология", "бестселлер", "новинка"
    );

    private TagDictionary() {} // приватный конструктор — утилитный класс
}