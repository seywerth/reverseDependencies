package at.catalysts.tools.reversedependencies.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.test.IntegrationTest;

public class RepositoryUrlReaderTest {

    @Category(IntegrationTest.class)
    @Test
    public void testScanUrlDirectoryWithCache() {
        String testrepo = "https://repo1.maven.org/maven2/";
        String testcache = "target/testcache.csv";
        DependencyFileWriter cacheWriter = new DependencyFileWriter(testcache, false, true);
        //repoWriter.createFile(); // clear done by mvn clean
        RepositoryUrlReader repoReader = new RepositoryUrlReader(testrepo);
        repoReader.setupCache(cacheWriter, createCachedDependencies());
        repoReader.setSubdirectory("org/apache/maven/plugins/maven-compiler-plugin/");

        List<DependencyTree> scannedDependencyTrees = repoReader.scanUrlDirectory(createQueryDependencies());
        assertNotNull(scannedDependencyTrees);
        assertTrue(scannedDependencyTrees.size() > 0);
        assertEquals("junit", scannedDependencyTrees.get(0).getDependency().getArtifactId());
        assertTrue(scannedDependencyTrees.get(0).getUsedBy().size() > 0);
        assertEquals("maven-compiler-plugin", scannedDependencyTrees.get(0).getUsedBy().get(0).getDependency().getArtifactId());
    }

    @Category(IntegrationTest.class)
    @Test
    public void testScanUrlDirectoryWithoutCache() {
        String testrepo = "https://repo1.maven.org/maven2/commons-cli/commons-cli/";
        RepositoryUrlReader repoReader = new RepositoryUrlReader(testrepo);

        List<DependencyTree> scannedDependencyTrees = repoReader.scanUrlDirectory(createQueryDependencies());
        assertNotNull(scannedDependencyTrees);
        assertTrue(scannedDependencyTrees.size() > 0);
        assertEquals("junit", scannedDependencyTrees.get(0).getDependency().getArtifactId());
        assertTrue(scannedDependencyTrees.get(0).getUsedBy().size() > 0);
        assertEquals("commons-cli", scannedDependencyTrees.get(0).getUsedBy().get(0).getDependency().getArtifactId());
    }

    public List<Dependency> createQueryDependencies() {
        List<Dependency> queryDependencies = new ArrayList<>();
        Dependency dep = new Dependency("junit", "", "");
        queryDependencies.add(dep);
        return queryDependencies;
    }

    @Test
    public void testFindCachedDependencyTree() {
        String repoUrl = "https://repo1.maven.org/maven2/";
        String juniturl = "https://repo1.maven.org/maven2/junit/junit/3.8.1/";
        List<DependencyTree> trees = RepositoryUrlReader.findCachedDependencyTrees(createCachedDependencies(), repoUrl, juniturl);
        assertEquals(1, trees.size());
        assertEquals("junit", trees.get(0).getDependency().getArtifactId());
        assertEquals("3.8.1", trees.get(0).getDependency().getVersion());
    }

    @Test
    public void testFindCachedDependencyTreeMvnCompPlugin() {
        String repoUrl = "https://repo1.maven.org/maven2/";
        String mvncomp = "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.6.0/";
        List<DependencyTree> trees = RepositoryUrlReader.findCachedDependencyTrees(createCachedDependencies(), repoUrl, mvncomp);
        assertEquals(1, trees.size());
        assertEquals("junit", trees.get(0).getDependency().getArtifactId());
        assertEquals("4.12", trees.get(0).getDependency().getVersion());
    }

    public List<DependencyTree> createCachedDependencies() {
        List<DependencyTree> trees = new ArrayList<>();
        Dependency dj = new Dependency("junit", "junit", "3.8.1");
        DependencyTree dtreej = new DependencyTree(dj);
        dtreej.getUsedBy().add(dtreej);
        trees.add(dtreej);
        Dependency dt = new Dependency("test", "com.test", "3.1");
        DependencyTree dtreet = new DependencyTree(dt);
        dtreet.getUsedBy().add(dtreet);
        trees.add(dtreet);
        Dependency dx = new Dependency("test", "com.test", null);
        DependencyTree dtreex = new DependencyTree(dx);
        dtreex.getUsedBy().add(dtreex);
        trees.add(dtreex);
        //org/apache/maven/plugins/maven-compiler-plugin/3.6.0
        Dependency mvncom = new Dependency("maven-compiler-plugin", "org.apache.maven.plugins", "3.6.0");
        Dependency junit4 = new Dependency("junit", "junit", "4.12");
        DependencyTree tjunit4 = new DependencyTree(junit4);
        DependencyTree tmvncomp = new DependencyTree(mvncom);
        tjunit4.getUsedBy().add(tmvncomp);
        trees.add(tjunit4);
        return trees;
    }

