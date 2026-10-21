package interfaces;

// Interface for entities that support keyword matching
public interface Searchable {
    boolean matchesKeyword(String keyword);
}