package at.catalysts.tools.reversedependencies.data;

import java.util.ArrayList;
import java.util.List;

/**
 * object holding information to a repository directory of folders and poms 
 *
 */
public class UrlDirectory {

    private List<String> folders = new ArrayList<>();
    private List<String> poms = new ArrayList<>();

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public List<String> getPoms() {
        return poms;
    }

    public void setPoms(List<String> poms) {
        this.poms = poms;
    }

}