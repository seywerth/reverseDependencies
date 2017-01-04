package at.catalysts.tools.reversedependencies.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.test.IntegrationTest;

/**
 * integrative Tests for DependencyFileWriter using DependencyFileReader because of laziness
 *
 */
public class DependencyFileWriterTest {

    private static String TESTFILE_HEADER = "target/testWriteHeader.csv";
    private static String TESTFILE_APPEND = "target/testWriteAppend.csv";
    private static String TESTFILE_DEPLIST = "target/testWriteDepList.csv";
    private static String TESTFILE_REVDEPLIST = "target/testWriteRevDepList.csv";

    @Category(IntegrationTest.class)
    @Test
    public void testCreateFileHeader() {
        DependencyFileWriter writer = new DependencyFileWriter(TESTFILE_HEADER, true, false);
        writer.createFile();
        List<Dependency> result = new DependencyFileReader(TESTFILE_HEADER).readDependencyFile();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Category(IntegrationTest.class)
    @Test
    public void testAppendToFile() {
        DependencyFileWriter writer = new DependencyFileWriter(TESTFILE_APPEND, true, false);
        writer.createFile();
        writer.appendToFile(createDependencyTree("ms-test").getDependency());
        List<Dependency> result = new DependencyFileReader(TESTFILE_APPEND).readDependencyFile();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("queryArtifactId", result.get(0).getArtifactId());
        assertEquals("queryGroupId", result.get(0).getGroupId());
        assertEquals("queryVersion", result.get(0).getVersion());
        assertEquals("ms-test", result.get(1).getArtifactId());
        assertEquals("at.test", result.get(1).getGroupId());
        assertEquals("1.0.1", result.get(1).getVersion());
    }

    @Category(IntegrationTest.class)
    @Test
    public void testWriteDependencyList() {
        DependencyFileWriter writer = new DependencyFileWriter(TESTFILE_DEPLIST, false, false);
        writer.writeDependencyList(createDependencyTrees());
        List<DependencyTree> result = new DependencyFileReader(TESTFILE_DEPLIST).readDependencyTreeFile();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ms-test", result.get(0).getDependency().getArtifactId());
        assertEquals("at.test", result.get(0).getDependency().getGroupId());
        assertEquals("1.0.1", result.get(0).getDependency().getVersion());
        assertEquals(0, result.get(0).getUses().size());
        assertEquals(0, result.get(0).getUsedBy().size());
    }

    @Category(IntegrationTest.class)
    @Test
    public void testWriteDependencyListWithUsedBy() {
        DependencyFileWriter writer = new DependencyFileWriter(TESTFILE_REVDEPLIST, false, true);
        writer.writeDependencyList(createDependencyTrees());
        DependencyFileReader reader = new DependencyFileReader(TESTFILE_REVDEPLIST);
        reader.setReversedWithUsedByDependencies(true);
        List<DependencyTree> result = reader.readDependencyTreeFile();
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("ms-test", result.get(0).getDependency().getArtifactId());
        assertEquals("at.test", result.get(0).getDependency().getGroupId());
        assertEquals("1.0.1", result.get(0).getDependency().getVersion());
        assertEquals(1, result.get(0).getUsedBy().size());
        assertNotNull(result.get(0).getUsedBy().get(0).getDependency());
        assertEquals("revdep", result.get(0).getUsedBy().get(0).getDependency().getArtifactId());
        assertEquals("at.test", result.get(0).getUsedBy().get(0).getDependency().getGroupId());
        assertEquals("3.2", result.get(0).getUsedBy().get(0).getDependency().getVersion());

        assertEquals("ms-test", result.get(1).getDependency().getArtifactId());
        assertEquals("at.test", result.get(1).getDependency().getGroupId());
        assertEquals("1.0.1", result.get(1).getDependency().getVersion());
        assertEquals(1, result.get(1).getUsedBy().size());
        assertEquals("revdep2", result.get(1).getUsedBy().get(0).getDependency().getArtifactId());
        assertEquals("at.test.util", result.get(1).getUsedBy().get(0).getDependency().getGroupId());
        assertEquals("2.2", result.get(1).getUsedBy().get(0).getDependency().getVersion());

        assertEquals("ms-util", result.get(2).getDependency().getArtifactId());
        assertEquals("at.test", result.get(2).getDependency().getGroupId());
        assertEquals("1.0.1", result.get(2).getDependency().getVersion());
        assertEquals(1, result.get(2).getUsedBy().size());
        assertEquals("revdep", result.get(2).getUsedBy().get(0).getDependency().getArtifactId());

        assertEquals("ms-util", result.get(3).getDependency().getArtifactId());
        assertEquals(1, result.get(3).getUsedBy().size());
        assertEquals("revdep2", result.get(3).getUsedBy().get(0).getDependency().getArtifactId());
    }

    public List<DependencyTree> createDependencyTrees() {
        List<DependencyTree> trees = new ArrayList<>();
        trees.add(createDependencyTree("ms-test"));
        trees.add(createDependencyTree("ms-util"));
        return trees;
    }

    public DependencyTree createDependencyTree(String artifactId) {
        Dependency dependency = new Dependency(artifactId, "at.test", "1.0.1");
        Dependency usedBy = new Dependency("revdep", "at.test", "3.2");
        Dependency usedBy2 = new Dependency("revdep2", "at.test.util", "2.2");
        DependencyTree tree = new DependencyTree(dependency);
        tree.getUsedBy().add(new DependencyTree(usedBy));
        tree.getUsedBy().add(new DependencyTree(usedBy2));
        return tree;
    }

}