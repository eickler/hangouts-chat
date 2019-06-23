package c8y.jenkins.hangouts.chat;

class HangoutsChatMember implements ChatMember {
    private final String displayName;
    private final String name;

    public HangoutsChatMember(String displayName, String name) {
        this.displayName = displayName;
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getName() {
        return name;
    }
}
