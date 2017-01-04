package at.catalysts.tools.reversedependencies.data;

/**
 * object holding arguments
 *
 */
public class Setting {

    private String pathQueryDependencies;
    private String pathMatchDependencies;
    private String repositoryUrl;
    private String subdirectory = "";

    private String ignorePostfixCsv = "";
    private String pathRepositoryCache = "";
    private String pathOutputCsv = "dependencyMatches.csv";

    private boolean matchMajorVersionOnly = false;

    /**
     * check if repository should be queried
     */
    public boolean queryRepository() {
        return !pathQueryDependencies.isEmpty() && repositoryUrl != null;
    }

    /**
     * check if results should be matched
     */
    public boolean matchResult() {
        return !pathMatchDependencies.isEmpty();
    }

    /**
     * check if used by dependencies should be printed
     */
    public boolean printUsedBy() {
        return !matchMajorVersionOnly;
    }

    /**
     * check if repository cache is to be used
     */
    public boolean useCache() {
        return !pathRepositoryCache.isEmpty();
    }

    public String getPathQueryDependencies() {
        return pathQueryDependencies;
    }

    public void setPathQueryDependencies(String pathQueryDependencies) {
        this.pathQueryDependencies = pathQueryDependencies;
    }

    public String getPathMatchDependencies() {
        return pathMatchDependencies;
    }

    public void setPathMatchDependencies(String pathMatchDependencies) {
        this.pathMatchDependencies = pathMatchDependencies;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getSubdirectory() {
        return subdirectory;
    }

    public void setSubdirectory(String subdirectory) {
        this.subdirectory = subdirectory;
    }

    public String getIgnorePostfixCsv() {
        return ignorePostfixCsv;
    }

    public void setIgnorePostfixCsv(String ignorePostfixCsv) {
        this.ignorePostfixCsv = ignorePostfixCsv;
    }

    public String getPathRepositoryCache() {
        return pathRepositoryCache;
    }

    public void setPathRepositoryCache(String pathRepositoryCache) {
        this.pathRepositoryCache = pathRepositoryCache;
    }

    public String getPathOutputCsv() {
        return pathOutputCsv;
    }

    public void setPathOutputCsv(String pathOutputCsv) {
        this.pathOutputCsv = pathOutputCsv;
    }

    public boolean isMatchMajorVersionOnly() {
        return matchMajorVersionOnly;
    }

    public void setMatchMajorVersionOnly(boolean matchMajorVersionOnly) {
        this.matchMajorVersionOnly = matchMajorVersionOnly;
    }

}
