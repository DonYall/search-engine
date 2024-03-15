public class Result implements SearchResult, Comparable<Result> {
    private String title;
    private double score;

    public Result(String title, double score) {
        this.title = title;
        this.score = score;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(Result o) {
        if ((double) Math.round(this.score * 1000) / 1000 == (double) Math.round(o.score * 1000) / 1000) {
            return this.title.compareTo(o.title);
        } else if (this.score > o.score) {
            return -1;
        } else {
            return 1;
        }
    }
}
