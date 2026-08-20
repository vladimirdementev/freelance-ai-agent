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
public class FreelancerPublicPageCollector implements ProjectCollector {

    private static final Logger log = LoggerFactory.getLogger(FreelancerPublicPageCollector.class);
    private static final String FREELANCER_BASE_URL = "https://www.freelancer.com";
    private static final Pattern PROJECT_LINK_PATTERN = Pattern.compile(
            "<a\\s+(?=[^>]*class=[\"'][^\"']*JobSearchCard-primary-heading-link[^\"']*[\"'])"
                    + "(?=[^>]*href=[\"']([^\"']+)[\"'])[^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern JOB_DESCRIPTION_PATTERN = Pattern.compile(
            "<p\\s+[^>]*class=[\"'][^\"']*JobSearchCard-primary-description[^\"']*[\"'][^>]*>(.*?)</p>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern JOB_TAG_PATTERN = Pattern.compile(
            "<a\\s+[^>]*class=[\"'][^\"']*JobSearchCard-primary-tagsLink[^\"']*[\"'][^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern H1_PATTERN = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern META_DESCRIPTION_PATTERN = Pattern.compile(
            "<meta[^>]+name=[\"']description[\"'][^>]+content=[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final FreelanceAiProperties properties;
    private final RestClient restClient;

    public FreelancerPublicPageCollector(FreelanceAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public List<CollectedProject> collect() {
        List<String> seedUrls = properties.getCollectors().getFreelancerSeedUrls();
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
                log.warn("Freelancer.com public page collection failed for {}", seedUrl, e);
            }
        }
        return projects;
    }

    List<CollectedProject> parse(String sourceUrl, String html) {
        if (!StringUtils.hasText(sourceUrl) || !StringUtils.hasText(html)) {
            return List.of();
        }

        List<CollectedProject> projects = parseProjectCards(html);
        if (!projects.isEmpty()) {
            return projects;
        }

        String title = firstMatch(html, H1_PATTERN);
        if (!StringUtils.hasText(title)) {
            title = firstMatch(html, TITLE_PATTERN);
        }
        title = normalize(title);
        if (!StringUtils.hasText(title)) {
            return List.of();
        }

        String description = normalize(firstMatch(html, META_DESCRIPTION_PATTERN));
        if (!StringUtils.hasText(description)) {
            description = "Public Freelancer.com page. Actual project details may require additional parsing or authenticated access.";
        }

        return List.of(new CollectedProject(
                ProjectPlatform.FREELANCER,
                externalId(sourceUrl),
                title,
                description,
                (BigDecimal) null,
                Instant.now(),
                sourceUrl,
                sourceCategory(sourceUrl)
        ));
    }

    private List<CollectedProject> parseProjectCards(String html) {
        List<ProjectLink> links = new ArrayList<>();
        Matcher linkMatcher = PROJECT_LINK_PATTERN.matcher(html);
        while (linkMatcher.find()) {
            String href = linkMatcher.group(1);
            if (isFreelancerProjectUrl(href)) {
                links.add(new ProjectLink(href, normalize(linkMatcher.group(2)), linkMatcher.start(), linkMatcher.end()));
            }
        }

        List<CollectedProject> projects = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            ProjectLink link = links.get(i);
            if (!StringUtils.hasText(link.title())) {
                continue;
            }
            int nextStart = i + 1 < links.size() ? links.get(i + 1).start() : html.length();
            String cardHtml = html.substring(link.end(), nextStart);
            String projectUrl = absoluteUrl(link.href());
            String description = normalize(firstMatch(cardHtml, JOB_DESCRIPTION_PATTERN));
            if (!StringUtils.hasText(description)) {
                description = "Public Freelancer.com project listing.";
            }

            projects.add(new CollectedProject(
                    ProjectPlatform.FREELANCER,
                    externalId(projectUrl),
                    link.title(),
                    description,
                    (BigDecimal) null,
                    Instant.now(),
                    projectUrl,
                    sourceCategory(cardHtml, projectUrl)
            ));
        }
        return projects;
    }

    private boolean isFreelancerProjectUrl(String href) {
        return href != null && (href.startsWith("/projects/") || href.startsWith(FREELANCER_BASE_URL + "/projects/"));
    }

    private String absoluteUrl(String href) {
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        return FREELANCER_BASE_URL + href;
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

    private String sourceCategory(String cardHtml, String sourceUrl) {
        List<String> tags = new ArrayList<>();
        Matcher tagMatcher = JOB_TAG_PATTERN.matcher(cardHtml);
        while (tagMatcher.find()) {
            String tag = normalize(tagMatcher.group(1));
            if (StringUtils.hasText(tag)) {
                tags.add(tag);
            }
        }
        return tags.isEmpty() ? sourceCategory(sourceUrl) : String.join(" / ", tags);
    }

    private String sourceCategory(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath().replaceAll("^/+", "").replaceAll("/+$", "");
        if (!StringUtils.hasText(path)) {
            return "freelancer";
        }
        String[] segments = path.split("/");
        List<String> categoryParts = new ArrayList<>();
        boolean projectUrl = "projects".equals(segments[0]);
        int firstIndex = projectUrl ? 1 : 0;
        int lastIndex = projectUrl && segments.length > 2 ? segments.length - 1 : segments.length;
        for (int i = firstIndex; i < lastIndex; i++) {
            if (StringUtils.hasText(segments[i])) {
                categoryParts.add(segments[i].replace("-", " "));
            }
        }
        return categoryParts.isEmpty() ? "freelancer" : String.join(" / ", categoryParts);
    }

    private record ProjectLink(String href, String title, int start, int end) {
    }
}
