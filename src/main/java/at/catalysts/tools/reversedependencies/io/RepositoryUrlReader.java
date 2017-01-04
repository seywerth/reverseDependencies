package at.catalysts.tools.reversedependencies.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import at.catalysts.tools.reversedependencies.data.Dependency;
import at.catalysts.tools.reversedependencies.data.DependencyTree;
import at.catalysts.tools.reversedependencies.data.UrlDirectory;

/**
 * Read dependencies from a repository like nexus with http-api
 * with access to maven pom files by hyperlinks.
 * The cacheWriter is used on the fly to prevent data loss.
 * 
 */
public class RepositoryUrlReader {

    private static final String REGEX_HYPERLINK = "<a\\ href=\"(.+?)\">";
    private static final String FILEENDING_POM = ".pom";

    private String repositoryUrl;
    private String subdirectory;
    private DependencyFileWriter cacheWriter;
    private List<DependencyTree> dependencyCache;
    private long cacheUsageCount;

    private String ignorePostfixCsv;

    public RepositoryUrlReader(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
        this.subdirectory = "";
        this.cacheWriter = null;
        this.setCacheUsageCount(0);
        this.dependencyCache = new ArrayList<>();
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

    public long getCacheUsageCount() {
        return cacheUsageCount;
    }

    public void setCacheUsageCount(long cacheUsageCount) {
        this.cacheUsageCount = cacheUsageCount;
    }

    /**
     * setup caching, create file if not existing
     * 
     * @param writer
     */
    public void setupCache(DependencyFileWriter cacheWriter, List<DependencyTree> dependencyCache) {
        this.cacheWriter = cacheWriter;
        if (!this.cacheWriter.fileExists()) {
            this.cacheWriter.createFile();
        }
        this.dependencyCache = dependencyCache;
    }

    /**
     * scan through web directory for dependencies in use
     * 
     * @param queryDependencies
     * @return reversed DependencyTrees
     */
    public List<DependencyTree> scanUrlDirectory(List<Dependency> queryDependencies) {
        List<DependencyTree> trees = scanUrlDirectory(this.repositoryUrl + this.subdirectory, queryDependencies);
        System.out.println("dep scanned: " + trees.size());
        return trees;
    }

    private List<DependencyTree> scanUrlDirectory(String url, List<Dependency> queryDependencies) {
        List<DependencyTree> result = new ArrayList<>();

        if (!this.dependencyCache.isEmpty()) {
            List<DependencyTree> cachedTrees = findCachedDependencyTrees(this.dependencyCache, this.repositoryUrl, url);
            if (!cachedTrees.isEmpty()) {
                this.cacheUsageCount += cachedTrees.size();
                return result;
            }
        }

        UrlDirectory urlDir = readUrlDirectory(this.repositoryUrl, url);
        if (!urlDir.getFolders().isEmpty()) {
            for (String folder : urlDir.getFolders()) {
                //System.out.println("scan subfolder: " + folder);
                List<DependencyTree> dependencyTrees = scanUrlDirectory(folder, queryDependencies);
                if (!dependencyTrees.isEmpty()) {
                    result.addAll(dependencyTrees);
                }
            }
        }
        if (!urlDir.getPoms().isEmpty()) {
            for (String pom : urlDir.getPoms()) {
                //System.out.println("check pom: " + pom);
                List<DependencyTree> dependencyTrees = lookForDependenciesInPom(pom, queryDependencies);
                if (!dependencyTrees.isEmpty()) {
                    result.addAll(dependencyTrees);
                }
            }
        }
        return result;
    }

    /**
     * find dependency by repository url from cache
     * 
     * @param repoUrl
     * @param cache
     * @param url
     * @return empty list if nothing found
     */
    public static List<DependencyTree> findCachedDependencyTrees(List<DependencyTree> cache, String repoUrl, String url) {
        List<DependencyTree> foundTrees = new ArrayList<>();
        Dependency currentDependency = parseDependencyFromUrl(repoUrl, url);
        if (currentDependency != null) {
            for (DependencyTree tree : cache) {
                if (tree.getUsedBy().isEmpty()) {
                    continue;
                }
                Dependency d = tree.getUsedBy().get(0).getDependency();
                if (d.getArtifactId().equals(currentDependency.getArtifactId()) && d.getGroupId().equals(currentDependency.getGroupId())
                        && d.getVersion().equals(currentDependency.getVersion())) {
                    foundTrees.add(tree);
                }
            }
        }
        return foundTrees;
    }

    /**
     * parse repository url into dependency<br>
     * use: repo/[groupId../..]/[artifactId]/[version]/file.pomx<br>
     * works only when artifactId is exactly within last pair of slashes
     * and groupId includes all directories between repoUrl and artifactId 
     * 
     * @param repoUrl
     * @param url
     * @return null if no valid dependency in url
     */
    public static Dependency parseDependencyFromUrl(String repoUrl, String url) {
        final String TMP_FILE = "x";

        if (url.startsWith(repoUrl)) {
            url = url.substring(repoUrl.length());
        }
        url = url.startsWith("/") ? url.substring(1) : url;
        if (!isSubFolderOrFile(url)) {
            return null;
        }
        url += TMP_FILE;
        String[] values = url.split("/");
        if (values.length > 3) {
            String artifactId = values[values.length-3];
            String groupId = Arrays.stream(values, 0, values.length-3).collect(Collectors.joining("."));
            String version = values[values.length-2];
            return new Dependency(artifactId, groupId, version);
        }
        return null;
    }

    private static UrlDirectory readUrlDirectory(String baseUrl, String url) {
        Pattern findurl = Pattern.compile(REGEX_HYPERLINK);
        UrlDirectory urlDirectory = new UrlDirectory();
        try {
            URLConnection yc = new URL(url).openConnection();
            //System.out.println("checking url: " + url);
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                Matcher match = findurl.matcher(inputLine);
                while (match.find()){
                    String link = match.group(1);
                    //System.out.println("link found: " + link);
                    if (isSubFolderOrFile(link) && !link.contains(url)) {
                        link = url + link;
                    }
                    if (isFolder(link)) {
                        urlDirectory.getFolders().add(link);
                        //System.out.println("folder found: " + link);
                    } else if (isPom(link)) {
                        urlDirectory.getPoms().add(link);
                        //System.out.println("pom found: " + link);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlDirectory;
    }

    /**
     * check string if subdirectory or file
     * 
     * @param str
     * @return
     */
    public static boolean isSubFolderOrFile(String str) {
        if (str.length() <= 2 || str.endsWith("./") || str.endsWith("../")) {
            return false;
        }
        return true;
    }

    /**
     * check string for folder
     * 
     * @param str
     * @return
     */
    public static boolean isFolder(String str) {
        if (isSubFolderOrFile(str) && str.endsWith("/")) {
            return true;
        }
        return false;
    }
    
    /**
     * check string for pom
     * 
     * @param str
     * @return
     */
    public static boolean isPom(String str) {
        if (str.endsWith(FILEENDING_POM) && str.length() > 4) {
            return true;
        }
        return false;
    }

    private List<DependencyTree> lookForDependenciesInPom(String pom, List<Dependency> queryDependencies) {
        List<DependencyTree> foundDTrees = new ArrayList<>(); //new DependencyTree();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new StringReader(readPomFromUrl(pom)));
            for (Dependency dep : queryDependencies) {
                if (dep.getArtifactId().isEmpty()) {
                    continue;
                }
                DependencyTree used = findDependent(model, dep, this.ignorePostfixCsv);
                if (used.getDependency() != null) {
                    DependencyTree deptree = new DependencyTree();
                    deptree.setDependency(used.getDependency());
                    Dependency usedBy = mapMavenModelToDependency(model);
                    deptree.getUsedBy().add(new DependencyTree(usedBy));
                    foundDTrees.add(deptree);
                    //System.out.println(used.getDependency().getArtifactId() + "," + used.getDependency().getGroupId() + "," + used.getDependency().getVersionId()
                    //    + "," + model.getArtifactId() + "," + model.getGroupId() + "," + model.getVersion());
                    if (this.cacheWriter != null) {
                        this.cacheWriter.appendToFile(used.getDependency(), usedBy);
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        return foundDTrees;
    }

    private static String readPomFromUrl(String pomUrl) {
        String file = "";
        try {
            URL url = new URL(pomUrl);
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) { 
                //System.out.println(inputLine);
                file += inputLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private static DependencyTree findDependent(Model model, Dependency dependencyToFind, String ignorePostfixCsv) {
        DependencyTree deptree = new DependencyTree();
        for (org.apache.maven.model.Dependency dep : model.getDependencies()) {
            if (matchesArtifactIdIgnorePostfix(dep.getArtifactId(), dependencyToFind.getArtifactId(), ignorePostfixCsv)) {
                String artifactId = dep.getArtifactId();
                String groupId = dep.getGroupId();
                String version = getModelDependencyVersion(model, dep.getVersion());
                Dependency dependency = new Dependency(artifactId, groupId, version);
                deptree.setDependency(dependency);
                break;
            }
        }
        return deptree;
    }

    public static boolean matchesArtifactIdIgnorePostfix(String artifactId, String mArtifactId, String ignorePostfixCsv) {
        if (artifactId.equals(mArtifactId)) {
            return true;
        }
        if (ignorePostfixCsv != null && !ignorePostfixCsv.isEmpty()) {
            String[] values = ignorePostfixCsv.split(",");
            if (values != null) {
                for (String value : values) {
                    if (artifactId.equals(mArtifactId + value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String getModelDependencyVersion(Model model, String version) {
        String out = version;
        if (version == null) {
            out = "";
        } else if (version.startsWith("$")) {
            out = model.getProperties().getProperty(version.substring(2, version.length()-1));
        }
        return out;
    }

    private static Dependency mapMavenModelToDependency(Model model) {
        String artifactId = model.getArtifactId();
        String groupId = model.getGroupId();
        if (groupId == null && model.getParent() != null) {
            groupId = model.getParent().getGroupId();
        }
        String version = model.getVersion();
        if (version == null && model.getParent() != null) {
            version = model.getParent().getVersion();
        }
        return new Dependency(artifactId, groupId, version);
    }

}