package software.ulpgc.es.monolith.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa un ISBN (ISBN-10 o ISBN-13) validando el formato y el checksum.
 */
public final class ISBN {

    private static final Pattern ISBN_10_PATTERN = Pattern.compile("^\\d{9}[\\dX]$");
    private static final Pattern ISBN_13_PATTERN = Pattern.compile("^97[89]\\d{10}$");

    private final String value;

    public ISBN(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El ISBN no puede ser nulo ni vacío.");
        }

        // Elimina guiones y espacios
        String cleaned = value.replaceAll("[-\\s]", "");

        // Verifica formato y checksum
        if (!isValid(cleaned)) {
            throw new IllegalArgumentException("ISBN inválido: " + value);
        }

        this.value = cleaned;
    }

    public static ISBN of(String value) {
        return new ISBN(value);
    }

    /**
     * Valida ISBN-10 o ISBN-13 incluyendo checksum
     */
    public static boolean isValid(String value) {
        if (ISBN_10_PATTERN.matcher(value).matches()) {
            return isValidISBN10(value);
        } else if (ISBN_13_PATTERN.matcher(value).matches()) {
            return isValidISBN13(value);
        } else {
            return false;
        }
    }

    private static boolean isValidISBN10(String isbn10) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (isbn10.charAt(i) - '0') * (10 - i);
        }
        char lastChar = isbn10.charAt(9);
        int checksum = (lastChar == 'X') ? 10 : (lastChar - '0');
        sum += checksum;
        return sum % 11 == 0;
    }

    private static boolean isValidISBN13(String isbn13) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn13.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        return checksum == (isbn13.charAt(12) - '0');
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISBN)) return false;
        ISBN isbn = (ISBN) o;
        return value.equals(isbn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
