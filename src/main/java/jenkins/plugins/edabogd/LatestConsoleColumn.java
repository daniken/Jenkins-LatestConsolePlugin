package jenkins.plugins.edabogd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;


/**
 * @author edabogd
 *
 */
public class LatestConsoleColumn extends ListViewColumn {
    
    private int logLength;
    private String regexFilter;
    
    /**
     * @param logLength - number of lines that wants to be displayed
     */
    @DataBoundConstructor
    public LatestConsoleColumn(int logLength, String regexFilter) {
        super();
        this.logLength = logLength;
        this.regexFilter = regexFilter;
    }
    
    public int getLogLength() {
        return logLength;
    }
    
    public String getRegexFilter() {
        return regexFilter;
    }
    
    
    /**
     * @param job current item
     * @return number of specified lines in the most recent console log as a string
     */
    public String getConsoleLog(@SuppressWarnings("rawtypes") Job job) {
        
        
        // checks if the input is really a instance of AbstractProject (AP)
        if( !(job instanceof AbstractProject<?, ?>))
            return "Faulty return in LatestConsoleColumn.getConsoleLog(). Job not found.";
        
        StringBuilder sb = new StringBuilder();
        
        try {
            if (job.getLastBuild() != null ) {
                       
                List <?> log = job.getLastBuild().getLog(500);                 // HARDCODED TO GET 500 LINES FROM CONSOLE LOG
                ArrayList<String> filteredLog = new ArrayList<String>();
                Pattern pattern = Pattern.compile(regexFilter);
                
                // search log for matching regex pattern and add to filtered log
                for (Object s : log) {

          
                    Matcher matcher = pattern.matcher(s.toString());
                    
                    // replaces "<" and ">" with their escape code so that jenkins don't think they are html tags
                    String fixedString = ((String) s).replace("<", "&#60;");
                    fixedString = fixedString.replace(">", "&#62;");
                    
                    if (matcher.find()) filteredLog.add(fixedString.toString());        
                }

                if (filteredLog.size() == 0) sb.append("No line matching the regex filter.");
                
                // append all lines if log is smaller than desired log length
                else if (filteredLog.size() < logLength) {
                    
                    for (String s : filteredLog) 
                        sb.append(s + "\n<br/>");
                    
                }
               
                // append specified number of lines from the filtered log
                else {
                    for (int i = 0; i < logLength; i++)
                        sb.append(filteredLog.get(filteredLog.size() - logLength + i) + "\n<br/>");

                }
            }
            
            // for jobs that never have been built
            else 
                sb.append("Job has never been built yet.");
        }

        catch (IOException e) {
            sb.append("N/A");
            e.printStackTrace();
        } 
        return sb.toString();
    }
    
    /**
     * @author edabogd
     *  
     */
    @Extension
    public static final class DescriptorImpl extends ListViewColumnDescriptor {
        
        @Override
        public String getDisplayName(){
            return "Latest Console Log";
        }
        
        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}
