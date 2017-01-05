package at.catalysts.tools.reversedependencies;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.data.Setting;
import at.catalysts.tools.reversedependencies.io.DependencyFileReader;
import at.catalysts.tools.reversedependencies.io.DependencyFileWriter;
import at.catalysts.tools.reversedependencies.io.RepositoryUrlReader;
import at.catalysts.tools.reversedependencies.util.DependencyMatcher;

/**
 * read a list of dependencies of interest and<br>
 * optional read reverse dependency list from file<br>
 * and
 * optional generate a list with (additional) reverse dependencies from a repo
 * optional match against a list of dependencies in use and generate usage list of dependencies of interest
 * <br>
 * optional use name-postfixes to match dependencies of interest<br>
 * <br>
 * run with integrative tests: mvn install -P IT
 * 
 */
public class ReverseDependencies {

    public final static String HELP_TXT = "java -jar reverseDependencies.jar -q query.txt -m inuse.csv"
            + " -r https://repo1.maven.org/maven2/ -c cache.csv";

    public static Setting setting;

    /**
     * Parse program arguments and execute reverse dependency check.
     * 
     * @param args
     */
    public static void main(String[] args) {
        long start = System.nanoTime();

        setting = parseArguments(args);
        if (setting == null) {
            return;
        }

        List<DependencyTree> reverseDependencyTrees = new ArrayList<>();
        if (setting.useCache()) {
            DependencyFileReader reader = new DependencyFileReader(setting.getPathRepositoryCache());
            reader.setReversedWithUsedByDependencies(true);
            reverseDependencyTrees = reader.readDependencyTreeFile();
            System.out.println("reverse dep in cache: " + reverseDependencyTrees.size());
        }

        if (setting.queryRepository()) {
            System.out.println("query repository: " + setting.getRepositoryUrl());
            if (!setting.getSubdirectory().isEmpty()) {
                System.out.println("query subdirectory: " + setting.getSubdirectory());
            }
            List<Dependency> queryDependencies = new ArrayList<>();
            queryDependencies.addAll(new DependencyFileReader(setting.getPathQueryDependencies()).readDependencyFile());

            RepositoryUrlReader repoReader = new RepositoryUrlReader(setting.getRepositoryUrl());
            if (setting.useCache()) {
                DependencyFileWriter cacheWriter = new DependencyFileWriter(setting.getPathRepositoryCache(), false, true);
                repoReader.setupCache(cacheWriter, reverseDependencyTrees);
            }
            repoReader.setSubdirectory(setting.getSubdirectory());
            repoReader.setIgnorePostfixCsv(setting.getIgnorePostfixCsv());
            reverseDependencyTrees.addAll(repoReader.scanUrlDirectory(queryDependencies));
            System.out.println("caching used for: " + repoReader.getCacheUsageCount());
        }

        if (setting.matchResult()) {
            System.out.println("match results with: " + setting.getPathMatchDependencies());
            List<Dependency> matchDependencies = new ArrayList<>();
            matchDependencies.addAll(new DependencyFileReader(setting.getPathMatchDependencies()).readDependencyFile());

            List<DependencyTree> result = DependencyMatcher.matchDependencyQuery(reverseDependencyTrees, matchDependencies, setting.isMatchMajorVersionOnly());
            DependencyFileWriter resultWriter = new DependencyFileWriter(setting.getPathOutputCsv(), true, setting.printUsedBy());
            resultWriter.writeDependencyList(result);
            System.out.println("dep in use found: " + result.size());
        }
        long elapsedTime = System.nanoTime() - start;
        System.out.println("took: " + (elapsedTime/1000000) + " ms");
    }

    protected static Setting parseArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        Setting setting = new Setting();
        Options options = new Options();
        options.addOption("q", "query", true, "path: dependencies we are looking for; eg: query.txt");
        options.addOption("m", "match", true, "path: dependencies currently in use to check against; eg: inuse.csv");
        options.addOption("r", "repository", true, "url: repository to check; eg: https://repo1.maven.org/maven2/");
        options.addOption("s", "subdirectory", true, "url: repository to check; eg: org/apache/maven/plugins/");
        options.addOption("i", "ignore", true, "string: add postfixes to include for matching; eg: -client,-bus-client");
        options.addOption("c", "cache", true, "path: optional file for caching repo for next use; eg: nexus-cache.csv");
        options.addOption("o", "ouput", true, "path: resulting output of dep <- used in dep; eg: depMatches.csv");
        options.addOption("x", "major", false, "specifiy to only match major versions; no output of used-by dependencies");

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("query")) {
                setting.setPathQueryDependencies(line.getOptionValue("query"));
            }
            if (line.hasOption("match")) {
                setting.setPathMatchDependencies(line.getOptionValue("match"));
            }
            if (line.hasOption("repository")) {
                setting.setRepositoryUrl(line.getOptionValue("repository"));
            }
            if (line.hasOption("subdirectory")) {
                setting.setSubdirectory(line.getOptionValue("subdirectory"));
            }
            if (line.hasOption("ignore")) {
                setting.setIgnorePostfixCsv(line.getOptionValue("ignore"));
            }
            if (line.hasOption("cache")) {
                setting.setPathRepositoryCache(line.getOptionValue("cache"));
            }
            if (line.hasOption("ouput")) {
                setting.setPathOutputCsv(line.getOptionValue("ouput"));
            }
            if (line.hasOption("major")) {
                setting.setMatchMajorVersionOnly(true);
            }
        } catch(ParseException exp) {
            formatter.printHelp(HELP_TXT, options);
            System.err.println("argument parsing failed: " + exp.getMessage());
            return null;
        }
        if (setting.getPathQueryDependencies() == null && setting.getPathMatchDependencies() == null) {
            formatter.printHelp(HELP_TXT, options);
            System.err.println("specify at least a path to query or matching dependencies!");
            return null;
        }
        return setting;
    }

}