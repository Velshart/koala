package me.mmtr.koala.util;

import java.util.HashMap;
import java.util.Map;

public class StringConverter {
    private static final Map<Character, Character> CHAR_MAP = new HashMap<>();

    static {
        CHAR_MAP.put('ą', 'a');
        CHAR_MAP.put('ć', 'c');
        CHAR_MAP.put('ę', 'e');
        CHAR_MAP.put('ł', 'l');
        CHAR_MAP.put('ń', 'n');
        CHAR_MAP.put('ó', 'o');
        CHAR_MAP.put('ś', 's');
        CHAR_MAP.put('ź', 'z');
        CHAR_MAP.put('ż', 'z');
        CHAR_MAP.put('Ą', 'A');
        CHAR_MAP.put('Ć', 'C');
        CHAR_MAP.put('Ę', 'E');
        CHAR_MAP.put('Ł', 'L');
        CHAR_MAP.put('Ń', 'N');
        CHAR_MAP.put('Ó', 'O');
        CHAR_MAP.put('Ś', 'S');
        CHAR_MAP.put('Ź', 'Z');
        CHAR_MAP.put('Ż', 'Z');

        CHAR_MAP.put('ä', 'a');
        CHAR_MAP.put('ë', 'e');
        CHAR_MAP.put('ï', 'i');
        CHAR_MAP.put('ö', 'o');
        CHAR_MAP.put('ü', 'u');
        CHAR_MAP.put('ÿ', 'y');
        CHAR_MAP.put('Ä', 'A');
        CHAR_MAP.put('Ë', 'E');
        CHAR_MAP.put('Ï', 'I');
        CHAR_MAP.put('Ö', 'O');
        CHAR_MAP.put('Ü', 'U');
        CHAR_MAP.put('Ÿ', 'Y');

        CHAR_MAP.put('á', 'a');
        CHAR_MAP.put('é', 'e');
        CHAR_MAP.put('í', 'i');
        CHAR_MAP.put('ú', 'u');
        CHAR_MAP.put('ý', 'y');
        CHAR_MAP.put('Á', 'A');
        CHAR_MAP.put('É', 'E');
        CHAR_MAP.put('Í', 'I');
        CHAR_MAP.put('Ú', 'U');
        CHAR_MAP.put('Ý', 'Y');

        CHAR_MAP.put('â', 'a');
        CHAR_MAP.put('ê', 'e');
        CHAR_MAP.put('î', 'i');
        CHAR_MAP.put('ô', 'o');
        CHAR_MAP.put('û', 'u');
        CHAR_MAP.put('Â', 'A');
        CHAR_MAP.put('Ê', 'E');
        CHAR_MAP.put('Î', 'I');
        CHAR_MAP.put('Ô', 'O');
        CHAR_MAP.put('Û', 'U');

        CHAR_MAP.put('à', 'a');
        CHAR_MAP.put('è', 'e');
        CHAR_MAP.put('ì', 'i');
        CHAR_MAP.put('ò', 'o');
        CHAR_MAP.put('ù', 'u');
        CHAR_MAP.put('À', 'A');
        CHAR_MAP.put('È', 'E');
        CHAR_MAP.put('Ì', 'I');
        CHAR_MAP.put('Ò', 'O');
        CHAR_MAP.put('Ù', 'U');

        CHAR_MAP.put('ã', 'a');
        CHAR_MAP.put('ñ', 'n');
        CHAR_MAP.put('õ', 'o');
        CHAR_MAP.put('Ã', 'A');
        CHAR_MAP.put('Ñ', 'N');
        CHAR_MAP.put('Õ', 'O');

        CHAR_MAP.put('å', 'a');
        CHAR_MAP.put('Å', 'A');

        CHAR_MAP.put('æ', 'a');
        CHAR_MAP.put('œ', 'o');
        CHAR_MAP.put('Æ', 'A');
        CHAR_MAP.put('Œ', 'O');

        CHAR_MAP.put('ç', 'c');
        CHAR_MAP.put('Ç', 'C');

        CHAR_MAP.put('ð', 'd');
        CHAR_MAP.put('Ð', 'D');

        CHAR_MAP.put('ø', 'o');
        CHAR_MAP.put('Ø', 'O');

        CHAR_MAP.put('ß', 's');

        CHAR_MAP.put('þ', 't');
        CHAR_MAP.put('Þ', 'T');
    }

    public static String replaceSpecialChars(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            result.append(CHAR_MAP.getOrDefault(c, c));
        }
        return result.toString();
    }
}
