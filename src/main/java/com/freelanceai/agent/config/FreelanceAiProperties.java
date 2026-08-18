package com.freelanceai.agent.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "freelance-ai")
public class FreelanceAiProperties {

    private Ai ai = new Ai();
    private Scoring scoring = new Scoring();
    private Telegram telegram = new Telegram();
    private Collectors collectors = new Collectors();
    private Workspaces workspaces = new Workspaces();
    private Execution execution = new Execution();

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public void setScoring(Scoring scoring) {
        this.scoring = scoring;
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public void setTelegram(Telegram telegram) {
        this.telegram = telegram;
    }

    public Collectors getCollectors() {
        return collectors;
    }

    public void setCollectors(Collectors collectors) {
        this.collectors = collectors;
    }

    public Workspaces getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(Workspaces workspaces) {
        this.workspaces = workspaces;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public static class Ai {
        private String openaiApiKey = "";
        private String openaiModel = "gpt-4.1-mini";

        public String getOpenaiApiKey() {
            return openaiApiKey;
        }

        public void setOpenaiApiKey(String openaiApiKey) {
            this.openaiApiKey = openaiApiKey;
        }

        public String getOpenaiModel() {
            return openaiModel;
        }

        public void setOpenaiModel(String openaiModel) {
            this.openaiModel = openaiModel;
        }
    }

    public static class Scoring {
        private BigDecimal targetHourlyRate = BigDecimal.valueOf(1500);
        private int minNotificationScore = 80;

        public BigDecimal getTargetHourlyRate() {
            return targetHourlyRate;
        }

        public void setTargetHourlyRate(BigDecimal targetHourlyRate) {
            this.targetHourlyRate = targetHourlyRate;
        }

        public int getMinNotificationScore() {
            return minNotificationScore;
        }

        public void setMinNotificationScore(int minNotificationScore) {
            this.minNotificationScore = minNotificationScore;
        }
    }

    public static class Telegram {
        private String botToken = "";
        private String chatId = "";

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }

    public static class Collectors {
        private boolean enabled = false;
        private String kworkFeedUrl = "";
        private String flRuFeedUrl = "https://www.fl.ru/rss/all.xml";
        private long pollIntervalMs = 1_800_000L;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getKworkFeedUrl() {
            return kworkFeedUrl;
        }

        public void setKworkFeedUrl(String kworkFeedUrl) {
            this.kworkFeedUrl = kworkFeedUrl;
        }

        public String getFlRuFeedUrl() {
            return flRuFeedUrl;
        }

        public void setFlRuFeedUrl(String flRuFeedUrl) {
            this.flRuFeedUrl = flRuFeedUrl;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }
    }

    public static class Workspaces {
        private String rootPath = "workspaces";

        public String getRootPath() {
            return rootPath;
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }
    }

    public static class Execution {
        private String agentCommand = "";
        private long timeoutSeconds = 1_800L;

        public String getAgentCommand() {
            return agentCommand;
        }

        public void setAgentCommand(String agentCommand) {
            this.agentCommand = agentCommand;
        }

        public long getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