    @Test
    public void testParseDependencyFromUrlAbsolute() {
        String repoUrl = "https://repo1.maven.org/maven2/";
        assertNull(RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/junit/"));
        assertNull(RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/3.8/"));
        assertNull(RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/junit/3.8/../"));
        assertNull(RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/junit/test"));
        Dependency d = RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/junit/3.8/");
        assertNotNull(d);
        assertEquals("junit", d.getGroupId());
        assertEquals("junit", d.getArtifactId());
        assertEquals("3.8", d.getVersion());
        d = RepositoryUrlReader.parseDependencyFromUrl(repoUrl, "https://repo1.maven.org/maven2/junit/x/group/test/3/test.pom");
        assertNotNull(d);
        assertEquals("junit.x.group", d.getGroupId());
        assertEquals("test", d.getArtifactId());
        assertEquals("3", d.getVersion());
    }

    @Test
    public void testParseDependencyFromUrlRelative() {
        assertNull(RepositoryUrlReader.parseDependencyFromUrl("", "/junit/3.8/"));
        assertNull(RepositoryUrlReader.parseDependencyFromUrl("", "junit/junit/3"));
        assertEquals("junit", RepositoryUrlReader.parseDependencyFromUrl("", "test/junit/junit/").getVersion());
        assertEquals("3.8", RepositoryUrlReader.parseDependencyFromUrl("", "junit/junit/3.8/").getVersion());
        assertEquals("3.8", RepositoryUrlReader.parseDependencyFromUrl("", "junit/junit/3.8/test.pom").getVersion());
        assertEquals("group", RepositoryUrlReader.parseDependencyFromUrl("", "group/junit/3.8/test.pom").getGroupId());
        assertEquals("com.group.x", RepositoryUrlReader.parseDependencyFromUrl("", "/com/group/x/junit/3.8/t.pom").getGroupId());
    }

    @Test
    public void testMatchesArtifactIdIgnorePostfixEquals() {
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId", "artifactId", null));
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId", "artifactId", ""));
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId", "artifactId", "-test"));
    }

    @Test
    public void testMatchesArtifactIdIgnorePostfixFalse() {
        assertFalse(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId", "artifactid", null));
        assertFalse(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId", "artifactId1", ""));
        assertFalse(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId-tes", "artifactId", "-test"));
    }

    @Test
    public void testMatchesArtifactIdIgnorePostfixWithPostfix() {
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId-test", "artifactId", "-test"));
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId-test", "artifactId-", "-tes,test"));
        assertTrue(RepositoryUrlReader.matchesArtifactIdIgnorePostfix("artifactId-test", "artifactId", "-x,-test,-testa"));
    }

    @Test
    public void testIsSubfolderOrFile() {
        assertFalse(RepositoryUrlReader.isSubFolderOrFile(""));
        assertFalse(RepositoryUrlReader.isSubFolderOrFile("."));
        assertFalse(RepositoryUrlReader.isSubFolderOrFile("../"));
        assertFalse(RepositoryUrlReader.isSubFolderOrFile("./"));
        assertTrue(RepositoryUrlReader.isSubFolderOrFile("test/"));
        assertTrue(RepositoryUrlReader.isSubFolderOrFile("pom.xml"));
        assertTrue(RepositoryUrlReader.isSubFolderOrFile("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/commons-cli-1.2.pom"));
        assertTrue(RepositoryUrlReader.isSubFolderOrFile("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/"));
    }

    @Test
    public void testIsFolder() {
        assertFalse(RepositoryUrlReader.isFolder("test"));
        assertFalse(RepositoryUrlReader.isFolder("../"));
        assertFalse(RepositoryUrlReader.isFolder("pom.xml"));
        assertFalse(RepositoryUrlReader.isFolder("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/commons-cli-1.2.pom"));
        assertTrue(RepositoryUrlReader.isFolder("test/"));
        assertTrue(RepositoryUrlReader.isFolder("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/"));
    }

    @Test
    public void testIsPom() {
        assertFalse(RepositoryUrlReader.isPom(""));
        assertFalse(RepositoryUrlReader.isPom("../"));
        assertFalse(RepositoryUrlReader.isPom("test/"));
        assertFalse(RepositoryUrlReader.isPom("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/"));
        assertTrue(RepositoryUrlReader.isPom("test.pom"));
        assertTrue(RepositoryUrlReader.isPom("https://repo1.maven.org/maven2/commons-cli/commons-cli/1.2/commons-cli-1.2.pom"));
    }

}