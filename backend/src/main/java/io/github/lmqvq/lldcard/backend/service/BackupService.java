package io.github.lmqvq.lldcard.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${lldcard.backup.host:localhost}")
    private String dbHost;

    @Value("${lldcard.backup.port:3306}")
    private int dbPort;

    @Value("${lldcard.backup.database:lldcard}")
    private String dbName;

    private final SettingsService settingsService;

    public BackupService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public String createBackup() throws IOException, InterruptedException {
        String backupDir = "backups";
        File dir = new File(backupDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create backup directory");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "backup_" + timestamp + ".sql";
        Path backupPath = Paths.get(backupDir, fileName).toAbsolutePath();

        List<String> commands = Arrays.asList(
                "mysqldump",
                "--host=" + dbHost,
                "--port=" + dbPort,
                "--user=" + dbUsername,
                "--single-transaction",
                "--routines",
                "--events",
                "--databases",
                dbName,
                "--result-file=" + backupPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.environment().put("MYSQL_PWD", dbPassword);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("Database backup failed with exit code " + exitCode + ": " + output);
        }

        applyRetentionPolicy(backupDir);
        return backupPath.toString();
    }

    private void applyRetentionPolicy(String backupDir) {
        try {
            String retentionStr = settingsService.getSetting("backupRetention");
            int retentionCount = 7;
            if (retentionStr != null && !retentionStr.isEmpty()) {
                try {
                    retentionCount = Integer.parseInt(retentionStr);
                } catch (NumberFormatException ignored) {
                    // 使用安全默认值。
                }
            }

            File dir = new File(backupDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith("backup_") && name.endsWith(".sql"));

            if (files != null && files.length > retentionCount) {
                List<File> sortedFiles = Arrays.stream(files)
                        .sorted(Comparator.comparing(File::getName))
                        .collect(Collectors.toList());

                int filesToDelete = sortedFiles.size() - retentionCount;
                for (int i = 0; i < filesToDelete; i++) {
                    File fileToDelete = sortedFiles.get(i);
                    if (!fileToDelete.delete()) {
                        System.err.println("Failed to delete expired backup: " + fileToDelete.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to apply backup retention policy: " + e.getClass().getSimpleName());
        }
    }
}