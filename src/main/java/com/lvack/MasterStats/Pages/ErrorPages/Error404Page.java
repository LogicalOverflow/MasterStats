package com.lvack.MasterStats.Pages.ErrorPages;

import com.lvack.MasterStats.Pages.BasePage;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Error404PageClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * simple 404 not found error page showing the error message if one exists
 */
public class Error404Page extends BasePage {
    public Error404Page(PageParameters parameters) {
        super(parameters, "404", null);
        List<FeedbackMessage> feedbackMessages = getSession().getFeedbackMessages().toList();
        String message = feedbackMessages.stream().map(FeedbackMessage::getMessage)
                .map(Serializable::toString).collect(Collectors.joining(", "));
        Label label = new Label("message", "By the way, the server wants me to tell you the following: " + message);
        if (message.length() == 0) label.setVisible(false);
        add(label);
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }
}
