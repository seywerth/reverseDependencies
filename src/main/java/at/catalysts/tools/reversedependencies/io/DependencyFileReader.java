package at.catalysts.tools.reversedependencies.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;

/**
 * Read dependencies and dependency trees from file
 * <br><br>
 * possible structures (comma or blanks as value separators):
 * <br>artifactId,groupId,version, usedby-artifactId, usedby-groupId, usedby-version
 * <br>artifactId version usedby-artifactId usedby-version
 * <br>artifactId
 * <br>artifactId version
 * <br>artifactId groupId   version
 *
 */
public class DependencyFileReader {

    private File file;
    private boolean reversedWithUsedByDependencies;

    /**
     * construct filereader with file
     * 
     * @param filename
     */
    public DependencyFileReader(String filename) {
        this.file = new File(filename);
        this.reversedWithUsedByDependencies = false;
    }

    public boolean isReversedWithUsedByDependencies() {
        return reversedWithUsedByDependencies;
    }

    public void setReversedWithUsedByDependencies(boolean reversedWithUsedByDependencies) {
        this.reversedWithUsedByDependencies = reversedWithUsedByDependencies;
    }

    /**
     * Read dependency trees from file
     * 
     * @return List<DependencyTree>
     */
    public List<DependencyTree> readDependencyTreeFile() {
        System.out.println("read dependency trees from: " + file.toPath());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return readDependencyTreeStream(lines);
        } catch (IOException e) {
            System.out.println(e.getClass() + ", msg: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Read dependency from file
     * 
     * @return List<Dependency>
     */
    public List<Dependency> readDependencyFile() {
        System.out.println("read dependencies from: " + file.toPath());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return readDependencyStream(lines);
        } catch (IOException e) {
            System.out.println(e.getClass() + ", msg: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Read dependencies from stream
     * 
     * @param lines Stream<String>
     * @return List<DependencyTree>
     */
    protected List<Dependency> readDependencyStream(Stream<String> lines) {
        List<Dependency> foundDeps = new ArrayList<>();

        for (String line : (Iterable<String>) lines::iterator) {
            DependencyTree depT = parseLine(line);
            if (depT != null) {
                foundDeps.add(depT.getDependency());
            }
        }
        System.out.println("read dependencies: " + foundDeps.size());
        return foundDeps;
    }

    /**
     * Read dependency trees from stream
     * 
     * @param lines Stream<String>
     * @return List<DependencyTree>
     */
    protected List<DependencyTree> readDependencyTreeStream(Stream<String> lines) {
        List<DependencyTree> foundReverseDeps = new ArrayList<>();

        for (String line : (Iterable<String>) lines::iterator) {
            DependencyTree depT = parseLine(line);
            if (depT != null) {
                foundReverseDeps.add(depT);
            }
        }
        System.out.println("read dependency trees: " + foundReverseDeps.size());
        return foundReverseDeps;
    }

    /**
     * parse line by blanks, and commas
     * 
     * @param line String
     * @return DependencyTree
     */
    private DependencyTree parseLine(String line) {
        DependencyTree depT = new DependencyTree();
        String artifactId = line.trim();
        String groupId = "";
        String version = "";
        String rArtifactId = "";
        String rGroupId = "";
        String rVersion = "";
        String[] values = null;

        if (artifactId.split(",").length > 1) {
            values = artifactId.split(",");
        } else if (artifactId.split("\\s+").length > 1) {
            values = artifactId.split("\\s+");
        }

        if (values != null) {
            if (values.length > 0) {
                artifactId = values[0].trim();
            }
            if (values.length == 2 || values.length == 4) {
                version = values[1].trim();
            }
            if (values.length == 4) {
                rArtifactId = values[2].trim();
                rVersion = values[3].trim();
            }
            if (values.length == 3 || values.length == 6) {
                groupId = values[1].trim();
                version = values[2].trim();
            }
            if (values.length == 6) {
                rArtifactId = values[3].trim();
                rGroupId = values[4].trim();
                rVersion = values[5].trim();
            }
        }
        if (!artifactId.equals("")) {
            Dependency depR = new Dependency(artifactId, groupId, version);
            depT.setDependency(depR);

            if (!(rArtifactId.equals(""))) {
                Dependency depM = new Dependency(rArtifactId, rGroupId, rVersion);
                DependencyTree depMT = new DependencyTree(depM);
                if (isReversedWithUsedByDependencies()) {
                    depT.getUsedBy().add(depMT);
                } else {
                    depT.getUses().add(depMT);
                }
            }
            return depT;
        }
        return null;
    }

}