package com.freelanceai.agent.collector;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

@Component
public class WorkzillaPublicPageCollector implements ProjectCollector {

    private static final Logger log = LoggerFactory.getLogger(WorkzillaPublicPageCollector.class);
    private static final Pattern H1_PATTERN = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
            "<meta[^>]+name=[\"']description[\"'][^>]+content=[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final FreelanceAiProperties properties;
    private final RestClient restClient;

    public WorkzillaPublicPageCollector(FreelanceAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public List<CollectedProject> collect() {
        List<String> seedUrls = properties.getCollectors().getWorkzillaSeedUrls();
        if (seedUrls == null || seedUrls.isEmpty()) {
            return List.of();
        }

        List<CollectedProject> projects = new ArrayList<>();
        for (String seedUrl : seedUrls) {
            if (!StringUtils.hasText(seedUrl)) {
                continue;
            }
            try {
                String html = restClient.get()
                        .uri(seedUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .retrieve()
                        .body(String.class);
                parse(seedUrl, html).forEach(projects::add);
            } catch (RuntimeException e) {
                log.warn("Workzilla public page collection failed for {}", seedUrl, e);
            }
        }
        return projects;
    }

    List<CollectedProject> parse(String sourceUrl, String html) {
        if (!StringUtils.hasText(sourceUrl) || !StringUtils.hasText(html)) {
            return List.of();
        }

        String title = firstMatch(html, H1_PATTERN);
        if (!StringUtils.hasText(title)) {
            title = firstMatch(html, TITLE_PATTERN);
        }
        title = normalize(title);
        if (!StringUtils.hasText(title)) {
            return List.of();
        }

        String description = normalize(firstMatch(html, DESCRIPTION_PATTERN));
        if (!StringUtils.hasText(description)) {
            description = "Public Workzilla page. Actual task feed may require authenticated access.";
        }

        return List.of(new CollectedProject(
                ProjectPlatform.WORKZILLA,
                externalId(sourceUrl),
                title,
                description,
                (BigDecimal) null,
                Instant.now(),
                sourceUrl,
                sourceCategory(sourceUrl)
        ));
    }

    private String firstMatch(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String normalize(String html) {
        String withoutTags = TAG_PATTERN.matcher(html == null ? "" : html).replaceAll(" ");
        return HtmlUtils.htmlUnescape(withoutTags)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String externalId(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath();
        String normalized = path.replaceAll("^/+", "").replaceAll("[^A-Za-z0-9]+", "-");
        if (!StringUtils.hasText(normalized)) {
            return Integer.toUnsignedString(sourceUrl.hashCode());
        }
        return normalized.toLowerCase(Locale.ROOT).replaceAll("(^-|-$)", "");
    }

    private String sourceCategory(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath().replaceAll("^/+", "");
        if (!StringUtils.hasText(path)) {
            return "workzilla";
        }
        String[] segments = path.replace("freelance-jobs/", "").split("/");
        List<String> categoryParts = new ArrayList<>();
        for (String segment : segments) {
            if (StringUtils.hasText(segment)) {
                categoryParts.add(segment.replace("-", " "));
            }
        }
        return categoryParts.isEmpty() ? "workzilla" : String.join(" / ", categoryParts);
    }
}
