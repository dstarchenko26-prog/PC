package select;

public class SelectionResult {
    public final int value;
    public final int totalUnique;

    public SelectionResult(int value, int totalUnique) {
        this.value = value;
        this.totalUnique = totalUnique;
    }
}