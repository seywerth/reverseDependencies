package at.catalysts.tools.reversedependencies.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;

/**
 * integrative Tests for DependencyFileWriter using DependencyFileReader because of laziness
 *
 */
public class DependencyMatcherTest {

    @Test
    public void testWriteDependencyListWithUsedBy() {
        List<DependencyTree> result = DependencyMatcher.matchDependencyQuery(createReverseDependencyTrees(), createDependencies());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("junit", result.get(0).getDependency().getArtifactId());
        assertEquals("at.used", result.get(0).getDependency().getGroupId());
        assertEquals("3.2", result.get(0).getDependency().getVersion());
        assertEquals(0, result.get(0).getUses().size());
        assertEquals(1, result.get(0).getUsedBy().size());
        assertNotNull(result.get(0).getUsedBy().get(0).getDependency());
        assertEquals("ms-test", result.get(0).getUsedBy().get(0).getDependency().getArtifactId());
        assertEquals("at.test", result.get(0).getUsedBy().get(0).getDependency().getGroupId());
        assertEquals("1.0.1", result.get(0).getUsedBy().get(0).getDependency().getVersion());
    }

    public List<DependencyTree> createReverseDependencyTrees() {
        List<DependencyTree> trees = new ArrayList<>();
        trees.add(createDependencyTree("ms-test", "junit"));
        trees.add(createDependencyTree("ms-util", "test"));
        return trees;
    }

    public DependencyTree createDependencyTree(String artifactId, String usesArtifactId) {
        Dependency dependency = new Dependency(usesArtifactId, "at.used", "3.2");
        Dependency usedBy = new Dependency(artifactId, "at.test", "1.0.1");
        DependencyTree tree = new DependencyTree(dependency);
        tree.getUsedBy().add(new DependencyTree(usedBy));
        return tree;
    }

    public List<Dependency> createDependencies() {
        List<Dependency> deps = new ArrayList<>();
        deps.add(new Dependency("ms-test", null, "1.0.1"));
        deps.add(new Dependency("ms-util", null, "1.0.2"));
        return deps;
    }
}