package ch.fhnw.apm.docfinder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@State(Scope.Benchmark)
public class DocFinderBenchmarks {
    public static final String SEARCH_TEXT = "friend";

    @Param({"1", "7"})
    public int threads;

    @Param({"true", "false"})
    public boolean ignoreCase;

    @Param({"friend", "hello friend world"})
    public String searchText;

    private DocFinder docFinder;

    @Setup
    public void setup() {
        var booksDir = Path.of("doc-finder/perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }

        docFinder = new DocFinder(booksDir);
        docFinder.setIgnoreCase(ignoreCase);
    }

    // @Benchmark
    @BenchmarkMode(Mode.Throughput)
    // @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 2)
    @Measurement(iterations = 30, time = 2)
    public List<Result> findDocs() throws IOException {
        return docFinder.findDocs(searchText, threads);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DocFinderBenchmarks.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
