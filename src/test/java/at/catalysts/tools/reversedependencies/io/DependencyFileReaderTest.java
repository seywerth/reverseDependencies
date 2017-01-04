package at.catalysts.tools.reversedependencies.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.io.DependencyFileReader;
import at.catalysts.tools.reversedependencies.test.IntegrationTest;

public class DependencyFileReaderTest {

    private static String TESTFILE = "src/test/resources/dependencyFile.txt";
    private static String TESTFILE_404 = "src/test/resources/nonexisting";

    @Category(IntegrationTest.class)
    @Test
    public void testReadDependencyWithFile() {
        DependencyFileReader reader = new DependencyFileReader(TESTFILE);
        List<Dependency> result = reader.readDependencyFile();
        assertNotNull(result);
        assertEquals(6, result.size());
    }

    @Category(IntegrationTest.class)
    @Test
    public void testReadDependencyWithFile404() {
        DependencyFileReader reader = new DependencyFileReader(TESTFILE_404);
        List<Dependency> result = reader.readDependencyFile();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testReadDependencyTreeFileWithBlanks() {
        Stream<String> lines = Stream.of("li-test-portlet     1.0.8    ", "  ", " ssp-test-hook group.id 2.1.46 ");
        DependencyFileReader reader = new DependencyFileReader(TESTFILE);

        List<DependencyTree> result = reader.readDependencyTreeStream(lines);

        assertNotNull(result);
        assertEquals(2, result.size());
        Dependency d = result.get(0).getDependency();
        assertEquals(0, result.get(0).getUses().size());
        assertEquals("li-test-portlet", d.getArtifactId());
        assertEquals("", d.getGroupId());
        assertEquals("1.0.8", d.getVersion());
        d = result.get(1).getDependency();
        assertEquals(0, result.get(1).getUses().size());
        assertEquals("ssp-test-hook", d.getArtifactId());
        assertEquals("group.id", d.getGroupId());
        assertEquals("2.1.46", d.getVersion());
    }

    @Test
    public void testReadDependencyFile() {
        Stream<String> lines = Stream.of("li-test-portlet     1.0.8    ", "  ", " ssp-test-hook group.id 2.1.46 ");
        DependencyFileReader reader = new DependencyFileReader(TESTFILE);

        List<Dependency> result = reader.readDependencyStream(lines);

        assertNotNull(result);
        assertEquals(2, result.size());
        Dependency d = result.get(0);
        assertEquals("li-test-portlet", d.getArtifactId());
        assertEquals("", d.getGroupId());
        assertEquals("1.0.8", d.getVersion());
        d = result.get(1);
        assertEquals("ssp-test-hook", d.getArtifactId());
        assertEquals("group.id", d.getGroupId());
        assertEquals("2.1.46", d.getVersion());
    }

    @Test
    public void testReadDependencyTreeFileCSV() {
        Stream<String> lines = Stream.of("ms-test", "ms-data, at.catalysts.test, 1.0.0");
        DependencyFileReader reader = new DependencyFileReader(TESTFILE);

        List<DependencyTree> result = reader.readDependencyTreeStream(lines);

        assertNotNull(result);
        assertEquals(2, result.size());
        Dependency d = result.get(0).getDependency();
        assertEquals(0, result.get(0).getUses().size());
        assertEquals("ms-test", d.getArtifactId());
        assertEquals("", d.getGroupId());
        assertEquals("", d.getVersion());
        d = result.get(1).getDependency();
        assertEquals(0, result.get(1).getUses().size());
        assertEquals("ms-data", d.getArtifactId());
        assertEquals("at.catalysts.test", d.getGroupId());
        assertEquals("1.0.0", d.getVersion());
    }

    @Test
    public void testReadDependencyTreeFileWithUses() {
        Stream<String> lines = Stream.of("ms-test,at.test,1.0.1,revdep,at.test,3.2", "ms-data,,1.0.0,revdep,,20");
        DependencyFileReader reader = new DependencyFileReader(TESTFILE);

        List<DependencyTree> result = reader.readDependencyTreeStream(lines);

        assertNotNull(result);
        assertEquals(2, result.size());
        Dependency d = result.get(0).getDependency();
        assertEquals(1, result.get(0).getUses().size());
        assertEquals("ms-test", d.getArtifactId());
        assertEquals("at.test", d.getGroupId());
        assertEquals("1.0.1", d.getVersion());
        Dependency u = result.get(0).getUses().get(0).getDependency();
        assertEquals(1, result.get(0).getUses().size());
        assertEquals("revdep", u.getArtifactId());
        assertEquals("at.test", u.getGroupId());
        assertEquals("3.2", u.getVersion());

        d = result.get(1).getDependency();
        assertEquals(1, result.get(1).getUses().size());
        assertEquals("ms-data", d.getArtifactId());
        assertEquals("", d.getGroupId());
        assertEquals("1.0.0", d.getVersion());
        u = result.get(1).getUses().get(0).getDependency();
        assertEquals("revdep", u.getArtifactId());
        assertEquals("", u.getGroupId());
        assertEquals("20", u.getVersion());
    }

}