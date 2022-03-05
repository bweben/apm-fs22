package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.DoubleStream;

public class DocFinderPerfTester {

    private static final int REPETITIONS = 30;
    public static final String SEARCH_TEXT = "friend";

    public static void main(String[] args) throws IOException {
        var booksDir = Path.of("perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }

        var start = 1;
        var end = Runtime.getRuntime().availableProcessors() * 2;

        var outputFile = Path.of("results.csv");
        Files.deleteIfExists(outputFile);
        Files.createFile(outputFile);
        Files.writeString(outputFile, "nThreads,avg,min,max,throughput\n");

        for (int nThreads = start; nThreads <= end; nThreads++) {
            runPerformanceTest(booksDir, nThreads, outputFile);
        }
    }

    private static void runPerformanceTest(Path booksDir, int nThreads, Path outputFile) throws IOException {
        var finder = new DocFinder(booksDir);

        var latencies = new double[REPETITIONS];
        for (int i = 0; i < REPETITIONS; i++) {
            var startTime = System.nanoTime();

            finder.findDocs(SEARCH_TEXT, nThreads);

            var latency = System.nanoTime() - startTime;
            latencies[i] = latency / 1_000_000.0; // convert to ms

            // print progress to err
            if ((i + 1) % 10 == 0) {
                System.err.println(i + 1 + "/" + REPETITIONS + " repetitions");
            }
        }
        System.err.println();

        for (int i = 0; i < REPETITIONS; i++) {
            System.out.printf("%.1f\n", latencies[i]);
        }
        System.out.println();

        var stats = DoubleStream.of(latencies).summaryStatistics();
        System.out.printf("Average: %.1f ms\n", stats.getAverage());
        System.out.printf("Min: %.1f ms\n", stats.getMin());
        System.out.printf("Max: %.1f ms\n", stats.getMax());

        var throughput = 1 / stats.getAverage();
        System.out.printf("Throughput: %.3f docs/s\n", throughput * 1000);

        // write stats to csv file
        Files.writeString(outputFile, String.format("%d,%.1f,%.1f,%.1f,%.3f\n",
                nThreads, stats.getAverage(), stats.getMin(), stats.getMax(), throughput * 1000), StandardOpenOption.APPEND);
    }
}
