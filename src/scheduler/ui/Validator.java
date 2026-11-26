package scheduler.ui;

/** Turns raw text into a whole number, or throws a clear message (same wording as the console versions). */
final class Validator {
    private Validator() {}

    static int readInt(String raw, String label, int min, int max) {
        String s = raw == null ? "" : raw.trim();
        String range = " Enter a whole number from " + min + " to " + max + ".";
        if (s.isEmpty())                  throw new IllegalArgumentException(label + ": Input cannot be empty." + range);
        if (s.matches("-?\\d+\\.\\d*"))   throw new IllegalArgumentException(label + ": Decimals are not allowed." + range);
        if (s.matches("-\\d+"))           throw new IllegalArgumentException(label + ": Negative numbers are not allowed." + range);
        if (!s.matches("\\d+"))           throw new IllegalArgumentException(label + ": Letters, symbols and spaces are not allowed." + range);
        if (s.length() > 9 || Integer.parseInt(s) < min || Integer.parseInt(s) > max)
                                          throw new IllegalArgumentException(label + ": Number is out of range." + range);
        return Integer.parseInt(s);
    }
}
