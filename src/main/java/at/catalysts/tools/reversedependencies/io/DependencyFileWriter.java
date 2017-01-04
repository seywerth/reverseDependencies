package at.catalysts.tools.reversedependencies.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;

/**
 * Write and append dependencies and dependency trees to file <br>
 * <br>
 * possible structures: <br>
 * artifactId,groupId,version,usedby-artifactId,usedby-groupId,usedby-version<br>
 * artifactId,groupId,version
 *
 */
public class DependencyFileWriter {

    private File file;
    private boolean printHeader;
    private boolean printUsedBy;

    /**
     * construct filewriter with file
     * 
     * @param filename
     */
    public DependencyFileWriter(String filename, boolean printHeader, boolean printUsedBy) {
        this.file = new File(filename);
        this.printHeader = printHeader;
        this.printUsedBy = printUsedBy;
    }

    /**
     * create file with csv-header
     */
    private void createFileHeader(PrintWriter out) {
        System.out.println("write header to: " + file.toPath());
        if (printUsedBy) {
            out.println("queryArtifactId,queryGroupId,queryVersion,usedByArtifactId,usedByGroupId,usedByVersion");
        } else {
            out.println("queryArtifactId,queryGroupId,queryVersion");
        }
    }


    public boolean fileExists() {
        if (this.file.exists() && !this.file.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * create/clear file
     */
    public void createFile() {
        System.out.println("create/clear file: " + file.toPath());
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
            if (printHeader) {
                createFileHeader(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * print dependencies to file
     * 
     * @param depTrees
     */
    public void writeDependencyList(List<DependencyTree> depTrees) {
        long countFound = 0;
        createFile();
        for (DependencyTree t : depTrees) {
            Dependency d = t.getDependency();
            System.out.println(d.getArtifactId() + "," + d.getGroupId() + "," + d.getVersion());
            for (DependencyTree u : t.getUsedBy()) {
                Dependency i = u.getDependency();
                System.out.println(" <- " + i.getArtifactId() + "," + i.getGroupId() + "," + i.getVersion());
                if (printUsedBy) {
                    appendToFile(t.getDependency(), i);
                    countFound++;
                }
            }
            if (!printUsedBy) {
                appendToFile(d);
                countFound++;
            }
        }
        System.out.println("wrote dependencies: " + countFound);
    }

    /**
     * append dependency-line to file
     * 
     * @param used dependency in question
     */
    public void appendToFile(Dependency used) {
        appendToFile(used, null);
    }

    /**
     * append dependency-line to file
     * 
     * @param used dependency in question
     * @param model used by this dependency
     */
    public void appendToFile(Dependency dep, Dependency usedBy) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            String line = dep.getArtifactId() + "," + dep.getGroupId() + "," + dep.getVersion();
            if (printUsedBy && usedBy != null) {
                line += "," + usedBy.getArtifactId() + "," + usedBy.getGroupId() + "," + usedBy.getVersion();
            }
            out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}