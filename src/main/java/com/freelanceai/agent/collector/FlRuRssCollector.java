package com.freelanceai.agent.collector;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

@Component
public class FlRuRssCollector implements ProjectCollector {

    private static final Logger log = LoggerFactory.getLogger(FlRuRssCollector.class);
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("/projects/(\\d+)/");
    private static final Pattern BUDGET_PATTERN = Pattern.compile("\\(\\s*Бюджет:\\s*([\\d\\s]+)\\s*(?:руб\\.?|₽|р\\.)[^)]*\\)");
    private static final Pattern PUBLIC_ACCESS_PATTERN = Pattern.compile("\\s*\\(\\s*для всех\\s*\\)\\s*$");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final FreelanceAiProperties properties;
    private final RestClient restClient;

    public FlRuRssCollector(FreelanceAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public List<CollectedProject> collect() {
        String feedUrl = properties.getCollectors().getFlRuFeedUrl();
        if (!StringUtils.hasText(feedUrl)) {
            return List.of();
        }

        try {
            String response = restClient.get()
                    .uri(feedUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(String.class);
            return parse(response);
        } catch (RuntimeException e) {
            log.warn("FL.ru RSS collection failed", e);
            return List.of();
        }
    }

    List<CollectedProject> parse(String rss) {
        if (!StringUtils.hasText(rss)) {
            return List.of();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(rss.getBytes(StandardCharsets.UTF_8)));
            NodeList items = document.getElementsByTagName("item");

            List<CollectedProject> projects = new ArrayList<>();
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String link = text(item, "link");
                String rawTitle = text(item, "title");
                String externalId = externalId(link);
                String title = normalizeTitle(rawTitle);
                if (!StringUtils.hasText(externalId) || !StringUtils.hasText(title)) {
                    continue;
                }
                String description = normalizeDescription(text(item, "description"));
                projects.add(new CollectedProject(
                        ProjectPlatform.FL_RU,
                        externalId,
                        title,
                        description,
                        parseBudget(rawTitle),
                        parsePublishedAt(text(item, "pubDate"))
                ));
            }
            return projects;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse FL.ru RSS feed", e);
        }
    }

    private String text(Element item, String tagName) {
        NodeList nodes = item.getElementsByTagName(tagName);
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent() == null) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    private String externalId(String link) {
        Matcher matcher = PROJECT_ID_PATTERN.matcher(link);
        return matcher.find() ? matcher.group(1) : link;
    }

    private String normalizeTitle(String rawTitle) {
        String withoutBudget = BUDGET_PATTERN.matcher(rawTitle).replaceAll("");
        String withoutAccess = PUBLIC_ACCESS_PATTERN.matcher(withoutBudget).replaceAll("");
        return withoutAccess.trim();
    }

    private String normalizeDescription(String description) {
        return HTML_TAG_PATTERN.matcher(description)
                .replaceAll(" ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private BigDecimal parseBudget(String rawTitle) {
        Matcher matcher = BUDGET_PATTERN.matcher(rawTitle);
        if (!matcher.find()) {
            return null;
        }
        String digits = matcher.group(1).replace(" ", "");
        return StringUtils.hasText(digits) ? new BigDecimal(digits) : null;
    }

    private Instant parsePublishedAt(String value) {
        if (!StringUtils.hasText(value)) {
            return Instant.now();
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (DateTimeParseException e) {
            return Instant.now();
        }
    }
}
