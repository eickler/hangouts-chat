package c8y.jenkins.hangouts;

import javax.annotation.Nonnull;

public interface ReportProvider {

    boolean canProvide();

    @Nonnull
    String report();
}
