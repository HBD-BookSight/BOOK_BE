import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class JsonlSplitter {
    public static void main(String[] args) throws IOException {
Path inputPath = Paths.get("books.jsonl");
Path outputDir = Paths.get("chunks");
        int chunkSize = 5000;
        int chunkIndex = 1;

        if (!Files.exists(inputPath)) {
            System.err.println("❌ 입력 파일이 존재하지 않습니다: " + inputPath);
            return;
        }

        Files.createDirectories(outputDir);
        List<String> buffer = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                if (buffer.size() == chunkSize) {
                    writeChunk(buffer, outputDir, chunkIndex++);
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                writeChunk(buffer, outputDir, chunkIndex);
            }
        }

        System.out.println("✅ 분할 완료: " + (chunkIndex) + "개 chunk 생성됨");
    }

    private static void writeChunk(List<String> lines, Path dir, int index) throws IOException {
        Path file = dir.resolve(String.format("chunk-%03d.jsonl", index));
        Files.write(file, lines, StandardCharsets.UTF_8);
        System.out.printf("✅ 생성됨: %s (%d줄)%n", file.getFileName(), lines.size());
    }
}
