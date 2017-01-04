package at.catalysts.tools.reversedependencies.data;

import java.util.ArrayList;
import java.util.List;

/**
 * object holding information to a dependency with its dependencies in use
 *
 */
public class DependencyTree {

    private Dependency dependency;

    private List<DependencyTree> uses = new ArrayList<>();
    private List<DependencyTree> usedBy = new ArrayList<>();

    public DependencyTree() {
    }

    public DependencyTree(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public List<DependencyTree> getUses() {
        return uses;
    }

    public void setUses(List<DependencyTree> uses) {
        this.uses = uses;
    }

    public List<DependencyTree> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<DependencyTree> usedBy) {
        this.usedBy = usedBy;
    }

}