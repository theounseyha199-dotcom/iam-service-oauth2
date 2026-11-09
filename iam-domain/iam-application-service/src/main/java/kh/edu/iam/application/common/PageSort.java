package kh.edu.iam.application.common;

public final class PageSort {
    private PageSort() {}

    public static String field(String value) {
        if (value == null) {
            return "createdAt";
        }
        String[] parts = value.split(",", -1);
        if (parts.length > 2
                || (parts.length == 2 && !parts[1].equalsIgnoreCase("asc") && !parts[1].equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Sort must be field,asc or field,desc");
        }
        return parts[0];
    }

    public static boolean descending(String value) {
        field(value);
        return value != null && value.toLowerCase(java.util.Locale.ROOT).endsWith(",desc");
    }
}
