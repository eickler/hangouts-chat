package c8y.jenkins.hangouts.chat;

public class Link {
    private final String url;
    private final String description;

    public static Link of(String url, String description) {
        return new Link(url, description);
    }

    private Link(String url, String description) {
        this.url = url;
        this.description = description;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("<").append(url)
                .append("|")
                .append(description).append(">")
                .toString();
    }
}


