package uk.gov.ida.notification.views;

import com.google.common.collect.ImmutableList;
import io.dropwizard.views.View;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ErrorPageView extends View {

    private final String issue;

    public ErrorPageView(String issue) {
        super("error-page.mustache");
        this.issue = issue;
    }

    public ErrorPageView(Throwable e) {
        super("error-page.mustache");
        this.issue = e.getMessage();
    }

    public String getIssue() {
        return issue;
    }

    public String getReaction() {
        final List<String> reactions = ImmutableList.of("😧","😮","😢","😭","👎","😶","🙃");
        final int reaction = new Random().nextInt(reactions.size());
        return reactions.get(reaction);
    }
}
