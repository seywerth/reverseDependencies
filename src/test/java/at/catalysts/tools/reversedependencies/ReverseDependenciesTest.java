package at.catalysts.tools.reversedependencies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.data.Setting;
import at.catalysts.tools.reversedependencies.io.DependencyFileReader;
import at.catalysts.tools.reversedependencies.test.IntegrationTest;

public class ReverseDependenciesTest {

    @Category(IntegrationTest.class)
    @Test
    public void testMainWithCache() {
        ReverseDependencies.main(new String[] {"-q", "src/test/resources/query.txt", "-m", "src/test/resources/inuse.csv",
                "-r", "https://repo1.maven.org/maven2/", "-o", "src/test/resources/results.csv",
                "-s", "org/apache/maven/plugins/maven-compiler-plugin/", "-c", "src/test/resources/cache.csv"});
        DependencyFileReader reader = new DependencyFileReader("src/test/resources/results.csv");
        reader.setReversedWithUsedByDependencies(true);
        List<DependencyTree> resultTrees = reader.readDependencyTreeFile();
        assertNotNull(resultTrees);
        assertEquals(2, resultTrees.size());
        assertEquals("queryArtifactId", resultTrees.get(0).getDependency().getArtifactId());
        assertEquals("junit", resultTrees.get(1).getDependency().getArtifactId());
        assertEquals("4.12", resultTrees.get(1).getDependency().getVersion());
        assertEquals(1, resultTrees.get(1).getUsedBy().size());
        assertEquals("maven-compiler-plugin", resultTrees.get(1).getUsedBy().get(0).getDependency().getArtifactId());
        assertEquals("3.6.0", resultTrees.get(1).getUsedBy().get(0).getDependency().getVersion());
    }

    @Test
    public void testParseArgumentsUnknown() {
        assertNull(ReverseDependencies.parseArguments(new String[]{"-a", "asdf"}));
    }

    @Test
    public void testParseArgumentsQueryMissing() {
        assertNull(ReverseDependencies.parseArguments(new String[]{}));
    }

    @Test
    public void testParseArgumentsQuery() {
        Setting s = ReverseDependencies.parseArguments(new String[]{"-q", "query.txt"});
        assertNotNull(s);
        assertEquals("query.txt", s.getPathQueryDependencies());
        assertEquals("", s.getPathRepositoryCache());
        assertNotNull(s.getPathOutputCsv());
        assertFalse(s.isMatchMajorVersionOnly());
    }

    @Test
    public void testParseArgumentsSetting() {
        Setting s = ReverseDependencies.parseArguments(new String[]{"-m", "inuse.csv", "-r", "https://repo1.maven.org/maven2/",
                "-s", "org/apache/maven/plugins/",
                "-i", "\"-client,-bus-client\"", "-c", "cache.csv", "-o", "results.csv", "-x"});
        assertNotNull(s);
        assertEquals("inuse.csv", s.getPathMatchDependencies());
        assertEquals("https://repo1.maven.org/maven2/", s.getRepositoryUrl());
        assertEquals("org/apache/maven/plugins/", s.getSubdirectory());
        assertEquals("-client,-bus-client", s.getIgnorePostfixCsv());
        assertEquals("cache.csv", s.getPathRepositoryCache());
        assertEquals("results.csv", s.getPathOutputCsv());
        assertTrue(s.isMatchMajorVersionOnly());
    }

}