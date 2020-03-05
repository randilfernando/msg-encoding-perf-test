package com.alternate.scripts;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * @author randilfernando
 */
public class TextConvert {
    public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile(
            "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        System.out.println("str---------------------------output    "+ str);
        return str;
    }

    public static void main(String[] args) {
        stripDiacritics("Et Ça sera sa moitié." );
        stripDiacritics("Et Ca sera sa moitie.");
    }
}
